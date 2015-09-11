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
     * @param info {@link ServerInfo} Contains all of the information about the server.
     */
    public LoginServlet(final ServerInfo info) {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new LoginServerWebSocketHandler(this, this.getServerInfo());
    }

    /**
     * We do not need to manage multiple connections so we might as well just
     * make it return null.
     *
     * @param info {@link ServerInfo} Contains all of the information about the server.
     * @return a null
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo info) {
        return null;
    }
}
