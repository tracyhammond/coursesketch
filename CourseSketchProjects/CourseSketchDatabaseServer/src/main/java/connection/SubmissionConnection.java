package connection;

import java.net.URI;
import java.nio.ByteBuffer;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ExperimentReview;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlExperimentList;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import database.user.UserClient;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class SubmissionConnection extends ConnectionWrapper {

	public SubmissionConnection(URI destination, GeneralConnectionServer parentServer) {
		super(destination, parentServer);
	}

	/**
	 * Splits the session info to find the correct level above to pass it up the chain to the correct client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = GeneralConnectionServer.Decoder.parseRequest(buffer); // this contains the solution
		System.out.println("Got a response from the submission server!");
		System.out.println(req.getSessionInfo());
		String[] sessionInfo = req.getSessionInfo().split("\\+");
		System.out.println(sessionInfo[1]);
		MultiConnectionState state = getStateFromId(sessionInfo[1]);
		System.out.println(state);
		if (req.getRequestType() == MessageType.DATA_REQUEST) {
			DataResult.Builder result2 = DataResult.newBuilder();
			// pass up the Id to the client
			try {
				DataResult result = DataResult.parseFrom(req.getOtherData());
				result2.clearResults();
				for (ItemResult item: result.getResultsList()) {
					if (item.hasAdvanceQuery() && item.getQuery() == ItemQuery.EXPERIMENT) {
						// we might have to do a lot of work here!
						ExperimentReview rev = ExperimentReview.parseFrom(item.getAdvanceQuery());
						if (rev.getShowUserNames()) {
							System.err.println("Attempting to change out usernames!");
							ItemResult returnResult = mapExperimentsToUser(item);
							result2.addResults(returnResult);
						}
					} else {
						result2.addResults(item); 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			Session connection = getConnectionFromState(state);
			builder.setOtherData(result2.build().toByteString());
			if (connection == null) {
				System.err.println("SOCKET IS NULL");
			}
			GeneralConnectionServer.send(getConnectionFromState(state), builder.build());
		}
	}

	/**
	 * Attaches user names to all of the experiments so that the users 
	 * @param item
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	private static final ItemResult mapExperimentsToUser(ItemResult item) throws InvalidProtocolBufferException {
		SrlExperimentList list = SrlExperimentList.parseFrom(item.getData());
		SrlExperimentList.Builder mappedList = SrlExperimentList.newBuilder();
		ItemResult.Builder result = ItemResult.newBuilder();
		for (SrlExperiment ment : list.getExperimentsList()) {
			if (ment.getUserId() == null) {
				System.err.println("USER ID IS NULL?");
				continue;
			}
			// TODO: get rid of this code in the loop! this is bad security!
			DB db= UserClient.getDB().getDB("login");
			DBCollection col = db.getCollection("CourseSketchUsers");
			DBCursor BAD_MAPPING_CURSOR = col.find(new BasicDBObject("ServerId" , ment.getUserId()));
			String userName = "" + BAD_MAPPING_CURSOR.next().get("UserName");
			System.out.println("New user name " + userName);
			SrlExperiment.Builder withUserName = ment.toBuilder();
			withUserName.setUserId(userName); // ID IS REPLACED WITH HUMAN READABLE USERNAME!
			mappedList.addExperiments(withUserName);
		}
		result.setData(mappedList.build().toByteString());
		result.setQuery(item.getQuery());
		if (item.hasErrorMessage()) {
			result.setErrorMessage(item.getErrorMessage());
		}
		return result.build();
	}
}