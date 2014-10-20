package connection;

import coursesketch.jetty.multiconnection.ServerWebSocketHandler;
import coursesketch.jetty.multiconnection.ServerWebSocketInitializer;
import internalConnection.AnswerConnectionState;
import internalConnection.SubmissionClientConnection;
import interfaces.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;

import com.google.protobuf.InvalidProtocolBufferException;
import utilities.ConnectionException;
import utilities.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket()
public class AnswerCheckerServerWebSocketHandler extends ServerWebSocketHandler {

    public AnswerCheckerServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
    }

    @Override
    public final void onMessage(final Session conn, final Request req) {

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
                final MultiConnectionState state = connectionToId.get(conn);
                try {
                    SrlUpdate.parseFrom(req.getOtherData());
                    System.out.println("Parsing as an update");
                    try {
                        getConnectionManager().send(req,
                                req.getSessionInfo() + "+" + state.getKey(),
                                SubmissionClientConnection.class);
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                    return;
                } catch (InvalidProtocolBufferException e) {
                    System.out.println("Parsing as an experiment");
                    SrlExperiment student = null;
                    try {
                        student = SrlExperiment.parseFrom(req.getOtherData());
                    } catch (InvalidProtocolBufferException e1) {
                        e1.printStackTrace();
                    }
                    ((AnswerConnectionState) state).addPendingExperiment(
                            req.getSessionInfo(), student);
                    System.out.println("Student exp " + student);
                    try {
                        getConnectionManager().send(req,
                                req.getSessionInfo() + "+" + state.getKey(),
                                SubmissionClientConnection.class);
                    } catch (ConnectionException e1) {
                        e1.printStackTrace();
                    } // pass submission on

                    // request the solution for checking NOSHIP: need to
                    // actually retrieve answer.
                    final Request.Builder builder = Request.newBuilder();
                    builder.setRequestType(MessageType.DATA_REQUEST);
                    builder.setSessionInfo(req.getSessionInfo() + "+"
                            + state.getKey());
                    final ItemRequest.Builder itemRequest = ItemRequest.newBuilder();
                    itemRequest.setQuery(ItemQuery.SOLUTION);
                    itemRequest.addItemId(student.getProblemId()); // FIXME:
                                                                   // this needs
                                                                   // to change
                                                                   // probably
                                                                   // to make
                                                                   // this work
                    // internalconnections.send(builder.setOtherData(itemRequest.build().toByteString()).build(),
                    // state.getKey(), SubmissionConnection.class);
                }
            } else {
                try {
                    getConnectionManager().send(req, req.getSessionInfo(),
                            SubmissionClientConnection.class);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns a number that should be unique.
     */
    @Override
    public final MultiConnectionState getUniqueState() {
        return new AnswerConnectionState(Encoder.nextID().toString());
    }
}
