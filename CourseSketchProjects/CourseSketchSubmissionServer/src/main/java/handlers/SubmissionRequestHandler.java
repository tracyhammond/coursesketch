package handlers;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import connection.DataClientWebSocket;
import coursesketch.server.interfaces.MultiConnectionManager;
import database.DatabaseAccessException;
import database.DatabaseClient;
import database.SubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.submission.Submission;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;
import utilities.ConnectionException;

/**
 * Handles the request of a submission.
 */
public final class SubmissionRequestHandler {

    /**
     * Declaration and Definition of Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseClient.class);

    /**
     * Private constructor.
     */
    private SubmissionRequestHandler() {
    }

    /**
     * Stores the submission in the database and sends communication to other servers if they need to know about the submission.
     *
     * @param req
     *         contains the data about the submission itself.
     * @param internalConnections
     *         Connections to other servers.
     * @return A Request that is sent back to be sent off to the client.
     */
    public static Request handleRequest(final Request req, final MultiConnectionManager internalConnections) {
        final String sessionInfo = req.getSessionInfo();
        try {
            final ByteString result = handleSubmission(req);
            final Request.Builder build = Request.newBuilder(req);
            build.setResponseText("Submission Succesful!");
            build.clearOtherData();
            LOG.info("Session Info: {}", sessionInfo);
            if (result != null) {
                // passes the data to the database for connecting
                final Data.DataSend send = Data.DataSend.newBuilder().addItems(Data.ItemSend.newBuilder().setData(result).
                        setQuery(Data.ItemQuery.EXPERIMENT)).build();
                final Request.Builder databaseRequest = Request.newBuilder(req);
                databaseRequest.setRequestType(Request.MessageType.DATA_INSERT);
                databaseRequest.setOtherData(send.toByteString());
                LOG.info("Sending experiment data to database server");
                internalConnections.send(databaseRequest.build(), "", DataClientWebSocket.class);
            }
            // sends the response back to the answer checker which can then send it back to the client.
            return build.build();
        } catch (SubmissionException e) {
            final Request.Builder build = Request.newBuilder();
            build.setRequestType(Request.MessageType.ERROR);
            if (e.getMessage() != null) {
                build.setResponseText(e.getMessage());
            }
            build.setSessionInfo(sessionInfo);
            LOG.info("Exception: {}", e);
            return build.build();
        } catch (ConnectionException e) {
            final Request.Builder build = Request.newBuilder();
            build.setRequestType(Request.MessageType.ERROR);
            if (e.getMessage() != null) {
                build.setResponseText(e.getMessage());
            }
            build.setSessionInfo(sessionInfo);
            LOG.info("Exception: {}", e);
        }
        return null;
    }

    /**
     * Saves the submission into the database.
     *
     * @param req
     *         The request of the submission.
     * @return {@link com.google.protobuf.ByteString} that represents the result if this is the first submission.
     * May be null if this is not the first submission.
     * @throws SubmissionException
     *         thrown if there is an error submitting.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static ByteString handleSubmission(final Message.Request req) throws SubmissionException {
        String resultantId = null;
        ByteString data = null;
        if ("student".equals(req.getResponseText()) || "grader".equals(req.getResponseText())) {
            Submission.SrlExperiment experiment = null;
            try {
                experiment = Submission.SrlExperiment.parseFrom(req.getOtherData());
                experiment = SrlExperiment.newBuilder(experiment).setUserId(req.getServersideId()).build();
            } catch (InvalidProtocolBufferException e) {
                throw new SubmissionException("submission was labeled as student but was not experiment", e);
            }
            try {
                resultantId = DatabaseClient.saveExperiment(DatabaseClient.getInstance(), experiment, req.getMessageTime());
                if (resultantId != null) {
                    final SrlExperiment.Builder builder = SrlExperiment.newBuilder(experiment);
                    // erase the actual data from the submission, leaving only the id.
                    builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
                    data = builder.build().toByteString();
                }
            } catch (DatabaseAccessException e) {
                throw new SubmissionException("an exception occurred while saving the experiment", e);
            }
        } else {
            Submission.SrlSolution solution = null;
            try {
                solution = Submission.SrlSolution.parseFrom(req.getOtherData());
            } catch (InvalidProtocolBufferException e) {
                throw new SubmissionException("submission was not labeled as student but was not solution", e);
            }
            try {
                resultantId = DatabaseClient.saveSolution(solution, DatabaseClient.getInstance());
                if (resultantId != null) {
                    final SrlSolution.Builder builder = SrlSolution.newBuilder(solution);
                    // erase the actual data from the submission, leaving only the id.
                    builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
                    data = builder.build().toByteString();
                }
            } catch (DatabaseAccessException e) {
                throw new SubmissionException("an exception occured while saving the solution", e);
            }
        }
        return data;
    }
}
