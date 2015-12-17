package coursesketch.services.submission;

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
import java.util.List;

/**
 * Created by dtracers on 12/15/2015.
 */
public class SubmissionWebSocketClient extends ClientWebSocket implements SubmissionManagerInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionWebSocketClient.class);

    /**
     * The default address for the identity server.
     */
    public static final String ADDRESS = "IDENTITY_IP_PROP";

    /**
     * The default port of the Identity Server.
     */
    public static final int PORT = 8891;

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

    @Override public List<Submission.SrlExperiment> getSubmission(final String authId, final Authenticator authenticator,
            final String problemId, final String... submissionIds) {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }
        return null;
    }

    @Override public String insertExperiment(final String authId, final Authenticator authenticator, final Submission.SrlExperiment submission,
            final long submissionTime) throws AuthenticationException, DatabaseAccessException {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }
        return null;
    }

    @Override public String insertSolution(final String authId, final Authenticator authenticator, final Submission.SrlSolution submission)
            throws AuthenticationException, DatabaseAccessException {
        if (submissionService == null) {
            submissionService = SubmissionServer.SubmissionService.newBlockingStub(getRpcChannel());
        }
        return null;
    }

}
