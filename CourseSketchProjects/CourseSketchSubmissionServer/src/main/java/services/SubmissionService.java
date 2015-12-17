package services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import protobuf.srl.request.Message;
import protobuf.srl.services.submission.SubmissionServer;

/**
 * Created by gigemjt on 12/14/15.
 */
public class SubmissionService extends SubmissionServer.SubmissionService implements CourseSketchRpcService {
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
    @Override public void insertSolution(final RpcController controller, final SubmissionServer.ExperimentInsert request,
            final RpcCallback<SubmissionServer.SubmissionResponse> done) {
    }
}
