package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.services.submission.SubmissionServer;
import protobuf.srl.submission.Submission;
import utilities.ExceptionUtilities;

import java.util.List;

/**
 * Created by gigemjt on 12/14/15.
 */
public class SubmissionService extends SubmissionServer.SubmissionService implements CourseSketchRpcService {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionService.class);

    private final Authenticator authenticator;
    private final SubmissionManagerInterface manager;

    public SubmissionService(final Authenticator authenticator, final SubmissionManagerInterface manager) {
        this.authenticator = authenticator;
        this.manager = manager;
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializer
     *         The {@link ISocketInitializer} that contains useful data for any RpcService used by CourseSketch.
     */
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
    }

    /**
     * <code>rpc getSubmission(.protobuf.srl.services.submission.SubmissionRequest) returns (.protobuf.srl.services.submission.ExperimentResponse);</code>
     *
     * <pre>
     * *
     * Gets the submissions. given the ids.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void getSubmission(final RpcController controller, final SubmissionServer.SubmissionRequest request,
            final RpcCallback<SubmissionServer.ExperimentResponse> done) {

        final List<String> ids = request.getSubmissionIdsList();
            try {
                final List<Submission.SrlExperiment> submission = manager
                        .getSubmission(request.getAuthId(), authenticator, request.getProblemId(), (String[]) ids.toArray());
            } catch (DatabaseAccessException e) {
                LOG.error("Database exception occurred while trying to get experiments", e);
                done.run(SubmissionServer.ExperimentResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
                return;
        } catch (AuthenticationException e) {
                LOG.error("Authentication exception occurred while trying to get experiments", e);
                done.run(SubmissionServer.ExperimentResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
                return;
        }
    }

    /**
     * <code>rpc insertExperiment(.protobuf.srl.services.submission.ExperimentInsert) returns (.protobuf.srl.services.submission.SubmissionResponse);</code>
     *
     * <pre>
     * *
     * Inserts the experiment into the submission server.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void insertExperiment(final RpcController controller, final SubmissionServer.ExperimentInsert request,
            final RpcCallback<SubmissionServer.SubmissionResponse> done) {
        try {
            final String submissionId = manager
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
    }

    /**
     * <code>rpc insertSolution(.protobuf.srl.services.submission.ExperimentInsert) returns (.protobuf.srl.services.submission.SubmissionResponse);</code>
     *
     * <pre>
     * *
     * Inserts the solution into the submission server.
     * </pre>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void insertSolution(final RpcController controller, final SubmissionServer.SolutionInsert request,
            final RpcCallback<SubmissionServer.SubmissionResponse> done) {
        try {
            final String submissionId = manager.insertSolution(request.getRequestData().getAuthId(), authenticator, request.getSubmission());
        } catch (AuthenticationException e) {
            LOG.error("Authentication exception occurred while trying to insert experiment", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        } catch (DatabaseAccessException e) {
            LOG.error("Database exception occurred while trying to insert experiment", e);
            done.run(SubmissionServer.SubmissionResponse.newBuilder().setDefaultResponse(ExceptionUtilities.createExceptionResponse(e)).build());
            return;
        }
    }

}
