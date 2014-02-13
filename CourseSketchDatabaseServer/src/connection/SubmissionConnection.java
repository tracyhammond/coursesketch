package connection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.swing.Timer;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class SubmissionConnection extends WrapperConnection {

	public SubmissionConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent );
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
}