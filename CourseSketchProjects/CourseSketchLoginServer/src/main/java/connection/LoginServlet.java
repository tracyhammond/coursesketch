package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

/**
 * Creates a servlet specific to the login server.
 */
@SuppressWarnings("serial")
public final class LoginServlet extends ServerWebSocketInitializer {

    /**
     * Creates a LoginServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public LoginServlet(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link LoginServerWebSocketHandler}.
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new LoginServerWebSocketHandler(this, this.getServerInfo());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The login server does not need to manage multiple connections so it returns null.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     * @return {@code null}.
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return null;
    }
}
