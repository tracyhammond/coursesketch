package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.SubmissionDatabaseClient;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.database.util.DatabaseAccessException;
import handlers.SubmissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.submission.SubmissionServer;
import protobuf.srl.submission.Submission;
import coursesketch.utilities.ExceptionUtilities;

import java.util.List;

/**
 * Created by gigemjt on 12/14/15.
 */
public final class SubmissionService extends SubmissionServer.SubmissionService implements CourseSketchRpcService {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionService.class);

    /**
     * Used to authenticate any permissions.
     */
    private final Authenticator authenticator;

    /**
     * Connects to the database to store and retrieve submissions.
     */
    private SubmissionManagerInterface submissionDatabaseInterface;

    /**
     * A constructor for the submission service that takes in an authenticator and a submission manager interface.
     * @param authenticator Authenticates users.
     */
    public SubmissionService(final Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * {@inheritDoc}
     * <code>rpc getSubmission(.protobuf.srl.services.submission.SubmissionRequest) returns
     * (.protobuf.srl.services.submission.ExperimentResponse);</code>
     *
     * <pre>
     * Gets the submissions. given the ids.
     * </pre>
     *
     */
    @Override public void getSubmission(final RpcController controller, final SubmissionServer.SubmissionRequest request,
            final RpcCallback<SubmissionServer.ExperimentResponse> done) {

        final List<String> ids = request.getSubmissionIdsList();
        List<Submission.SrlExperiment> srlExperimentList;
        try {
            srlExperimentList = submissionDatabaseInterface
                    .getSubmission(request.getAuthId(), authenticator, request.getProblemId(), ids.toArray(new String[ids.size()]));
        } catch (DatabaseAccessException e) {
            LOG.error("Database exception occurred while trying to get experiments", e);
            done.run(SubmissionServer.ExperimentResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        } catch (AuthenticationException e) {
            LOG.error("Authentication exception occurred while trying to get experiments", e);
            done.run(SubmissionServer.ExperimentResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        }
        done.run(SubmissionServer.ExperimentResponse.newBuilder().addAllExperiments(srlExperimentList).build());
    }

    /**
     * {@inheritDoc}
     * <code>rpc getSubmission(.protobuf.srl.services.submission.SubmissionRequest) returns
     * (.protobuf.srl.services.submission.SolutionResponse);</code>
     *
     * <pre>
     * Gets the solution. given the id.
     * </pre>
     *
     */
    @Override
    public void getSolution(final RpcController controller, final SubmissionServer.SubmissionRequest request,
            final RpcCallback<SubmissionServer.SolutionResponse> done) {
        final String solutionId = request.getSubmissionIds(0);
        Submission.SrlSolution srlSolution;
        try {
            srlSolution = submissionDatabaseInterface
                    .getSolution(request.getAuthId(), authenticator, request.getProblemId(), solutionId);
        } catch (DatabaseAccessException e) {
            LOG.error("Database exception occurred while trying to get experiments", e);
            done.run(SubmissionServer.SolutionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        } catch (AuthenticationException e) {
            LOG.error("Authentication exception occurred while trying to get experiments", e);
            done.run(SubmissionServer.SolutionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        }
        done.run(SubmissionServer.SolutionResponse.newBuilder().setSolution(srlSolution).build());
    }

    /**
     * {@inheritDoc}
     * <code>rpc insertExperiment(.protobuf.srl.services.submission.ExperimentInsert) returns
     * (.protobuf.srl.services.submission.SubmissionResponse);</code>
     *
     * <pre>
     * *
     * Inserts the experiment into the submission server.
     * </pre>
     */
    @Override public void insertExperiment(final RpcController controller, final SubmissionServer.ExperimentInsert request,
            final RpcCallback<SubmissionServer.SubmissionResponse> done) {
        String submissionId;
        try {
            submissionId = submissionDatabaseInterface
                    .insertExperiment(request.getRequestData().getAuthId(), authenticator, request.getSubmission(), request.getSubmissionTime());
        } catch (AuthenticationException e) {
            LOG.error("Authentication exception occurred while trying to insert experiment", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Database exception occurred while trying to insert experiment", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        }
        done.run(SubmissionServer.SubmissionResponse.newBuilder().setSubmissionId(submissionId).build());
    }

    /**
     * {@inheritDoc}
     * <code>rpc insertSolution(.protobuf.srl.services.submission.ExperimentInsert) returns
     * (.protobuf.srl.services.submission.SubmissionResponse);</code>
     *
     * <pre>
     * *
     * Inserts the solution into the submission server.
     * </pre>
     */
    @Override public void insertSolution(final RpcController controller, final SubmissionServer.SolutionInsert request,
            final RpcCallback<SubmissionServer.SubmissionResponse> done) {
        String submissionId;
        try {
            submissionId = submissionDatabaseInterface.insertSolution(request.getRequestData().getAuthId(), authenticator, request.getSubmission());
        } catch (AuthenticationException e) {
            LOG.error("Authentication exception occurred while trying to insert solution", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Database exception occurred while trying to insert solution", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        }
        done.run(SubmissionServer.SubmissionResponse.newBuilder().setSubmissionId(submissionId).build());
    }

    @Override
    public AbstractCourseSketchDatabaseReader createDatabaseReader(ServerInfo serverInfo) {
        return new SubmissionDatabaseClient(serverInfo);
    }

    @Override
    public void setDatabaseReader(AbstractCourseSketchDatabaseReader databaseReader) {
        this.submissionDatabaseInterface = new SubmissionManager((SubmissionDatabaseClient) databaseReader);
    }

    @Override
    public void onInitialize() {

    }
}
