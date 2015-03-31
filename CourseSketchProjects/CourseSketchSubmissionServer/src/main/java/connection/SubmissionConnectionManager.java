package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ConnectionException;
import utilities.LoggingConstants;

/**
 * A manager for holding all of the connections that were created.
 *
 * @author gigemjt
 */
public final class SubmissionConnectionManager extends MultiConnectionManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionConnectionManager.class);

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
            createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", PORT, isSecure(), DataClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
