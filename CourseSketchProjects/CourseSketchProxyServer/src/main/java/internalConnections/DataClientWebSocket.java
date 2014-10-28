package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import utilities.ConnectionException;
import utilities.TimeManager;

<<<<<<< HEAD:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/DataConnection.java
=======
/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket()
public final class DataClientWebSocket extends ClientWebSocket {

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the database server.
     * @param parent
     *            The proxy server instance.
     */
    public DataClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parent) {
        super(destination, parent);
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
     *
     * @param buffer
     *            The message that is received by this object.
     */
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final Request req = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
>>>>>>> origin/master:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/DataClientWebSocket.java


<<<<<<< HEAD:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/DataConnection.java
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
=======
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, req.getSessionInfo(), DataClientWebSocket.class);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            final MultiConnectionState state = getStateFromId(AbstractServerWebSocketHandler.Decoder.parseRequest(buffer).getSessionInfo());

            final Request request = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
            // Strips away identification.
            final Request result = ProxyConnectionManager.createClientRequest(request);
            this.getParentServer().send(getConnectionFromState(state), result);
        }
    }
}
>>>>>>> origin/master:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/DataClientWebSocket.java
