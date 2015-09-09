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
     * Creates a GeneralConnectionServlet.
     *
     * @param timeoutTime
     *            The time it takes before a connection times out.
     * @param secure
     *            True if the connection is allowing SSL connections.
     * @param connectLocally
     *            True if the server is connecting locally.
     */
    public LoginServlet(ServerInfo info) {
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
     * @param connectLocally
     *            <code>true</code> if the connection manager should use local
     *            connections, <code>false</code> otherwise
     * @param secure
     *            <code>true</code> if the connections should be secured,
     *            <code>false</code> otherwise
     *
     * @param info
     * @return a new connection manager object
     */
    @Override
    public MultiConnectionManager createConnectionManager(ServerInfo info) {
        return null;
    }
}
