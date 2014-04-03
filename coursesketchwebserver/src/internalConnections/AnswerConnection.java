package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.drafts.Draft;
import protobuf.srl.request.Message.Request;
import proxyServer.ProxyServer;
import proxyServer.TimeManager;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class AnswerConnection extends WrapperConnection {

	public AnswerConnection(URI serverUri, Draft draft, MultiInternalConnectionServer parentServer) {
		super(serverUri, draft, parentServer);
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 *
	 * Also removes all identification that should not be sent to the client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		MultiConnectionState state = getStateFromId(MultiInternalConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());

		Request r = MultiInternalConnectionServer.Decoder.parseRequest(buffer);
		if (r.getRequestType() == Request.MessageType.TIME) {
			
			Request rsp = TimeManager.decodeRequest(r);
			if (rsp != null) {
				this.parentManager.send(rsp, r.getSessionInfo(), AnswerConnection.class);
			}
			return;
		}
		Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
		getConnectionFromState(state).send(result.toByteArray());
	}

	/*
	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "Open Recognition connection" );
		// if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
	}

	
	@Override
	public void onMessage( String message ) {
		System.out.println( "received: " + message );
	}
	 //Accepts messages and sends the request to the correct server and holds minimum client state.
	@Override
	public void onMessage(ByteBuffer buffer) {
		if (connection!=null) {
			ConnectionState state = parent.getIdToState().get(Decoder.parseRequest(buffer).getSessionInfo());
			System.out.println("SESSION KEY: " + Decoder.parseRequest(buffer).getSessionInfo());
			System.out.println("STATE KEY: " + state.getKey());
			parent.getIdToConnection().get(state).send(buffer);
		}
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
	}*/

}