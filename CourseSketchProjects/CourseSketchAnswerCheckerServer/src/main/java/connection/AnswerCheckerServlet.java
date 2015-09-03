package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

/**
 * The default servlet it creates a single websocket instance that is then used
 * on all messages.
 *
 * To create a custom management of the connections use this version.
 *
 * @author gigemjt
 */
public class AnswerCheckerServlet extends ServerWebSocketInitializer {

    /**
     * Creates a AnswerCheckerServlet.
     *
     * @param timeoutTime
     *         The time it takes before a connection times out.
     * @param secure
     *         True if the connection is allowing SSL connections.
     * @param connectLocally
     *         True if the server is connecting locally.
     */
    public AnswerCheckerServlet(ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new AnswerCheckerServerWebSocketHandler(this);
    }

    /**
     * {@inheritDoc}
     * @param serverInfo
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new AnswerConnectionManager(this.getServer(), serverInfo.isLocal(), serverInfo.isSecure());
    }
}
