package coursesketch.services.submission;

import com.google.protobuf.ServiceException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.submission.SubmissionServer;
import protobuf.srl.submission.Submission;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dtracers on 12/15/2015.
 */
public final class SubmissionWebSocketClient extends ClientWebSocket implements SubmissionManagerInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionWebSocketClient.class);

    /**
     * The default address for the Submission server.
     */
    public static final String ADDRESS = "SUBMISSION_IP_PROP";

    /**
     * The default port of the Submission Server.
     */
    public static final int PORT = 8892;

    /**
     * The blocker service that is used to communicate.
     */
    private SubmissionServer.SubmissionService.BlockingInterface submissionService;


    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer The server that created the websocket.
     */
    public SubmissionWebSocketClient(final URI iDestination,
            final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<Submission.SrlExperiment> getSubmission(final String authId, final Authenticator authenticator,
            final String problemId, final String... submissionIds) throws DatabaseAccessException {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }

        final SubmissionServer.SubmissionRequest request = SubmissionServer.SubmissionRequest.newBuilder()
                .setAuthId(authId)
                .setProblemId(problemId)
                .addAllSubmissionIds(Arrays.asList(submissionIds))
                .build();

        SubmissionServer.ExperimentResponse response = null;
        try {
            LOG.debug("Sending submission request");
            response = submissionService.getSubmission(getNewRpcController(), request);
            LOG.debug("Submission response: {}", response);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                final DatabaseAccessException authExcep =
                        new DatabaseAccessException("Exception with submission server");
                authExcep.setProtoException(response.getDefaultResponse().getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException("Exception inserting submission into submission server", e);
        }
        return response.getExperimentsList();
    }

    /**
     * {@inheritDoc}
     */
    @Override public String insertExperiment(final String authId, final Authenticator authenticator, final Submission.SrlExperiment submission,
            final long submissionTime) throws AuthenticationException, DatabaseAccessException {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }

        LOG.debug("Creating submission request");

        final SubmissionServer.ExperimentInsert request = SubmissionServer.ExperimentInsert.newBuilder()
                .setRequestData(SubmissionServer.SubmissionRequest.newBuilder()
                        .setAuthId(authId)
                        .setProblemId(submission.getProblemId())
                        .build())
                .setSubmission(submission)
                .setSubmissionTime(submissionTime)
                .build();

        SubmissionServer.SubmissionResponse response = null;
        try {
            LOG.debug("Sending submission request");
            response = submissionService.insertExperiment(getNewRpcController(), request);
            LOG.debug("Submission response {}", response);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                final DatabaseAccessException databaseAccessException =
                        new DatabaseAccessException("Exception with submission server");
                databaseAccessException.setProtoException(response.getDefaultResponse().getException());
                throw databaseAccessException;
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException("Exception inserting submission into submission server", e);
        }
        return response.getSubmissionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override public String insertSolution(final String authId, final Authenticator authenticator, final Submission.SrlSolution submission)
            throws AuthenticationException, DatabaseAccessException {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }

        LOG.debug("Creating submission request");

        final SubmissionServer.SolutionInsert request = SubmissionServer.SolutionInsert.newBuilder()
                .setRequestData(SubmissionServer.SubmissionRequest.newBuilder()
                        .setAuthId(authId)
                        .setProblemId(submission.getProblemBankId())
                        .build())
                .setSubmission(submission)
                .build();

        SubmissionServer.SubmissionResponse response = null;
        try {
            LOG.debug("Sending submission request");
            response = submissionService.insertSolution(getNewRpcController(), request);
            LOG.debug("Submission response {}", response);
            if (response.hasDefaultResponse() && response.getDefaultResponse().hasException()) {
                final DatabaseAccessException authExcep =
                        new DatabaseAccessException("Exception with submission server");
                authExcep.setProtoException(response.getDefaultResponse().getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException("Exception inserting submission into submission server", e);
        }
        return response.getSubmissionId();
    }

}
