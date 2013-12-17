package connection;

//import internalConnections.LoginConnectionState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class LoginServer extends WebSocketServer {

	public static final int MAX_CONNECTIONS = 20;
	public static final int STATE_SERVER_FULL = 4001;
	static final String FULL_SERVER_MESSAGE = "Sorry, the RECOGNITION server is full";
	
	public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect username or password";
	public static final String INCORRECT_LOGIN_TYPE_MESSAGE = "You do not have the ability to login as that type!";
	public static final String PERMISSION_ERROR_MESSAGE = "There was an error assigning permissions";
	public static final String CORRECT_LOGIN_MESSAGE = "Login is successful";
	public static final String LOGIN_ERROR_MESSAGE = "An Error Occured While Logging in: Wrong Message Type.";
	public static final String REGISTRATION_ERROR_MESSAGE = "Could not Register: User name is already taken";

	
	List<WebSocket> connections = new LinkedList<WebSocket>();

	static int numberOfConnections = Integer.MIN_VALUE;
	public LoginServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public LoginServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println("Open Login Connection now...");
		if (connections.size() >= MAX_CONNECTIONS) {
			// Return negatative state.
			conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
			System.out.println("FULL SERVER"); // send message to someone?
			return;
		}
		//ConnectionState id = getUniqueId();
		connections.add(conn);
		System.out.println("ID ASSIGNED");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has disconnected from Recognition.");
		connections.remove(conn);
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
	}

	//Encrypt user id with session ID (e-user ID)
	
	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		Request req = Decoder.parseRequest(buffer);
		try {
			//This is assuming user is logged in
			//conn.send(createLoginResponse(req, true));
			
			if (req.getLogin().getIsRegistering()) {
				boolean successfulRegistration = registerUser(req.getLogin().getUsername(), req.getLogin().getPassword(), req.getLogin().getEmail(), req.getLogin().getIsInstructor());
				if(!successfulRegistration){
					conn.send(createLoginResponse(req, false, REGISTRATION_ERROR_MESSAGE, false, null).toByteArray());
					return;
				}
			}
			String userLoggedIn = checkUserLogin(req.getLogin().getUsername(), req.getLogin().getPassword());
			if (userLoggedIn != null) {
				String[] ids= userLoggedIn.split(":");
				if (ids.length == 2) {
					boolean isInstructor = checkUserInstructor(req.getLogin().getUsername());
				 	//return if database is an instructor
					conn.send(createLoginResponse(req, true, CORRECT_LOGIN_MESSAGE, isInstructor, ids).toByteArray());
					return;
				}
			}
			conn.send(createLoginResponse(req, false, INCORRECT_LOGIN_MESSAGE, false, null).toByteArray());
		}
		catch (Exception e) {
			e.printStackTrace();
			conn.send(createLoginResponse(req, false, e.getMessage(), false, null).toByteArray());
		}
	}
	
	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	private static String checkUserLogin(String user, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException
	{
		System.out.println("About to identify the user!");
		String result = DatabaseClient.mongoIdentify(user,password);
		return result;
	}

	private static boolean checkUserInstructor(String user)
	{
		System.out.println("About to check if user is an instructor!");
		if (DatabaseClient.mongoIsInstructor(user))
			return true;
		return false;
	}
	
	private static boolean registerUser(String user, String password,String email,boolean isInstructor) throws InvalidKeySpecException, GeneralSecurityException{
		if (DatabaseClient.MongoAddUser(user, password, email, isInstructor)){
			return true;
		}
		return false;
	}

	public List<WebSocket> getConnections(){
		return connections;
	}

	/**
	 * Creates a {@link Request} to return on login request.
	 */
	/* package-private */private static Request createLoginResponse(Request req, boolean success, String message, boolean instructorIntent, String[] ids) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		requestBuilder.setResponseText(message);
		requestBuilder.setSessionInfo(req.getSessionInfo());
		if (ids != null && ids.length > 0) {
			requestBuilder.setSessionId(ids[0]); // TODO: encrypt this id
		}
		System.out.println("setting return session information " + req.getSessionInfo());
		
		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		loginBuilder.setIsInstructor(instructorIntent);
		if (ids != null && ids.length > 1) {
			loginBuilder.setUserId(ids[1]);
		}

		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		// Build and send.
		return requestBuilder.build();
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Login Server: Version 1.0.2.gopher");
		WebSocketImpl.DEBUG = true;
		int port = 8886; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		LoginServer s = new LoginServer( port );
		s.start();
		System.out.println( "Login Server started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			}
		}
	}
}
