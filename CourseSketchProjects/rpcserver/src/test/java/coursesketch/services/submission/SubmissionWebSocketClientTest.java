package coursesketch.services.submission;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.services.submission.SubmissionServer;
import protobuf.srl.submission.Submission;

import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by dtracers on 12/30/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SubmissionServer.SubmissionService.class, SubmissionWebSocketClient.class })
public class SubmissionWebSocketClientTest {

    private static final String VALID_SUBMISSION_ID = new ObjectId().toHexString();
    private static final String VALID_BANK_PROBLEM_ID = new ObjectId().toHexString();

    private static final String VALID_SUBMISSION_ID_1 = new ObjectId().toHexString();
    private static final String VALID_SUBMISSION_ID_2 = new ObjectId().toHexString();

    private static final String TEACHER_AUTH_ID = new ObjectId().toHexString();
    private static final long SUBMISSION_TIME = 1556;


    @Mock
    private SubmissionServer.SubmissionService.BlockingInterface mockSubmissionService;
    @Mock
    private AbstractServerWebSocketHandler mockHandler;

    private SubmissionWebSocketClient webclient;

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(SubmissionServer.SubmissionService.class);

        when(SubmissionServer.SubmissionService.newBlockingStub(any(BlockingRpcChannel.class))).thenReturn(mockSubmissionService);

        URI mockUri = new URI("http://localhost");
        webclient = new SubmissionWebSocketClient(mockUri, mockHandler);

        webclient = PowerMockito.spy(webclient);

        PowerMockito.doReturn(mock(RpcClientChannel.class)).when(webclient).getRpcChannel();

        when(mockSubmissionService.getSubmission(any(RpcController.class), any(SubmissionServer.SubmissionRequest.class)))
                .thenReturn(SubmissionServer.ExperimentResponse.getDefaultInstance());

        when(mockSubmissionService.getSolution(any(RpcController.class), any(SubmissionServer.SubmissionRequest.class)))
                .thenReturn(SubmissionServer.SolutionResponse.getDefaultInstance());

        when(mockSubmissionService.insertExperiment(any(RpcController.class), any(SubmissionServer.ExperimentInsert.class)))
                .thenReturn(SubmissionServer.SubmissionResponse.getDefaultInstance());
        when(mockSubmissionService.insertSolution(any(RpcController.class), any(SubmissionServer.SolutionInsert.class)))
                .thenReturn(SubmissionServer.SubmissionResponse.getDefaultInstance());
    }

    @Test
    public void callsGetSolutionServiceCorrectlyWithNullAuthenticator() throws Exception {
        webclient.getSolution(TEACHER_AUTH_ID, null, VALID_BANK_PROBLEM_ID, VALID_SUBMISSION_ID);

        final SubmissionServer.SubmissionRequest request = SubmissionServer.SubmissionRequest.newBuilder()
                .setAuthId(TEACHER_AUTH_ID)
                .setProblemId(VALID_BANK_PROBLEM_ID)
                .addSubmissionIds(VALID_SUBMISSION_ID)
                .build();

        Mockito.verify(mockSubmissionService).getSolution(any(RpcController.class), eq(request));
    }

    @Test
    public void callsGetSubmissionCorrectly() throws Exception {

        webclient.getSubmission(TEACHER_AUTH_ID, null, VALID_BANK_PROBLEM_ID, VALID_SUBMISSION_ID, VALID_SUBMISSION_ID_1, VALID_SUBMISSION_ID_2);

        final SubmissionServer.SubmissionRequest request = SubmissionServer.SubmissionRequest.newBuilder()
                .setAuthId(TEACHER_AUTH_ID)
                .setProblemId(VALID_BANK_PROBLEM_ID)
                .addSubmissionIds(VALID_SUBMISSION_ID)
                .addSubmissionIds(VALID_SUBMISSION_ID_1)
                .addSubmissionIds(VALID_SUBMISSION_ID_2)
                .build();

        Mockito.verify(mockSubmissionService).getSubmission(any(RpcController.class), eq(request));
    }

    @Test
    public void callsInsertExperimentCorrectly() throws Exception {

        Submission.SrlExperiment defaultInstance = Submission.SrlExperiment.newBuilder()
                .setProblemId(VALID_BANK_PROBLEM_ID)
                .build();

        webclient.insertExperiment(TEACHER_AUTH_ID, null, defaultInstance, SUBMISSION_TIME);

        final SubmissionServer.ExperimentInsert request = SubmissionServer.ExperimentInsert.newBuilder()
                .setRequestData(SubmissionServer.SubmissionRequest.newBuilder()
                        .setAuthId(TEACHER_AUTH_ID)
                        .setProblemId(VALID_BANK_PROBLEM_ID)
                        .build())
                .setSubmission(defaultInstance)
                .setSubmissionTime(SUBMISSION_TIME)
                .build();


        Mockito.verify(mockSubmissionService).insertExperiment(any(RpcController.class), eq(request));
    }

    @Test
    public void callsInsertSolutionCorrectly() throws Exception {

        Submission.SrlSolution defaultInstance = Submission.SrlSolution.newBuilder()
                .setProblemBankId(VALID_BANK_PROBLEM_ID)
                .build();

        webclient.insertSolution(TEACHER_AUTH_ID, null, defaultInstance);

        final SubmissionServer.SolutionInsert request = SubmissionServer.SolutionInsert.newBuilder()
                .setRequestData(SubmissionServer.SubmissionRequest.newBuilder()
                        .setAuthId(TEACHER_AUTH_ID)
                        .setProblemId(VALID_BANK_PROBLEM_ID)
                        .build())
                .setSubmission(defaultInstance)
                .build();


        Mockito.verify(mockSubmissionService).insertSolution(any(RpcController.class), eq(request));
    }
}
