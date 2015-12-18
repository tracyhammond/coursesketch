package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.submission.Submission;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

/**
 * Created by dtracers on 12/17/2015.
 */
public class SubmissionHandler {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataInsertHandler.class);

    /**
     * The string used to separate ids when returning a result.
     */
    private static final String ID_SEPARATOR = " : ";

    /**
     * A message returned when the insert was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * Private constructor.
     */
    private SubmissionHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param instance The database backer.
     */
    public static void handleData(final Message.Request req, final SocketSession conn, final SubmissionManagerInterface submissionManager,
            final Institution instance) {
        if (req.getResponseText().equals("student")) {
            LOG.info("Parsing as an experiment");
            Submission.SrlExperiment student = null;
            try {
                student = Submission.SrlExperiment.parseFrom(req.getOtherData());
            } catch (InvalidProtocolBufferException e1) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e1);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                return; // sorry but we are bailing if anything does not look right.
            }

            String submissionId = null;
            try {
                submissionId = submissionManager.insertExperiment(req.getServersideId(), null, student, req.getMessageTime());
            } catch (AuthenticationException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            } catch (DatabaseAccessException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }

            try {
                // TODO: use the hashedUserId instead of the server-side id when you convert everything to using the identity server
                final String hashedUserId = MongoInstitution.convertUserId(req.getServerUserId(), student.getCourseId());
                instance.insertSubmission(req.getServersideId(), student.getProblemId(), submissionId, true);
            } catch (DatabaseAccessException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            } catch (AuthenticationException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
        } else {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(
                    new DatabaseAccessException("INSTRUCTORS CAN NOT SUBMIT SOLUTIONS"));
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.warn("INSTRUCTORS CAN NOT SUBMIT ANYTHING RIGHT NOW");
        }
    }
}
