package connection;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class SolutionConnection extends WrapperConnection {

	public SolutionConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent );
	}

	/**
	 * Splits the session info to find the correct level above to pass it up the chain to the correct client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = MultiInternalConnectionServer.Decoder.parseRequest(buffer); // this contains the solution
		System.out.println(req.getSessionInfo());
		String[] sessionInfo = req.getSessionInfo().split("\\+");
		System.out.println(sessionInfo[1]);
		MultiConnectionState state = getStateFromId(sessionInfo[1]);
		System.out.println(state);
		if (req.getRequestType() == MessageType.DATA_REQUEST) {
			// pass up the Id to the client
			Request.Builder builder = Request.newBuilder(req);
			builder.setSessionInfo(sessionInfo[0]);
			WebSocket connection = getConnectionFromState(state);
			if (connection == null) {
				System.err.println("SOCKET IS NULL");
			}
			getConnectionFromState(state).send(builder.build().toByteArray());
		}
	}
}