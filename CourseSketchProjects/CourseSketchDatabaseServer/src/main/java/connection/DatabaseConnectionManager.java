package connection;

import interfaces.IServerWebSocketHandler;
import interfaces.MultiConnectionManager;
import utilities.ConnectionException;

/**
 * Creates a connection to the submission server.
 */
public class DatabaseConnectionManager extends MultiConnectionManager {

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
    public DatabaseConnectionManager(final IServerWebSocketHandler parent, final boolean connectType, final boolean secure) {
        super(parent, connectType, secure);
    }

    /**
     * Called to connect this server to other servers.
     * @param serv The current server that the connections will be made from.
     */
    @Override
    public final void connectServers(final IServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, this.isConnectionLocal(), "srl02.tamu.edu", SUBMISSION_PORT, this.isSecure(), SubmissionClientConnection.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
