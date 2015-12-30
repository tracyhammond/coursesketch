package handlers;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import protobuf.srl.submission.Submission;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

import java.util.Arrays;

/**
 * Handles submission storage requests.
 *
 * All request to save submissions is sent to this class.
 * Created by dtracers on 12/17/2015.
 */
public final class SubmissionHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataInsertHandler.class);

    /**
     * Private constructor.
     */
    private SubmissionHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link Institution} (courses, assignments, ...) then repackage everything and send it out.
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param submissionManager
     *         The manager for submission data on other servers.
     * @param instance The database backer.
     */
    public static void handleData(final Message.Request req, final SocketSession conn, final SubmissionManagerInterface submissionManager,
            final Institution instance) {
        if (req.getResponseText().equals("student")) {
            LOG.info("Parsing as an experiment");
            Submission.SrlExperiment experiment = null;
            try {
                experiment = Submission.SrlExperiment.parseFrom(req.getOtherData());
            } catch (InvalidProtocolBufferException e1) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e1);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                return; // sorry but we are bailing if anything does not look right.
            }

            final Submission.SrlExperiment experimentWithIds = Submission.SrlExperiment.newBuilder(experiment)
                    .setUserId(req.getServersideId())
                    .build();

            String submissionId;
            try {
                submissionId = submissionManager.insertExperiment(req.getServersideId(), null, experimentWithIds, req.getMessageTime());
            } catch (AuthenticationException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                // bail early
                return;
            } catch (DatabaseAccessException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                // bail early
                return;
            }

            if (Strings.isNullOrEmpty(submissionId)) {
                final Exception exception = new DatabaseAccessException("Unable to store submission in the database!");
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(exception);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, exception);
                // bail early
                return;
            }

            try {
                final String hashedUserId = MongoInstitution.hashUserId(req.getServerUserId(), experiment.getCourseId());
                LOG.debug("Hashed user id: {}", hashedUserId);
                instance.insertSubmission(hashedUserId, req.getServersideId(), experiment.getProblemId(), submissionId, true);
            } catch (DatabaseAccessException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                // bail early
                return;
            } catch (AuthenticationException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                // bail early
                return;
            }

            final Message.Request result = ResultBuilder.buildRequest(
                    Arrays.asList(ResultBuilder.buildResult(Data.ItemQuery.NO_OP, "SUCCESSFULLY STORED SUBMISSION")),
                    "SUCCESSFULLY STORED SUBMISSION",
                    req
            );
            conn.send(result);
        } else {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(
                    new DatabaseAccessException("INSTRUCTORS CAN NOT SUBMIT SOLUTIONS"));
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.warn("INSTRUCTORS CAN NOT SUBMIT ANYTHING RIGHT NOW");
        }
    }
}
