package connection;

import java.net.URI;
import java.nio.ByteBuffer;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;

import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ExperimentReview;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public class SubmissionClientWebSocket extends ClientWebSocket {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionClientWebSocket.class);

    /**
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parentServer
     *            The server that is using this connection wrapper.
     */
    public SubmissionClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parentServer) {
        super(destination, parentServer);
    }

    /**
     * Splits the session info to find the correct level above to pass it up the
     * chain to the correct client.
     *
     * @param buffer Contains the sketch itself that is being passed.
     */
    @Override
    public final void onMessage(final ByteBuffer buffer) {
        final Request req = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
        LOG.info("Got a response from the submission server!");
        LOG.info(req.getSessionInfo());

        final String[] sessionInfo = req.getSessionInfo().split("\\+");

        LOG.info(sessionInfo[1]);

        final MultiConnectionState state = getStateFromId(sessionInfo[1]);

        LOG.info("State {}", state);

        if (req.getRequestType() == MessageType.DATA_REQUEST) {
            final DataResult.Builder result2 = DataResult.newBuilder();
            // pass up the Id to the client
            try {
                final DataResult result = DataResult.parseFrom(req.getOtherData());
                result2.clearResults();
                for (ItemResult item : result.getResultsList()) {
                    if (item.hasAdvanceQuery() && item.getQuery() == ItemQuery.EXPERIMENT) {
                        // we might have to do a lot of work here!
                        final ExperimentReview rev = ExperimentReview.parseFrom(item.getAdvanceQuery());
                        if (rev.getShowUserNames()) {
                            LOG.info("Attempting to change out usernames!");
                            result2.addResults(item);
                        } else {
                            result2.addResults(item);
                        }
                    } else {
                        result2.addResults(item);
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
            final Request.Builder builder = Request.newBuilder(req);
            builder.setSessionInfo(sessionInfo[0]);
            final SocketSession connection = getConnectionFromState(state);
            builder.setOtherData(result2.build().toByteString());
            if (connection == null) {
                LOG.error("SOCKET IS NULL");
            }
            this.getParentServer().send(getConnectionFromState(state), builder.build());
        }
    }
}
