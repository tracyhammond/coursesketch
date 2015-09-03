package serverfront;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.request.Message.Request;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public final class DefaultWebSocketHandler extends ServerWebSocketHandler {

    /**
     * @param parent
     *            The servlet made for this server.
     */
    public DefaultWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
    }

    /**
     * Tries to sync time with this new client.
     *
     * @param conn
     *            the connection that is being opened.
     */
    @Override
    public void openSession(final SocketSession conn) {
    }

    /**
     * {@inheritDoc} Routes the given request to the correct server.
     */
    @Override
    public void onMessage(final SocketSession conn, final Request req) {
    }

}
