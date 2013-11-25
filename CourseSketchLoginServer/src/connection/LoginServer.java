package connection;

//import internalConnections.LoginConnectionState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
		System.out.println("Open Login Connection");
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
		try{
			//This is assuming user is logged in
			//conn.send(createLoginResponse(req, true));
			boolean userLoggedIn = CheckUserLogin();
			if (userLoggedIn) {
			 	//return if database is an instructor
				conn.send(createLoginResponse(req, true, CORRECT_LOGIN_MESSAGE, userLoggedIn).toByteArray());
			} else {
				//state.addTry();
				conn.send(createLoginResponse(req, true, INCORRECT_LOGIN_MESSAGE, userLoggedIn).toByteArray());
			}
		}
		catch(Exception e){
			conn.send(createLoginResponse(req, false, LOGIN_ERROR_MESSAGE, false).toByteArray());
		}
	}
	
	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	/**
	 * Returns a number that should be unique.
	 */
	/*public ConnectionState getUniqueId() {
		// TODO: Assign ID using a linked list so they can be used multiple times.  O(1) when used as a Queue
		return new ConnectionState(numberOfConnections++);
	}*/
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Login Server: Version 1.0.2.boa");
		WebSocketImpl.DEBUG = true;
		int port = 8886; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		LoginServer s = new LoginServer( port );
		s.start();
		System.out.println( "Recognition Server started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
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
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
	private boolean CheckUserLogin(){
		return true;
	}
	
	public List<WebSocket> getConnections(){
		return connections;
	}
	
	/**
	 * Creates a {@link Request} to return on login request.
	 */
	/* package-private */private static Request createLoginResponse(Request req, boolean success, String message, boolean instructorIntent) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setRequestType(MessageType.LOGIN);
		requestBuilder.setResponseText(message);
		
		// Create the Login Response.
		LoginInformation.Builder loginBuilder = LoginInformation.newBuilder();
		loginBuilder.setUsername(req.getLogin().getUsername());
		loginBuilder.setIsLoggedIn(success);
		loginBuilder.setIsInstructor(instructorIntent);
		loginBuilder.setSessionInfo("SESSION_KEY");

		// Add login info.
		requestBuilder.setLogin(loginBuilder.build());
		// Build and send.
		return requestBuilder.build();
	}
}
