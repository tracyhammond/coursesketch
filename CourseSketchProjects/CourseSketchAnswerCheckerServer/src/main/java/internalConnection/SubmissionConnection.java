package internalConnection;

import java.net.URI;
import java.nio.ByteBuffer;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
// import protobuf.srl.submission.Submission.SrlExperiment;
// import protobuf.srl.submission.Submission.SrlSolution;

// simport com.google.protobuf.InvalidProtocolBufferException;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class SubmissionConnection extends ConnectionWrapper {

	public SubmissionConnection(URI destination, GeneralConnectionServer parentServer) {
		super(destination, parentServer);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = GeneralConnectionServer.Decoder.parseRequest(buffer); // this contains the solution
		System.out.println(req.getSessionInfo());
		String[] sessionInfo = req.getSessionInfo().split("\\+");
		System.out.println(sessionInfo[1]);
		AnswerConnectionState state = (AnswerConnectionState) getStateFromId(sessionInfo[1]);
		System.out.println(state);
		if (req.getRequestType() == MessageType.DATA_REQUEST) {
			// SrlExperiment expr = state.getExperiment(sessionInfo[1]);
			// SrlSolution sol = null;
			/*try {
				sol = SrlSolution.parseFrom(req.getOtherData());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}*/
			// FIXME: implement comparison.
			// this could take a very very long time!

			// we need to this at least
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			GeneralConnectionServer.send(getConnectionFromState(state), builder.build());
		} else if (req.getRequestType() == MessageType.SUBMISSION) {
			// pass up the Id to the client
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			Session connection = getConnectionFromState(state);
			if (connection == null) {
				System.err.println("SOCKET IS NULL");
			}
			GeneralConnectionServer.send(getConnectionFromState(state), builder.build());
		}
	}
}