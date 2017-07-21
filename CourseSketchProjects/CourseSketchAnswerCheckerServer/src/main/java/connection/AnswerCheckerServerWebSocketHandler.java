package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.services.submission.SubmissionWebSocketClient;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission;
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
        super(parent, parent.getServerInfo());
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
                handleStudentSubmission(conn, req);
            } else if(req.getResponseText().equals("instructor")) {
                handleInstructorSubmission(conn, req);
            }
        }
    }

    private void handleStudentSubmission(final SocketSession conn, final Request req) {
        final SubmissionManagerInterface submissionInterface = getConnectionManager().getBestConnection(SubmissionWebSocketClient.class);
        SrlExperiment studentExperiment;
        try {
            studentExperiment = SrlExperiment.parseFrom(req.getOtherData());
        } catch (InvalidProtocolBufferException e1) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e1);
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            return; // sorry but we are bailing if anything does not look right.
        }

        // submissionInterface.getSolutionForSubmission()
    }

    private void handleInstructorSubmission(final SocketSession conn, final Request req) {
        final SubmissionManagerInterface submissionInterface = getConnectionManager().getBestConnection(SubmissionWebSocketClient.class);
        Submission.SrlSolution solution;
        try {
            solution = Submission.SrlSolution.parseFrom(req.getOtherData());
        } catch (InvalidProtocolBufferException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            return; // sorry but we are bailing if anything does not look right.
        }
        generateUniqueKey();
        // If a solution is being submitted then we store a key for the answer checker server to grab later
        // This key is unique to the solution and is used as an auth id to authenticate the solution.
        // This key is mapped to the problemId+partId
    }

    private void generateUniqueKey() {
        //
    }
    // do submission stuff





    /**
     * @return {@link AnswerConnectionState} that can be used for holding experiments for checking.
     */
    @Override
    public final MultiConnectionState getUniqueState() {
        return new AnswerConnectionState(Encoder.nextID().toString());
    }
}
