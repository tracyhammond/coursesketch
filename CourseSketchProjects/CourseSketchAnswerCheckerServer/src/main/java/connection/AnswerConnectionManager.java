package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import protobuf.srl.request.Message;
import utilities.ConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ExceptionUtilities;
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
     * Port number.
     */
    private static final int PORT = 8883;

    /**
     * Creates a default {@link MultiConnectionManager}.
     *
     * @param parent  The server that is using this object.
     * @param isLocal True if the connection should be for a local server instead of
     *                 a remote server.
     * @param isSecure  True if the connections should be secure.
     */
    public AnswerConnectionManager(final AbstractServerWebSocketHandler parent,
            final boolean isLocal, final boolean isSecure) {
        super(parent, isLocal, isSecure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void connectServers(final AbstractServerWebSocketHandler parent) {
        try {
            createAndAddConnection(parent, isConnectionLocal(), "srl02.tamu.edu",
                    PORT, this.isSecure(), SubmissionClientWebSocket.class);
        } catch (ConnectionException e) {
            final Message.ProtoException p1 = ExceptionUtilities.createProtoException(e);
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
