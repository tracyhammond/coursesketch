package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
//import sun.rmi.runtime.Log;
import utilities.ConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Creates a connection to the submission server.
 */
public class DatabaseConnectionManager extends MultiConnectionManager {

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
    public final void connectServers(final AbstractServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, this.isConnectionLocal(), "srl02.tamu.edu", SUBMISSION_PORT, this.isSecure(),
                    SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.info("Exception: {}", e);
        }
    }
}
