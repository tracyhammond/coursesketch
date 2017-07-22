package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.AnswerCheckerDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default servlet it creates a single websocket instance that is then used
 * on all messages.
 *
 * To create a custom management of the connections use this version.
 *
 * @author gigemjt
 */
public class AnswerCheckerServlet extends ServerWebSocketInitializer {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnswerCheckerServlet.class);

    private AnswerCheckerDatabase databaseReader;

    /**
     * Creates a AnswerCheckerServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public AnswerCheckerServlet(final ServerInfo serverInfo) {
        super(serverInfo);
        databaseReader = new AnswerCheckerDatabase(serverInfo);
    }

    /**
     * Sets the authentication websocket as an authenticator.
     */
    @Override
    protected void onReconnect() {
        try {
            databaseReader.startDatabase();
        } catch (DatabaseAccessException e) {
            LOG.error("Error starting coursesketch.util.util", e);
        }
        // Does nothing by default
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new AnswerCheckerServerWebSocketHandler(this, databaseReader);
    }

    /**
     * {@inheritDoc}
     * @return {@link AnswerConnectionManager}.
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new AnswerConnectionManager(this.getServer(), serverInfo);
    }
}
