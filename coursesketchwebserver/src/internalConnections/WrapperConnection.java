package internalConnections;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import proxyServer.ConnectionState;
import proxyServer.Decoder;
import proxyServer.ProxyServer;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class WrapperConnection extends WebSocketClient {

	ProxyServer parent;
	public WrapperConnection( URI serverUri , Draft draft , ProxyServer parent) {
		this( serverUri, draft );
		this.parent = parent;
	}
	
	public WrapperConnection( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public WrapperConnection( URI serverURI ) {
		super( serverURI );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "Open Recognition connection" );
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
		ConnectionState state = parent.getIdToState().get(Decoder.parseRequest(buffer).getSessionInfo());
		System.out.println("SESSION KEY: " + Decoder.parseRequest(buffer).getSessionInfo());
		System.out.println("STATE KEY: " + state.getKey());
		parent.getIdToConnection().get(state).send(buffer);
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

}