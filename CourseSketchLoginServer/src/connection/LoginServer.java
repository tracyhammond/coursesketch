package connection;

// import internalConnections.LoginConnectionState;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import database.DatabaseClient;

/**
 * A simple WebSocketServer implementation.
 * 
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket()
public final class LoginServer extends GeneralConnectionServer {

	public LoginServer(GeneralConnectionServlet parent) {
		super(parent);
	}

	@SuppressWarnings("hiding")
	public static final int MAX_CONNECTIONS = 20;

	public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect username or password";
	public static final String INCORRECT_LOGIN_TYPE_MESSAGE = "You do not have the ability to login as that type!";
	public static final String PERMISSION_ERROR_MESSAGE = "There was an error assigning permissions";
	public static final String CORRECT_LOGIN_MESSAGE = "Login is successful";
	public static final String LOGIN_ERROR_MESSAGE = "An Error Occured While Logging in: Wrong Message Type.";
	public static final String REGISTRATION_ERROR_MESSAGE = "Could not Register: User name is already taken";

	static int numberOfConnections = Integer.MIN_VALUE;

	@Override
	public void onMessage(Session conn, Request req) {
		try {
			// This is assuming user is logged in
			// conn.send(createLoginResponse(req, true));

			if (req.getRequestType() == Request.MessageType.TIME) {
				Request rsp = TimeManager.decodeRequest(req);
				if (rsp != null) {
					send(conn, rsp);
				}
				return;
			}

			if (req.getLogin().getIsRegistering()) {
				boolean successfulRegistration = registerUser(req.getLogin().getUsername(), req.getLogin().getPassword(), req.getLogin().getEmail(),
						req.getLogin().getIsInstructor());
				if (!successfulRegistration) {
					send(conn, createLoginResponse(req, false, REGISTRATION_ERROR_MESSAGE, false, null));
					return;
				}
			}
			String userLoggedIn = checkUserLogin(req.getLogin().getUsername(), req.getLogin().getPassword());
			if (userLoggedIn != null) {
				String[] ids = userLoggedIn.split(":");
				if (ids.length == 2) {
					boolean isInstructor = checkUserInstructor(req.getLogin().getUsername());
					// return if database is an instructor
					send(conn, createLoginResponse(req, true, CORRECT_LOGIN_MESSAGE, isInstructor, ids));
					return;
				}
			}
			send(conn, createLoginResponse(req, false, INCORRECT_LOGIN_MESSAGE, false, null));
		} catch (Exception e) {
			e.printStackTrace();
			send(conn, createLoginResponse(req, false, e.getMessage(), false, null));
		}
	}

	private static String checkUserLogin(String user, String password) throws NoSuchAlgorithmException, InvalidKeySpecException,
			UnknownHostException {
		System.out.println("About to identify the user!");
		String result = DatabaseClient.mongoIdentify(user, password);
		return result;
	}

	private static boolean checkUserInstructor(String user) {
		System.out.println("About to check if user is an instructor!");
		if (DatabaseClient.mongoIsInstructor(user))
			return true;
		return false;
	}

	private static boolean registerUser(String user, String password, String email, boolean isInstructor) throws InvalidKeySpecException,
			GeneralSecurityException {
		if (DatabaseClient.MongoAddUser(user, password, email, isInstructor)) {
			return true;
		}
		return false;
	}

	/**
	 * Creates a {@link Request} to return on login request.
	 */
	private static Request createLoginResponse(Request req, boolean success, String message, boolean instructorIntent,
			String[] ids) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		requestBuilder.setResponseText(message);
		requestBuilder.setSessionInfo(req.getSessionInfo());
		if (ids != null && ids.length > 0 && success) {
			requestBuilder.setServersideId(ids[0]); // TODO: encrypt this id
		}

		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		loginBuilder.setIsInstructor(instructorIntent);
		if (success) {
			// The reason for this is so the proxy can continue to register user
			loginBuilder.setIsRegistering(req.getLogin().getIsRegistering());
			if (loginBuilder.getIsRegistering()) {
				loginBuilder.setEmail(req.getLogin().getEmail());
			}
			if (ids != null && ids.length > 1) {
				loginBuilder.setUserId(ids[1]);
			}
		}

		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		// Build and send.
		return requestBuilder.build();
	}
}
