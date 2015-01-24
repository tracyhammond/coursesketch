package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import utilities.ConnectionException;

/**
 * A manager for holding all of the connections that were created.
 *
 * @author gigemjt
 */
public final class SubmissionConnectionManager extends MultiConnectionManager {

    /**
     * Port number.
     */
    private static final int PORT = 8885;

    /**
     * Creates a default {@link MultiConnectionManager}.
     *
     * @param parent  The server that is using this object.
     * @param isLocal True if the connection should be for a local server instead of
     *                 a remote server.
     * @param isSecure  True if the connections should be isSecure.
     */
    public SubmissionConnectionManager(final AbstractServerWebSocketHandler parent, final boolean isLocal, final boolean isSecure) {
        super(parent, isLocal, isSecure);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void connectServers(final AbstractServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, isConnectionLocal(), "10.9.74.201", PORT, isSecure(), DataClientWebSocket.class);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
