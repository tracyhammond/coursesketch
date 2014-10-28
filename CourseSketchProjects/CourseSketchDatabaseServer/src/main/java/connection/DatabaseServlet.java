package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;

/**
 * A database specific servlet that creates a new Database server and Database
 * Connection Managers.
 *
 * @author gigemjt
 *
 */
public class DatabaseServlet extends ServerWebSocketInitializer {

    /**
     * Constructor for DatabaseServlet.
     *
     * @param timeoutTime
     *            The time before a stale connection times out.
     * @param secure
     *            True if the connection should use SSL
     * @param connectLocally
     *            True if the connection is a local connection.
     */
    public DatabaseServlet(final long timeoutTime, final boolean secure, final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new DatabaseServerWebSocketHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean secure) {
        return new DatabaseConnectionManager(getServer(), connectLocally, secure);
    }
}
