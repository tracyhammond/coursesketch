package coursesketch.services.grading;

import com.google.protobuf.ServiceException;
import coursesketch.database.grading.GradingManagerInterface;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading;
import protobuf.srl.request.Message;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.services.grading.GradingServer;
import protobuf.srl.services.submission.SubmissionServer;

import java.net.URI;

/**
 * A service for submission.
 */
public final class GradingWebSocketClient extends ClientWebSocket implements GradingManagerInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GradingWebSocketClient.class);

    /**
     * The default address for the Submission server.
     */
    public static final String ADDRESS = "DATABASE_IP_PROP";

    /**
     * The default port of the Database Server.
     */
    public static final int PORT = 8885;

    /**
     * The blocker service that is used to communicate.
     */
    private GradingServer.GradingService.BlockingInterface gradingService;


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
    public GradingWebSocketClient(final URI iDestination,
            final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGrade(Authentication.AuthRequest authRequest, Grading.ProtoGrade grade) throws DatabaseAccessException {
        if (gradingService == null) {
            gradingService = GradingServer.GradingService.newBlockingStub(getRpcChannel());
        }

        LOG.debug("Creating submission request");

        final GradingServer.GradeRequest gradeRequest = GradingServer.GradeRequest.newBuilder()
                .setAuthRequest(authRequest)
                .setGrade(grade).build();

        Message.DefaultResponse response;
        try {
            LOG.debug("Sending add grade request");
            response = gradingService.insertRawGrade(getNewRpcController(), gradeRequest);
            LOG.debug("Submission response {}", response);
            if (response.hasException()) {
                final DatabaseAccessException authExcep =
                        new DatabaseAccessException("Exception with grading server");
                authExcep.setProtoException(response.getException());
                throw authExcep;
            }
        } catch (ServiceException e) {
            throw new DatabaseAccessException("Exception inserting grade into grading server", e);
        }
    }
}
