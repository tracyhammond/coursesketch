package connection;

import java.net.URI;
import java.nio.ByteBuffer;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServer.Decoder;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public class DataConnection extends ConnectionWrapper {

	public DataConnection(URI destination, GeneralConnectionServer parentServer) {
		super(destination, parentServer);
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 *
	 * Also removes all identification that should not be sent to the client.
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		Request req = Decoder.parseRequest(buffer);
		
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
	}
}