package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.drafts.Draft;

import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DataConnection extends WrapperConnection {
	
	public DataConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent);
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
		Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
		getConnectionFromState(state).send(result.toByteArray());
	}
}