package handlers;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.institution.Institution;
import coursesketch.database.institution.mongo.MongoInstitution;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.interfaces.SocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import protobuf.srl.school.Problem;
import protobuf.srl.submission.Submission;

import java.util.Collections;

import static coursesketch.utilities.ExceptionUtilities.createAndSendException;

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
            saveExperiment(req, conn, submissionManager, instance);
        } else {
            saveSolution(req, conn, submissionManager, instance);
        }
    }

    /**
     * Takes in a request that has to deal with inserting an experiment.
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param submissionManager
     *         The manager for submission data on other servers.
     * @param instance The database backer.
     */
    private static void saveExperiment(final Message.Request req, final SocketSession conn, final SubmissionManagerInterface submissionManager,
            final Institution instance) {
        LOG.info("Parsing as an experiment");
        Submission.SrlExperiment experiment;
        try {
            experiment = Submission.SrlExperiment.parseFrom(req.getOtherData());
        } catch (InvalidProtocolBufferException exception) {
            createAndSendException(conn, req, exception);
            return; // sorry but we are bailing if anything does not look right.
        }

        final Submission.SrlExperiment experimentWithIds = Submission.SrlExperiment.newBuilder(experiment)
                .setUserId(req.getServersideId())
                .build();

        String submissionId;
        try {
            submissionId = submissionManager.insertExperiment(req.getServersideId(), null, experimentWithIds, req.getMessageTime());
        } catch (AuthenticationException | DatabaseAccessException exception) {
            createAndSendException(conn, req, exception);
            // bail early
            return;
        }

        if (Strings.isNullOrEmpty(submissionId)) {
            final Exception exception = new DatabaseAccessException("Unable to store submission in the database!");
            createAndSendException(conn, req, exception);
            // bail early
            return;
        }

        try {
            final String hashedUserId = MongoInstitution.hashUserId(req.getServerUserId(), experiment.getCourseId());
            LOG.debug("Hashed user id: {}", hashedUserId);
            instance.insertSubmission(hashedUserId, req.getServersideId(), experiment.getProblemId(), experiment.getPartId(), submissionId,
                    true);
        } catch (AuthenticationException | DatabaseAccessException exception) {
            createAndSendException(conn, req, exception);
            // bail early
            return;
        }

        final Message.Request result = ResultBuilder.buildRequest(
                Collections.singletonList(ResultBuilder.buildResult(Data.ItemQuery.NO_OP, "SUCCESSFULLY STORED EXPERIMENT")),
                "SUCCESSFULLY STORED EXPERIMENT", req);
        conn.send(result);
    }


    /**
     * Takes in a request that has to deal with inserting an experiment.
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param submissionManager
     *         The manager for submission data on other servers.
     * @param instance The database backer.
     */
    private static void saveSolution(final Message.Request req, final SocketSession conn, final SubmissionManagerInterface submissionManager,
            final Institution instance) {
        LOG.info("Parsing as an experiment");
        Submission.SrlSolution solution;
        try {
            solution = Submission.SrlSolution.parseFrom(req.getOtherData());
        } catch (InvalidProtocolBufferException exception) {
            createAndSendException(conn, req, exception);
            return; // sorry but we are bailing if anything does not look right.
        }


        String submissionId;
        try {
            submissionId = submissionManager.insertSolution(req.getServersideId(), null, solution);
        } catch (AuthenticationException | DatabaseAccessException exception) {
            createAndSendException(conn, req, exception);
            // bail early
            return;
        }

        if (!solution.getSubmission().getId().equals(submissionId)) {
            // Update the submission id for the bank problem
            final Problem.SrlBankProblem bankProblem =
                    Problem.SrlBankProblem.newBuilder().setSolutionId(submissionId).setId(solution.getProblemBankId()).build();
            try {
                instance.updateBankProblem(req.getServersideId(), bankProblem);
            } catch (AuthenticationException | DatabaseAccessException exception) {
                createAndSendException(conn, req, exception);
            }
        }

        if (Strings.isNullOrEmpty(submissionId)) {
            final Exception exception = new DatabaseAccessException("Unable to store submission in the database!");
            createAndSendException(conn, req, exception);
            // bail early
            return;
        }

        final Message.Request result = ResultBuilder.buildRequest(
                Collections.singletonList(ResultBuilder.buildResult(Data.ItemQuery.NO_OP, "SUCCESSFULLY STORED SOLUTION")),
                "SUCCESSFULLY STORED SOLUTION", req);
        conn.send(result);
    }
}
