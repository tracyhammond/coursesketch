package serverfront;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import internalconnections.ProxyConnectionManager;

/**
 *
 */
@SuppressWarnings("serial")
public final class ProxyServlet extends ServerWebSocketInitializer {

    /**
     * @param timeoutTime
     *            The time it takes before a connection times out.
     * @param secure
     *            True if the connection is allowing SSL connections.
     * @param connectLocally
     *            True if the server is connecting locally.
     */
    public ProxyServlet(final long timeoutTime, final boolean secure, final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new ProxyServerWebSocketHandler(this);
    }

    /**
     * @param connectLocally
     *            True if the connection is acting as if it is on a local
     *            computer (used for testing)
     * @param secure
     *            True if the connection is using SSL.
     * @return {@link internalconnections.ProxyConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean secure) {
        return new ProxyConnectionManager(getServer(), connectLocally, secure);
    }

    /**
     * initializes the listeners for the servers.
     */
    @Override
    protected void onReconnect() {
        ((ProxyServerWebSocketHandler) getServer()).initializeListeners();
    }
}
