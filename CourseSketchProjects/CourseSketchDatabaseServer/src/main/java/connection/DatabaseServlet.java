package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

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
    public DatabaseServlet(ServerInfo serverInfo) {
        super(serverInfo);
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
     * @param serverInformation
     */
    @Override
    public final MultiConnectionManager createConnectionManager(ServerInfo serverInformation) {
        return new DatabaseConnectionManager(getServer(), , serverInformation.isLocal());
    }
}
