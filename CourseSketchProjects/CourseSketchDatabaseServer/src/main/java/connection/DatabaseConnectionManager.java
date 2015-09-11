package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
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
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String SUBMISSION_ADDRESS = "";

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
     * @param connectType If the connection is local or if it is remote
     * @param secure If ssl should be used.
     */
    public DatabaseConnectionManager(final AbstractServerWebSocketHandler parent, final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    /**
     * Called to connect this server to other servers.
     * @param serv The current server that the connections will be made from.
     */
    @Override
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public final void connectServers(final AbstractServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, this.isConnectionLocal(), SUBMISSION_ADDRESS, SUBMISSION_PORT, this.isSecure(),
                    SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
