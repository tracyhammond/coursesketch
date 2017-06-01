package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import coursesketch.database.DatabaseClient;
import coursesketch.database.LoginException;
import coursesketch.database.RegistrationException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.identity.IdentityWebSocketClient;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;
import utilities.TimeManager;

import java.security.GeneralSecurityException;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public final class LoginServerWebSocketHandler extends ServerWebSocketHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoginServerWebSocketHandler.class);

    /**
     * The name of the socket.
     */
    public static final String NAME = "Login";

    /**
     * The login server can only support 20 connections at a time.
     *
     * Just a random default that was not determined empirically.
     */
    @SuppressWarnings("hiding")
    public static final int MAX_CONNECTIONS = 20;

    /**
     * Sent when the user logs in with an incorrect password or username.
     */
    public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect username " + "or password";

    /**
     * Sent if the user does not have the ability to login as a student or an instructor.
     */
    public static final String INCORRECT_LOGIN_TYPE_MESSAGE = "You do " + "not have the ability to login as that type!";
    /**
     * Sent to the user if there was a problem creating the permissions.
     */
    public static final String PERMISSION_ERROR_MESSAGE = "There was an error " + "assigning permissions";

    /**
     * Sent to the user when they login correctly.
     */
    public static final String CORRECT_LOGIN_MESSAGE = "Login is successful";

    /**
     * Sent if the message type was wrong.
     */
    public static final String LOGIN_ERROR_MESSAGE = "An Error Occured While " + "Logging in: Wrong Message Type.";

    /**
     * Sent if the user is already registered in the system.
     */
    public static final String REGISTRATION_ERROR_MESSAGE = "Could not " + "Register: User name is already taken";

    /**
     * Creates an instance of the login server.
     *
     * @param parent
     *            {@link connection.LoginServlet}
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public LoginServerWebSocketHandler(final ServerWebSocketInitializer parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final SocketSession conn, final Request req) {
        if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                send(conn, rsp);
            }
            return;
        }

        try {
            final LoginInformation login = LoginInformation.parseFrom(req.getOtherData());
            if (login.getIsRegistering()) {
                registerUserMessage(conn, req, login);
            } else {
                loginUser(conn, req, login);
            }
        } catch (final InvalidProtocolBufferException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            send(conn, createLoginResponse(req, null, false, INCORRECT_LOGIN_MESSAGE, null));
        }
    }

    /**
     * Registers the user than attempts to log in the user using the newly
     * created information.
     *
     * @param conn
     *            the session object that created the message
     * @param req
     *            the message itself
     * @param login
     *            the information about the user attempting to log in and how
     *            they are attempting to log in.
     */
    private void registerUserMessage(final SocketSession conn, final Request req, final LoginInformation login) {
        try {
            final DatabaseClient client = (DatabaseClient) super.getDatabaseReader();
            // registers user
            client.createUser(login.getUsername(), login.getPassword(), login.getEmail(), login.getIsInstructor());
            // login user after registering user.
            loginUser(conn, req, login);
        } catch (GeneralSecurityException | AuthenticationException | RegistrationException | DatabaseAccessException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            send(conn, createLoginResponse(req, login, false, e.getMessage(), null));
        }
    }

    /**
     * Logs in the user and sends a message to the client.
     *
     * @param conn
     *            the session object that created the message
     * @param req
     *            the message itself
     * @param login
     *            the information about the user attempting to log in and how
     *            they are attempting to log in.
     */
    private void loginUser(final SocketSession conn, final Request req, final LoginInformation login) {
        // if not specified then log in as default user.
        final boolean loginAsDefault = !login.hasIsInstructor();
        final DatabaseClient client = (DatabaseClient) super.getDatabaseReader();
        try {
            final BasicDBObject userLoginInfo = client.mongoIdentify(login.getUsername(), login.getPassword(), loginAsDefault,
                    login.getIsInstructor());
            if (userLoginInfo != null) {
                send(conn, createLoginResponse(req, login, true, CORRECT_LOGIN_MESSAGE, userLoginInfo));
                client.userLoggedInSuccessfully(login.getUsername(), (String) userLoginInfo.get(DatabaseClient.SERVER_ID),
                        (boolean) userLoginInfo.get(DatabaseClient.IS_INSTRUCTOR), TimeManager.getSystemTime());
            }
        } catch (LoginException e) {
            LOG.warn("Login failed, creating failed response.");
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            send(conn, createLoginResponse(req, login, false, e.getMessage(), null));
        }
    }

    /**
     * Creates a {@link Request} to return on login request.
     *
     * @param userLoginInfo A {@link BasicDBObject} with a set of values:
     *          {
     *              CLIENT_ID: clientId,
     *              SERVER_ID: serverId,
     *              IS_INSTRUCTOR: boolean
     *          }
     * @param req
     *            Request from which to generate the response.
     * @param login
     *            The information sent by the user for logging in.
     * @param success
     *            <code>true</code> if the login was successful,
     *            <code>false</code> otherwise
     * @param message
     *            Message text to be included in the response.
     * @param userLoginInfo
     * @return {@link Request} that contains the response from the login server.
     */
    private static Request createLoginResponse(final Request req, final LoginInformation login, final boolean success, final String message,
            final BasicDBObject userLoginInfo) {
        final Request.Builder requestBuilder = ProtobufUtilities.createBaseResponse(req);
        requestBuilder.setResponseText(message);
        if (userLoginInfo != null) {
            requestBuilder.setServersideId((String) userLoginInfo.get(DatabaseClient.SERVER_ID));
            requestBuilder.setServerUserId((String) userLoginInfo.get(DatabaseStringConstants.USER_ID));
        }
        if (login != null) {
            // Create the Login Response.
            final LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
            loginBuilder.setUsername(login.getUsername());
            loginBuilder.setIsLoggedIn(success);
            if (success && userLoginInfo != null) {
                // The reason for this is so the proxy can continue to register user
                loginBuilder.setIsRegistering(login.getIsRegistering());
                if (loginBuilder.getIsRegistering()) {
                    loginBuilder.setEmail(login.getEmail());
                }
                loginBuilder.setUserId((String) userLoginInfo.get(DatabaseClient.CLIENT_ID));
                loginBuilder.setIsInstructor((Boolean) userLoginInfo.get(DatabaseClient.IS_INSTRUCTOR));
            }

            // Add login info.
            requestBuilder.setOtherData(loginBuilder.build().toByteString());
        }
        // Build and send.
        return requestBuilder.build();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link DatabaseClient}.
     */
    @Override protected AbstractCourseSketchDatabaseReader createDatabaseReader(final ServerInfo info) {
        final IdentityManagerInterface identityWebSocketClient = (IdentityWebSocketClient) getConnectionManager()
                .getBestConnection(IdentityWebSocketClient.class);
        return new DatabaseClient(info, identityWebSocketClient);
    }

}
