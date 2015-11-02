package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import utilities.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * Creates a connection to the submission server.
 */
public class DatabaseConnectionManager extends MultiConnectionManager {

    /**
     * IP address for submission server.
     */
    private static final String SUBMISSION_ADDRESS = "SUBMISSION_IP_PROP";

    /**
     *  Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnectionManager.class);

    /**
     * The port number of the submission server.
     */
    private static final int SUBMISSION_PORT = 8883;

    /**
     * A constructor for the multi connection manager.
     * @param parent The parent server
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public DatabaseConnectionManager(final AbstractServerWebSocketHandler parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
    }

    /**
     * Called to connect this server to other servers.
     * @param serv The current server that the connections will be made from.
     */
    @Override
    public final void connectServers(final AbstractServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, this.isConnectionLocal(), SUBMISSION_ADDRESS, SUBMISSION_PORT, this.isSecure(),
                    SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
