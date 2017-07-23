package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.database.AnswerCheckerDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.grading.AutoGrader;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.services.submission.SubmissionWebSocketClient;
import coursesketch.utilities.ExceptionUtilities;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Submission;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.utils.Util;
import utilities.Encoder;
import utilities.LoggingConstants;
import utilities.TimeManager;

import static coursesketch.utilities.ExceptionUtilities.createAndSendException;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
    AnswerCheckerServerWebSocketHandler(final ServerWebSocketInitializer parent) {
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
        try {
            if (req.getRequestType() == Request.MessageType.SUBMISSION) {
                // then we submit!
                if (req.getResponseText().equals("student")) {

                    handleStudentSubmission(conn, req);

                } else if (req.getResponseText().equals("instructor")) {
                    handleInstructorSubmission(conn, req);
                }
            }
        } catch (AuthenticationException | DatabaseAccessException e) {
            LOG.error("Exception handling problem", e);
            createAndSendException(conn, req, e);
        }
    }

    /**
     * Checks the student solution and automatically grades it.
     *
     * @param conn
     *            The session object that created the message.
     * @param req
     *            The message itself.
     * @throws AuthenticationException Thrown if the user can not be authenticated.
     * @throws DatabaseAccessException Thrown if there is a problem getting submission data.
     */
    private void handleStudentSubmission(final SocketSession conn, final Request req) throws AuthenticationException, DatabaseAccessException {
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

        if (!studentExperiment.hasSolutionId() || isEmpty(studentExperiment.getSolutionId())) {
            LOG.info("Experiment user: {} problem: {} does not have an associated solution", studentExperiment.getUserId(),
                    studentExperiment.getProblemId());
            // No need to try and automatically grade something that does not exist.
            return;
        }

        final String authId = ((AnswerCheckerDatabase) getDatabaseReader()).getKey(req.getServersideId(), studentExperiment);

        final Submission.SrlSolution solution =
                submissionInterface.getSolution(authId, null, studentExperiment.getProblemBankId(), studentExperiment.getSolutionId());

        final Feedback.SubmissionFeedback submissionFeedback =
                new AutoGrader(((AnswerCheckerDatabase) getDatabaseReader())).gradeProblem(studentExperiment, solution);

        LOG.info("Feedback is: {}", submissionFeedback);
    }

    /**
     * Inserts an instructor submission into the database.
     *
     * @param conn
     *            The session object that created the message.
     * @param req
     *            The message itself.
     * @throws AuthenticationException Thrown if the user can not be authenticated.
     * @throws DatabaseAccessException Thrown if there is a problem getting submission data.
     */
    private void handleInstructorSubmission(final SocketSession conn, final Request req) throws DatabaseAccessException, AuthenticationException {
        Submission.SrlSolution solution;
        try {
            solution = Submission.SrlSolution.parseFrom(req.getOtherData());
        } catch (InvalidProtocolBufferException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            return; // sorry but we are bailing if anything does not look right.
        }

        final AuthenticationWebSocketClient authentication = getConnectionManager().getBestConnection(AuthenticationWebSocketClient.class);

        final String userId = ((AnswerCheckerDatabase) getDatabaseReader()).generateKey(req.getServersideId(), solution);
        if (userId != null) {
            authentication.addUser(req.getServersideId(), userId, solution.getProblemBankId(), Util.ItemType.BANK_PROBLEM,
                    Authentication.AuthResponse.PermissionLevel.TEACHER);
        }
    }

    /**
     * @return {@link AnswerConnectionState} that can be used for holding experiments for checking.
     */
    @Override
    public final MultiConnectionState getUniqueState() {
        return new AnswerConnectionState(Encoder.nextID().toString());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link AnswerCheckerDatabase}.
     */
    @Override
    protected final AbstractCourseSketchDatabaseReader createDatabaseReader(final ServerInfo info) {
        final AuthenticationWebSocketClient authChecker = getConnectionManager()
                .getBestConnection(AuthenticationWebSocketClient.class);

        return new AnswerCheckerDatabase(info, authChecker);
    }
}
