package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import utilities.ConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * A manager for holding all of the connections that were created.
 *
 * @author gigemjt
 */
public class AnswerConnectionManager extends MultiConnectionManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnswerConnectionManager.class);

    /**
     * IP address.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String SUBMISSION_ADDRESS = "192.168.56.202";

    /**
     * Port number.
     */
    private static final int SUBMISSION_PORT = 8883;

    /**
     * Creates a default {@link MultiConnectionManager}.
     *  @param parent  The server that is using this object.
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public AnswerConnectionManager(final AbstractServerWebSocketHandler parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void connectServers(final AbstractServerWebSocketHandler parent) {
        try {
            createAndAddConnection(parent, isConnectionLocal(), SUBMISSION_ADDRESS,
                    SUBMISSION_PORT, this.isSecure(), SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
