package connection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.swing.Timer;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ExperimentReview;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlExperimentList;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class SubmissionConnection extends WrapperConnection {

	private DB TEMP_BAD_DB; // going against all styles with these variables to make them go away as quickly as possible!
	
	
	public SubmissionConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent );
		try {
			TEMP_BAD_DB = new MongoClient("goldberglinux.tamu.edu").getDB("login");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Splits the session info to find the correct level above to pass it up the chain to the correct client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = MultiInternalConnectionServer.Decoder.parseRequest(buffer); // this contains the solution
		System.out.println("Got a response from the submission server!");
		System.out.println(req.getSessionInfo());
		String[] sessionInfo = req.getSessionInfo().split("\\+");
		System.out.println(sessionInfo[1]);
		MultiConnectionState state = getStateFromId(sessionInfo[1]);
		System.out.println(state);
		if (req.getRequestType() == MessageType.DATA_REQUEST) {
			// pass up the Id to the client
			try {
				DataResult result = DataResult.parseFrom(req.getOtherData());
				for (ItemResult item: result.getResultsList()) {
					if (item.hasAdvanceQuery() && item.getQuery() == ItemQuery.EXPERIMENT) {
						// we might have to do a lot of work here!
						ExperimentReview rev = ExperimentReview.parseFrom(item.getAdvanceQuery());
						if (rev.getShowUserNames()) {
							mapExperimentsToUser(item);
						}
					}
				}
			}catch (Exception e) {
				
			}
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			WebSocket connection = getConnectionFromState(state);
			if (connection == null) {
				System.err.println("SOCKET IS NULL");
			}
			getConnectionFromState(state).send(builder.build().toByteArray());
		}
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		super.onClose(code, reason, remote);
		System.out.println("Attempting to reconnect");
		if (remote && false) {
			// TODO: create the connection so e do not have to type reconnect on our computer 
			Timer t = new Timer(5000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Attempting to reconnect to servers");
					parentServer.reconnect();
				}
			});
			t.start();
		}
	}

	/**
	 * Attaches user names to all of the experiments so that the users 
	 * @param item
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	private final ItemResult mapExperimentsToUser(ItemResult item) throws InvalidProtocolBufferException {
		SrlExperimentList list = SrlExperimentList.parseFrom(item.getData());
		SrlExperimentList.Builder mappedList = SrlExperimentList.newBuilder();
		ItemResult.Builder result = ItemResult.newBuilder();
		for (SrlExperiment ment : list.getExperimentsList()) {
			// TODO: get rid of this code in the loop! this is bad security!
			DBCursor BAD_MAPPING_CURSOR = TEMP_BAD_DB.getCollection("CourseSketchUsers").find(new BasicDBObject("ServerId" , ment.getUserId()));
			String userName = "" + BAD_MAPPING_CURSOR.next().get("UserName");
			SrlExperiment.Builder withUserName = ment.toBuilder();
			withUserName.setUserId(userName); // ID IS REPLACED WITH HUMAN READABLE USERNAME!
			mappedList.addExperiments(withUserName);
		}
		result.setData(mappedList.build().toByteString());
		result.setQuery(item.getQuery());
		if (item.hasErrorMessage()) {
			result.setErrorMessage(item.getErrorMessage());
		}
		return item;
	}
}