package handlers;

import protobuf.srl.request.Message.Request;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.google.protobuf.ByteString;

import connection.DataClientConnection;
import database.DatabaseClient;
import database.UpdateHandler;

public class SubmissionRequestHandler {
	
	private static final UpdateHandler updateHandler = new UpdateHandler();
	public static Request handleRequest(Request req, MultiConnectionManager internalConnections) {
		final String sessionInfo = req.getSessionInfo();
		try {
			String resultantId = null;
			if (updateHandler.addRequest(req)) {
				System.out.println("Update is finished building!");
				ByteString data = null;
				if (updateHandler.isSolution(sessionInfo)) {
					resultantId = DatabaseClient.saveSolution(updateHandler.getSolution(sessionInfo), DatabaseClient.getInstance());
					if (resultantId != null) {
						SrlSolution.Builder builder = SrlSolution.newBuilder(updateHandler.getSolution(sessionInfo));
						builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
						data = builder.build().toByteString();
					}
				} else {
					System.out.println("Saving experiment");
					resultantId = DatabaseClient.saveExperiment(updateHandler.getExperiment(sessionInfo), DatabaseClient.getInstance());
					if (resultantId != null) {
						SrlExperiment.Builder builder = SrlExperiment.newBuilder(updateHandler.getExperiment(sessionInfo));
						builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
						data = builder.build().toByteString();
					}
				}
				Request.Builder build = Request.newBuilder(req);
				build.setResponseText("Submission Succesful!");
				build.clearOtherData();
				build.setSessionInfo(sessionInfo);
				System.out.println(sessionInfo);
				if (resultantId != null) {
					// it can be null if this solution has already been stored
					if (data != null) {
						// passes the data to the database for connecting
						build.setOtherData(data);
						internalConnections.send(build.build(), "", DataClientConnection.class);
					}
				}
				updateHandler.clearSubmission(sessionInfo);
				// sends the response back to the answer checker which can then send it back to the client.
				return build.build();
			} 
			//ItemResult 
		} catch (Exception e) {
			updateHandler.clearSubmission(sessionInfo);
			Request.Builder build = Request.newBuilder();
			build.setRequestType(Request.MessageType.ERROR);
			if (e.getMessage() != null) {
				build.setResponseText(e.getMessage());
			}
			build.setSessionInfo(sessionInfo);
			e.printStackTrace();
			return build.build();
		}
		return null;
	}
}
