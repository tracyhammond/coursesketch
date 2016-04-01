package coursesketch.serverfront;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import protobuf.srl.request.Message.Request;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public final class DefaultWebSocketHandler extends ServerWebSocketHandler {

    /**
     * Creates a websocket handler with the initializer and info.
     *
     * @param parent
     *            The servlet made for this server.
     * @param info
     *            Contains information about the parent.
     */
    public DefaultWebSocketHandler(final ServerWebSocketInitializer parent, final ServerInfo info) {
        super(parent, info);
    }

    /**
     * Tries to sync time with this new client.
     *
     * @param conn
     *            The connection that is being opened.
     */
    @Override
    public void openSession(final SocketSession conn) {
        // Defined by the specific implementations.
    }

    /**
     * {@inheritDoc} Routes the given request to the correct server.
     */
    @Override
    public void onMessage(final SocketSession conn, final Request req) {
        // Defined by the specific implementations.
    }
}
