package connection;

import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ConnectionException;
import coursesketch.server.interfaces.MultiConnectionManager;
import protobuf.srl.request.Message.Request;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
public final class ProxyConnectionManager extends MultiConnectionManager {

    /**
     * IP address for login server.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String LOGIN_ADDRESS = "192.168.56.200";

    /**
     * IP address for database server.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String DATABASE_ADDRESS = "192.168.56.201";

    /**
     * IP address for answer checker server.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String ANSWER_ADDRESS = "192.168.56.203";

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyConnectionManager.class);

    /**
     * Port for the login server.
     */
    private static final int LOGIN_PORT = 8886;

    /**
     * Port for the Database server.
     */
    private static final int DATABASE_PORT = 8885;

    /**
     * Port for the Answer checker server.
     */
    private static final int ANSWER_PORT = 8884;

    private static final int IDENTITY_PORT = 8890;

    /**
     * Creates a manager for the proxy connections.
     *  @param parent
     *            {@link serverfront.ProxyServerWebSocketHandler}
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public ProxyConnectionManager(final AbstractServerWebSocketHandler parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
    }

    /**
     * connects to other servers.
     *
     * @param serv
     *            an instance of the local server (
     *            {@link serverfront.ProxyServerWebSocketHandler}) in this case.
     */
    @Override
    public void connectServers(final AbstractServerWebSocketHandler serv) {
        // System.out.println("Open Recognition...");
        LOG.info("Open Login...");
        LOG.info("Is Connection Local? {}", isConnectionLocal());
        LOG.info("Is Secure? {}", isSecure());
        try {
            createAndAddConnection(serv, isConnectionLocal(), LOGIN_ADDRESS, LOGIN_PORT, isSecure(), LoginClientWebSocket.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }

        LOG.info("Open Data...");
        try {
            createAndAddConnection(serv, isConnectionLocal(), DATABASE_ADDRESS, DATABASE_PORT, isSecure(), DataClientWebSocket.class);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }

        LOG.info("Open Answer...");
        try {
            createAndAddConnection(serv, isConnectionLocal(), ANSWER_ADDRESS, ANSWER_PORT, isSecure(), AnswerClientWebSocket.class);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }

        LOG.info("Open Identity...");
        try {
            createAndAddConnection(serv, isConnectionLocal(), "srl04.tamu.edu", IDENTITY_PORT, isSecure(), IdentityClientWebSocket.class);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
        // System.out.println("Open Answer Checker Server...");
        // createAndAddConnection(serv, true, 8884, AnswerConnection.class);
    }

    /**
     * Creates a request for the web client and strips out all information that
     * should not be sent to the client.
     *
     * @param request
     *            The request that is being sent.
     * @return A clean version of this request.
     */
    public static Request createClientRequest(final Request request) {
        final Request.Builder build = ProtobufUtilities.createBaseResponse(request, true);
        build.clearServersideId();
        build.clearSessionInfo();
        return build.build();
    }

    /**
     * Sends a request to a backend server. (clears the request and replaces the
     * id with the server id)
     *
     * @param req
     *            The message being sent to the client.
     * @param sessionId
     *            the session of the connection the message is being sent to.
     * @param connectionType
     *            the type that the connection is being sent to.
     * @param userId
     *            the sever side id.
     * @throws ConnectionException
     *             thrown if there are problems sending the message.
     */
    public void send(final Request req, final String sessionId, final Class<? extends AbstractClientWebSocket> connectionType, final String userId)
            throws ConnectionException {
        final Request.Builder builder = ProtobufUtilities.createBaseResponse(req, true);
        builder.clearServersideId();
        builder.setServersideId(userId);
        super.send(builder.build(), sessionId, connectionType);
    }
}
