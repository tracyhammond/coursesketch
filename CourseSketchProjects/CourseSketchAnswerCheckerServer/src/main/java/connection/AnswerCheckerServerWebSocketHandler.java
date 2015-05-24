package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import utilities.ConnectionException;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;
import utilities.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket()
public class AnswerCheckerServerWebSocketHandler extends ServerWebSocketHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnswerCheckerServerWebSocketHandler.class);

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent
     *         The parent servlet of this server.
     */
    public AnswerCheckerServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onMessage(final SocketSession conn, final Request req) {
        if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                send(conn, rsp);
            }
            return;
        }
        if (req.getRequestType() == Request.MessageType.SUBMISSION) {
            // then we submit!
            if (req.getResponseText().equals("student")) {
                final MultiConnectionState state = getConnectionToId().get(conn);
                LOG.info("Parsing as an experiment");
                SrlExperiment student = null;
                try {
                    student = SrlExperiment.parseFrom(req.getOtherData());
                } catch (InvalidProtocolBufferException e1) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e1);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                    return; // sorry but we are bailing if anything does not look right.
                }

                ((AnswerConnectionState) state).addPendingExperiment(
                        req.getSessionInfo(), student);
                LOG.info("Student experiment {}", student);
                try {
                    getConnectionManager().send(req,
                            req.getSessionInfo() + "+" + state.getKey(),
                            SubmissionClientWebSocket.class);
                } catch (ConnectionException e1) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e1);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                } // pass submission on

                // request the solution for checking FUTURE: need to
                // actually retrieve answer.
                final Request.Builder builder = ProtobufUtilities.createRequestFromData(MessageType.DATA_REQUEST, null,
                        req.getSessionInfo() + "+" + state.getKey());

                final ItemRequest.Builder itemRequest = ItemRequest.newBuilder();
                itemRequest.setQuery(ItemQuery.SOLUTION);
                itemRequest.addItemId(student.getProblemId());
                // FIXME this needs to change probably to make this work
                // internalconnections.send(builder.setOtherData(itemRequest.build().toByteString()).build(),
                // state.getKey(), SubmissionConnection.class);
            } else {
                try {
                    getConnectionManager().send(req, req.getSessionInfo(),
                            SubmissionClientWebSocket.class);
                } catch (ConnectionException e) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                }
            }
        }
    }
    /**
     * @return {@link AnswerConnectionState} that can be used for holding experiments for checking.
     */
    @Override
    public final MultiConnectionState getUniqueState() {
        return new AnswerConnectionState(Encoder.nextID().toString());
    }
}
