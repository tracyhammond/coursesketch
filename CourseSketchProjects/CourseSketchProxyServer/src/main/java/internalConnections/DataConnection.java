package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import connection.ConnectionException;
import connection.TimeManager;



/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public class DataConnection extends ConnectionWrapper {

	public DataConnection(URI destination, GeneralConnectionServer parent) {
		super(destination, parent);
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 *
	 * Also removes all identification that should not be sent to the client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = GeneralConnectionServer.Decoder.parseRequest(buffer);

		if (req.getRequestType() == Request.MessageType.TIME) {

			Request rsp = TimeManager.decodeRequest(req);
			if (rsp != null) {
				try {
					this.parentManager.send(rsp, req.getSessionInfo(), DataConnection.class);
				} catch (ConnectionException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			MultiConnectionState state = getStateFromId(GeneralConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());

			Request r = GeneralConnectionServer.Decoder.parseRequest(buffer);
			Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
			GeneralConnectionServer.send(getConnectionFromState(state), result);
		}
	}
}