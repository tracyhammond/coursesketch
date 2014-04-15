package tallNateConnection;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class WrapperConnection extends WebSocketClient {

	protected MultiInternalConnectionServer parentServer;
	protected MultiConnectionManager parentManager;
	public WrapperConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parentServer) {
		super( serverUri, draft );
		this.parentServer = parentServer;
		if (parentServer == null) {
			System.out.println("Warning Parent is null");
		}
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "Open Wrapper Connection" );
		// if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
	}

	@Override
	public void onMessage( String message ) {
		System.out.println( "received: " + message );
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		MultiConnectionState state = getStateFromId(MultiInternalConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());
		getConnectionFromState(state).send(buffer);
	}

	/**
	 * For fragments This is ignored right now.
	 * @param fragment
	 */
	public void onFragment( Framedata fragment ) {
		System.out.println( "received fragment: " + new String( fragment.getPayloadData().array() ) );
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		// The code are documented in class org.java_websocket.framing.CloseFrame
		System.out.println("Connection closed by " + ( remote ? "remote peer" : "us" ) +" with code: " + code + " because of: " + reason);
	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}

	protected MultiConnectionState getStateFromId(String key) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToState() == null) {
			System.out.println("null getIdToState");
		}
		return parentServer.getIdToState().get(key);
	}
	
	protected WebSocket getConnectionFromState(MultiConnectionState state) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToConnection() == null) {
			System.out.println("null IdToConnection");
		}
		return parentServer.getIdToConnection().get(state);
	}
}