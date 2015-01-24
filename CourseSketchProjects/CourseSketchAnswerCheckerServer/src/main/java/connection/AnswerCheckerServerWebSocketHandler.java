package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import utilities.ConnectionException;
import utilities.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket()
public class AnswerCheckerServerWebSocketHandler extends ServerWebSocketHandler {

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
                System.out.println("Parsing as an experiment");
                SrlExperiment student = null;
                try {
                    student = SrlExperiment.parseFrom(req.getOtherData());
                } catch (InvalidProtocolBufferException e1) {
                    conn.send(createExceptionRequest(e1, req));
                    e1.printStackTrace();
                    return; // sorry but we are bailing if anything does not look right.
                }

                ((AnswerConnectionState) state).addPendingExperiment(
                        req.getSessionInfo(), student);
                System.out.println("Student exp " + student);
                try {
                    getConnectionManager().send(req,
                            req.getSessionInfo() + "+" + state.getKey(),
                            SubmissionClientWebSocket.class);
                } catch (ConnectionException e1) {
                    conn.send(createExceptionRequest(e1, req));
                    e1.printStackTrace();
                } // pass submission on

                // request the solution for checking FUTURE: need to
                // actually retrieve answer.
                final Request.Builder builder = Request.newBuilder();
                builder.setRequestType(MessageType.DATA_REQUEST);
                builder.setSessionInfo(req.getSessionInfo() + "+"
                        + state.getKey());
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
                    conn.send(createExceptionRequest(e, req));
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a request that represents the exception that was caused.
     *
     * @param exception
     *         the exception to be sent back to the client.
     * @param inputRequest
     *         The request that was sent to this server.
     * @return A request that warps around the exception.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Request createExceptionRequest(final Exception exception, final Request inputRequest) {
        final Request.Builder builder = Request.newBuilder(inputRequest);
        builder.setRequestType(MessageType.ERROR);
        builder.clearOtherData();
        builder.clearMessageTime();
        builder.setResponseText(exception.getMessage());
        return builder.build();
    }

    /**
     * @return {@link AnswerConnectionState} that can be used for holding experiments for checking.
     */
    @Override
    public final MultiConnectionState getUniqueState() {
        return new AnswerConnectionState(Encoder.nextID().toString());
    }
}
