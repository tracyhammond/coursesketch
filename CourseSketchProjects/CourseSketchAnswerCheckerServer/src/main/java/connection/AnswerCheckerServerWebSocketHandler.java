package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.services.submission.SubmissionWebSocketClient;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.AnswerCheckerDatabase;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.submission.Submission;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.utils.Util;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.TimeManager;

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
            createAndSendException(conn, req, e);
        }
    }

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
            // No need to try and automatically grade something that does not exist.
            return;
        }

        AuthenticationChecker authChecker = getConnectionManager().getBestConnection(AuthenticationWebSocketClient.class);

        String authId = ((AnswerCheckerDatabase) getDatabaseReader()).getKey(req.getServersideId(), studentExperiment);

        Submission.SrlSolution solution =
                submissionInterface.getSolution(authId, null, studentExperiment.getProblemBankId(), studentExperiment.getSolutionId());



        // submissionInterface.getSolutionForSubmission()
    }

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

        AuthenticationWebSocketClient authentication = getConnectionManager().getBestConnection(AuthenticationWebSocketClient.class);

        String userId = ((AnswerCheckerDatabase) getDatabaseReader()).generateKey(req.getServersideId(), solution);
        if (userId != null) {
            authentication.addUser(req.getServersideId(), userId, solution.getProblemBankId(), Util.ItemType.BANK_PROBLEM);
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
     * Creates and sends an exception.
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param exception
     *         The exception that occurred.
     */
    private static void createAndSendException(final SocketSession conn, final Message.Request req, final Exception exception) {
        final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(exception);
        conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, exception);
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
        final Authenticator auth = new Authenticator(authChecker, createAuthChecker());

        return new AnswerCheckerDatabase(info, auth);
    }

    private AuthenticationOptionChecker createAuthChecker() {
        return new AuthenticationOptionChecker() {
            @Override
            public boolean authenticateDate(AuthenticationDataCreator dataCreator, long checkTime) throws DatabaseAccessException {
                return true;
            }

            @Override
            public boolean isItemRegistrationRequired(AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                return false;
            }

            @Override
            public boolean isItemPublished(AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                return true;
            }

            @Override
            public AuthenticationDataCreator createDataGrabber(Util.ItemType collectionType, String itemId) throws DatabaseAccessException {
                return null;
            }
        };
    }

}
