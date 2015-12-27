package connection;

import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.services.submission.SubmissionWebSocketClient;
import utilities.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * Creates a connection to the submission server.
 */
public class DatabaseConnectionManager extends MultiConnectionManager {

    /**
     *  Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnectionManager.class);

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
            createAndAddConnection(serv, this.isConnectionLocal(), AuthenticationWebSocketClient.ADDRESS, AuthenticationWebSocketClient.PORT,
                    this.isSecure(), AuthenticationWebSocketClient.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }

        try {
            createAndAddConnection(serv, this.isConnectionLocal(), SubmissionWebSocketClient.ADDRESS, SubmissionWebSocketClient.PORT,
                    this.isSecure(), SubmissionWebSocketClient.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
