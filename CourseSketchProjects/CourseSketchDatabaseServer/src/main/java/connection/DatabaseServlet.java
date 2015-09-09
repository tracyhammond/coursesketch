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
     * @param serverInformation {@link ServerInfo} Contains all of the information about the server.
     */
    public DatabaseServlet(final ServerInfo serverInformation) {
        super(serverInformation);
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
     * @param serverInformation {@link ServerInfo} Contains all of the information about the server.
     * @return {@link DatabaseConnectionManager}.
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInformation) {
        return new DatabaseConnectionManager(getServer(), serverInformation);
    }
}
