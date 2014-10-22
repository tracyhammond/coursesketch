package connection;

import java.security.GeneralSecurityException;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.SocketSession;
import database.LoginException;
import database.RegistrationException;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import database.DatabaseClient;
import utilities.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket()
public final class LoginServerWebSocketHandler extends ServerWebSocketHandler {

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
     * Sent if the user does not have the ability to login as a student or an
     * instructor.
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
     */
    public LoginServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
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
            e.printStackTrace();
            send(conn, createLoginResponse(req, null, false, INCORRECT_LOGIN_MESSAGE, false, null));
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
            // registers user
            DatabaseClient.createUser(login.getUsername(), login.getPassword(), login.getEmail(), login.getIsInstructor());

            // login user after registering user.
            loginUser(conn, req, login);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            send(conn, createLoginResponse(req, login, false, e.getMessage(), false, null));
        } catch (RegistrationException e) {
            e.printStackTrace();
            send(conn, createLoginResponse(req, login, false, e.getMessage(), false, null));
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
        final boolean loginAsDefault = login.hasIsInstructor();
        try {
            final String userLoggedIn = DatabaseClient.mongoIdentify(login.getUsername(), login.getPassword(), loginAsDefault,
                    login.getIsInstructor());
            if (userLoggedIn != null) {
                final String[] ids = userLoggedIn.split(":");
                if (ids.length == 2) {
                    final boolean isInstructor = checkUserInstructor(login.getUsername(), login);
                    send(conn, createLoginResponse(req, login, true, CORRECT_LOGIN_MESSAGE, isInstructor, ids));
                }
            }
        } catch (LoginException e) {
            e.printStackTrace();
            send(conn, createLoginResponse(req, login, false, e.getMessage(), false, null));
        }
    }

    /**
     * @param user
     *            the user id.
     * @param login
     *            the information of what the user is attempting to do.
     * @return true if the user is an instructor false otherwise.
     */
    private static boolean checkUserInstructor(final String user, final LoginInformation login) {
        System.out.println("About to check if user is an instructor!");
        if (!login.hasIsInstructor()) {
            return DatabaseClient.defaultIsInstructor(user);
        } else {
            return login.getIsInstructor();
        }
    }

    /**
     * Creates a {@link Request} to return on login request.
     *
     * @param req
     *            Request from which to generate the response.
     * @param success
     *            <code>true</code> if the login was successful,
     *            <code>false</code> otherwise
     * @param message
     *            Message text to be included in the response.
     * @param instructorIntent
     *            <code>true</code> if the user is an instructor,
     *            <code>false</code> otherwise
     * @param ids
     *            List of user IDs.
     * @param login
     *            The information sent by the user for logging in.
     *
     * @return the request body
     */
    private static Request createLoginResponse(final Request req, final LoginInformation login, final boolean success, final String message,
            final boolean instructorIntent, final String[] ids) {
        final Request.Builder requestBuilder = Request.newBuilder();
        requestBuilder.setRequestType(MessageType.LOGIN);
        requestBuilder.setResponseText(message);
        requestBuilder.setSessionInfo(req.getSessionInfo());
        if (ids != null && ids.length > 0 && success) {
            requestBuilder.setServersideId(ids[0]); // TODO encrypt this id
        }

        if (login != null) {
            // Create the Login Response.
            final LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
            loginBuilder.setUsername(login.getUsername());
            loginBuilder.setIsLoggedIn(success);
            loginBuilder.setIsInstructor(instructorIntent);
            if (success) {
                // The reason for this is so the proxy can continue to register
                // user
                loginBuilder.setIsRegistering(login.getIsRegistering());
                if (loginBuilder.getIsRegistering()) {
                    loginBuilder.setEmail(login.getEmail());
                }
                if (ids != null && ids.length > 1) {
                    loginBuilder.setUserId(ids[1]);
                }
            }

            // Add login info.
            requestBuilder.setOtherData(loginBuilder.build().toByteString());
        }
        // Build and send.
        return requestBuilder.build();
    }
}
