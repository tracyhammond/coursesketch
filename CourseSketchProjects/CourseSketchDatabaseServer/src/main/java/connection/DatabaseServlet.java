package connection;

import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;

/**
 * A database specific servlet that creates a new Database server and Database
 * Connection Managers.
 *
 * @author gigemjt
 *
 */
public class DatabaseServlet extends GeneralConnectionServlet {

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
    public final GeneralConnectionServer createServerSocket() {
        return new DatabaseServer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean secure) {
        return new DatabaseConnectionManager(getServer(), connectLocally, secure);
    }
}
