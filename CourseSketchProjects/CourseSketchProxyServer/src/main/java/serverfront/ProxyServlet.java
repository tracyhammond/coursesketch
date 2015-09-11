package serverfront;

import connection.ProxyConnectionManager;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

/**
 *
 */
@SuppressWarnings("serial")
public final class ProxyServlet extends ServerWebSocketInitializer {

    /**
     * @param info {@link ServerInfo} Contains all of the information about the server.
     */
    public ProxyServlet(final ServerInfo info) {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new ProxyServerWebSocketHandler(this);
    }

    /**
     * @param info {@link ServerInfo} Contains all of the information about the server.
     * @return {@link ProxyConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo info) {
        return new ProxyConnectionManager(getServer(), info);
    }

    /**
     * initializes the listeners for the servers.
     */
    @Override
    protected void onReconnect() {
        ((ProxyServerWebSocketHandler) getServer()).initializeListeners();
    }
}
