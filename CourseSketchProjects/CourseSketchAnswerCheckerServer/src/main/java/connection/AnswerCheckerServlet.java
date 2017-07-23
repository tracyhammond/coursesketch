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
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    AnswerCheckerServlet(final ServerInfo serverInfo) {
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
     * @return {@link AnswerConnectionManager}.
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new AnswerConnectionManager(this.getServer(), serverInfo);
    }
}
