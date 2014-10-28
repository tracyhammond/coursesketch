package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;

@SuppressWarnings("serial")
public final class LoginServlet extends ServerWebSocketInitializer {

    public LoginServlet(final long timeoutTime, final boolean secure,
            final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new LoginServerWebSocketHandler(this);
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
     * @return a new connection manager object
     */
    @Override
<<<<<<< HEAD
    protected MultiConnectionManager createConnectionManager(
            final boolean connectLocally, final boolean secure) {
=======
    public MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean secure) {
>>>>>>> origin/master
        return null;
    }
}
