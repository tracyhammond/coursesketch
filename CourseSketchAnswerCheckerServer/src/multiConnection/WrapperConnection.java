package multiConnection;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class WrapperConnection extends WebSocketClient {

	protected MultiInternalConnectionServer parent;
	public WrapperConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft );
		this.parent = parent;
		if (parent == null) {
			System.out.println("Warning Parent is null");
		}
	}

	public WrapperConnection( URI serverUri , Draft draft ) {
		this(serverUri, draft, null);
	}

	public WrapperConnection( URI serverURI ) {
		super( serverURI );
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

	public void onFragment( Framedata fragment ) {
		System.out.println( "received fragment: " + new String( fragment.getPayloadData().array() ) );
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );
	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}

	protected MultiConnectionState getStateFromId(String key) {
		return parent.getIdToState().get(key);
	}
	
	protected WebSocket getConnectionFromState(MultiConnectionState state) {
		if (parent == null) {
			System.out.println("null parent");
		}
		if (parent.getIdToConnection() == null) {
			System.out.println("null IdToConnection");
		}
		return parent.getIdToConnection().get(state);
	}
}