package coursesketch.serverfront;

import coursesketch.server.frontend.ServerWebSocketHandler;
import coursesketch.server.frontend.ServerWebSocketInitializer;
import connection.ProxyConnectionManager;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

/**
 *
 */
@SuppressWarnings("serial")
public final class ProxyServlet extends ServerWebSocketInitializer {

    /**
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public ProxyServlet(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new ProxyServerWebSocketHandler(this);
    }

    /**
     * {@inheritDoc}
     * @return {@link ProxyConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new ProxyConnectionManager(getServer(), serverInfo);
    }

    /**
     * initializes the listeners for the servers.
     */
    @Override
    protected void onReconnect() {
        ((ProxyServerWebSocketHandler) getServer()).initializeListeners();
    }
}
