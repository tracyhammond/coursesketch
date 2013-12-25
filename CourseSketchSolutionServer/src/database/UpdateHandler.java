package database;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.request.Message.Request;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;

public class UpdateHandler {
	HashMap<String, ListInstance> sessionToInstance = new HashMap<String, ListInstance>();
	public boolean addRequest(Request req) throws Exception {
		SrlUpdateList updates = null;
		if (!sessionToInstance.containsKey(req.getSessionInfo())) {
			SubmissionInstance instance = new SubmissionInstance();
			sessionToInstance.put(req.getSessionInfo(), instance);
			try {
				if (req.getResponseText().equals("student")) {
					SrlExperiment experiment = SrlExperiment.parseFrom(req.getOtherData());
					instance.setExperiment(experiment);
					updates = experiment.getSubmission().getUpdateList(); // gets the first update chunk
				} else {
					SrlSolution solution = SrlSolution.parseFrom(req.getOtherData());
					instance.setSolution(solution);
					updates = solution.getSubmission().getUpdateList(); // gets the first update chunk
					// if this is the first submission it will contain stuff regarding that submission
				}
			} catch(Exception e) {
			}
		}
		if (updates == null) {
			try {
				updates = SrlUpdateList.parseFrom(req.getOtherData());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		if (updates == null) {
			throw new Exception("Mismatched Message Exception");
		}
		return sessionToInstance.get(req.getSessionInfo()).addRequest(updates);
	}

	public boolean isSolution(String sessionInfo) {
		return ((SubmissionInstance) sessionToInstance.get(sessionInfo)).isSolution();
	}
	
	public SrlSolution getSolution(String sessionInfo) throws Exception {
		return ((SubmissionInstance) sessionToInstance.get(sessionInfo)).getSolution();
	}
	
	public SrlExperiment getExperiment(String sessionInfo) throws Exception {
		return ((SubmissionInstance) sessionToInstance.get(sessionInfo)).getExperiment();
	}

	/**
	 * Allows a list of updates to be built slowly over time ensuring the correct order.
	 *
	 * The correct ordered will happen even if they arrive out of order.
	 * @author gigemjt
	 *
	 */
	private class ListInstance {
		SrlUpdateList.Builder list = SrlUpdateList.newBuilder();
		protected SrlUpdateList result;

		private boolean started = false;
		private boolean finished = false;
		private int currentIndex = -1;
		ArrayList<SrlUpdate> buffer = new ArrayList<SrlUpdate>();
		
		public boolean isFinished() {
			return finished;
		}

		/**
		 * There are three different things that can be submitted.
		 * #1 an SrlUpdateList
		 * #2 an SrlUpdate
		 * @param req
		 */
		public boolean addRequest(SrlUpdateList updates) {
			if (finished) {
				return true;
			}
			try {
				for (SrlUpdate update: updates.getListList()) {
					//SrlUpdate update = SrlUpdate.parseFrom(req.getOtherData());
					if (started && update.getCommands(update.getCommandsCount() - 1).getCommandType() == CommandType.CLOSE_SYNC) {
						started = false;
						finished = true;
						result = list.build();
						return true;
					}
					if (!started && update.getCommands(0).getCommandType() ==CommandType.OPEN_SYNC) {
						started = true;
					} else if (!started) {
						insertInBuffer(update);
					} else {
						if (update.getCommandNumber() == currentIndex + 1) {
							addToList(update);
							currentIndex +=1;
							searchBuffer();
						}
				}
				}
				return false;
			} catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * Goes through the sorted buffer and checks to see if the next element has the correct number.
		 *
		 */
		private void searchBuffer() {
			SrlUpdate nextUpdate = null;
			while (buffer.size() > 0) {
				nextUpdate = buffer.get(0);
				if (nextUpdate.getCommandNumber() == currentIndex + 1) {
					buffer.remove(0);
					addToList(nextUpdate);
					currentIndex +=1;
				} else {
					break; // quit going we will try again later.
				}
			}
		}

		/**
		 * Inserts the update into the buffer.
		 *
		 * The list will be sorted before this insertion and it will remain sorted after this insertion.
		 */
		private void insertInBuffer(SrlUpdate update) {
			// TODO Insert an update into the list so that the list is always sorted.
		}

		private void addToList(SrlUpdate update) {
			list.addList(update);
			// may execute the action while inserting to the list.
			// this will be called in a sorted order.
		}
	}

	private class SubmissionInstance extends ListInstance {
		com.google.protobuf.GeneratedMessage submission = null;
		private boolean isSolution = false;
		public boolean isSolution() {
			return isSolution;
		}

		public void setExperiment(SrlExperiment experiment) {
			isSolution = false;
			submission = experiment;
		}
		
		public void setSolution(SrlSolution solution) {
			isSolution = true;
			submission = solution;
		}

		public SrlSolution getSolution() throws Exception {
			if (!isSolution) {
				throw new Exception("Incorrect Message Type: cannot retrieve solution");
			}
			if (!this.isFinished()) {
				throw new Exception("cannot retrieve incomplete submission");
			}
			SrlSolution.Builder builder = SrlSolution.newBuilder((SrlSolution) submission);
			SrlSubmission.Builder newSubmission = SrlSubmission.newBuilder();
			if (((SrlSolution) submission).hasSubmission()) { 
				SrlSubmission oldSubmission = ((SrlSolution) submission).getSubmission();
				if (oldSubmission.hasId()) {
					newSubmission.setId(oldSubmission.getId());
				}
			}
			newSubmission.setUpdateList(this.result);
			return builder.setSubmission(newSubmission).build();
		}

		public SrlExperiment getExperiment() throws Exception {
			if (isSolution) {
				throw new Exception("Incorrect Message Type: cannot retrieve solution");
			}
			if (!this.isFinished()) {
				throw new Exception("cannot retrieve incomplete submission");
			}
			SrlExperiment.Builder builder = SrlExperiment.newBuilder((SrlExperiment) submission);
			SrlSubmission.Builder newSubmission = SrlSubmission.newBuilder();
			if (((SrlExperiment) submission).hasSubmission()) { 
				SrlSubmission oldSubmission = ((SrlExperiment) submission).getSubmission();
				if (oldSubmission.hasId()) {
					newSubmission.setId(oldSubmission.getId());
				}
			}
			newSubmission.setUpdateList(this.result);
			return builder.setSubmission(newSubmission).build();
		}
	}
}
