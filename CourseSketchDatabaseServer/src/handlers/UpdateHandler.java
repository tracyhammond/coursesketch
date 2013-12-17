package handlers;

import java.util.ArrayList;
import java.util.HashMap;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.request.Message.Request;

public class UpdateHandler {
	HashMap<String, ListInstance> sessionToInstance = new HashMap<String, ListInstance>();
	public void addRequest(Request req) {
		if (!sessionToInstance.containsKey(req.getSessionInfo())) {
			sessionToInstance.put(req.getSessionInfo(), new ListInstance());
		}
		sessionToInstance.get(req.getSessionInfo()).addRequest(req);
	}

	class ListInstance {
		SrlUpdateList.Builder list = SrlUpdateList.newBuilder();
		SrlUpdateList result;

		private boolean started = false;
		private boolean finished = false;
		private int currentIndex = -1;
		ArrayList<SrlUpdate> buffer = new ArrayList<SrlUpdate>();

		/**
		 * There are three different things that can be submitted.
		 * #1 an SrlUpdateList
		 * #2 an SrlUpdate
		 * @param req
		 */
		public void addRequest(Request req) {
			if (finished) {
				return;
			}
			try {
				SrlUpdate update = SrlUpdate.parseFrom(req.getOtherData());
				if (started && update.getCommands(0).getCommandType() == CommandType.CLOSE_SYNC) {
					started = false;
					finished = true;
					result = list.build();
					return;
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
				return;
			} catch(Exception e) {
			}
			// should only get here if there is an exception
			try {
				result = SrlUpdateList.parseFrom(req.getOtherData());
				started = false;
				finished = true;
			} catch (Exception e) {
				
			}
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
}
