package proxyServer;

import internalConnections.AnswerConnection;
import internalConnections.DataConnection;
import internalConnections.LoginConnection;
import internalConnections.LoginConnectionState;
import internalConnections.ProxyConnectionManager;
import internalConnections.RecognitionConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class ProxyServer extends MultiInternalConnectionServer {

	public static final int MAX_CONNECTIONS = 60; // sets see if this works!

	public static final int STATE_INVALID_LOGIN = 4002;
	public static final int MAX_LOGIN_TRIES = 5;
	public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";

	//private ExampleClient login = connectLogin(this, connectionType);
	private ProxyConnectionManager serverManager = new ProxyConnectionManager(this);

	static int numberOfConnections = Integer.MIN_VALUE;
	public ProxyServer( int port ) {
		this( new InetSocketAddress( port ) );
	}

	public ProxyServer( InetSocketAddress address ) {
		super( address );
	}

	public void reConnect() {
		serverManager.dropAllConnection(false, true);
		serverManager.connectServers(this);
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);
		LoginConnectionState state = (LoginConnectionState) connectionToId.get(conn);

		if (req == null) {
			System.out.println("protobuf error");
			//this.
			// we need to somehow send an error to the client here
			return;
		}
		//DO NOT FORGET ABOUT THIS
		if (state.isPending()) {
			//conn.send(pending);
			return;
		}
		if (!state.isLoggedIn()) {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			String sessionID = state.getKey();
			System.out.println("Request type is " + req.getRequestType().name());
			try {
				serverManager.send(req, sessionID, LoginConnection.class);
			} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
				conn.send(createBadConnectionResponse(req, LoginConnection.class).toByteArray());
			}
		} else {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			if (req.getRequestType() == MessageType.RECOGNITION) {
				System.out.println("REQUEST TYPE = RECOGNITION");
				String sessionID = state.getKey();
				try {
					serverManager.send(req, sessionID, RecognitionConnection.class); // no userId is sent for security reasons.
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					conn.send(createBadConnectionResponse(req, RecognitionConnection.class).toByteArray());
				}
				return;
			}
			if (req.getRequestType() == MessageType.SUBMISSION) {
				System.out.println("REQUEST TYPE = SUBMISSION");
				String sessionID = state.getKey();
				try {
					serverManager.send(req, sessionID, AnswerConnection.class, ((ProxyConnectionState) state).getUserId());
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					conn.send(createBadConnectionResponse(req, AnswerConnection.class).toByteArray());
				}
				return;
			}
			if (req.getRequestType() == MessageType.DATA_REQUEST || req.getRequestType() == MessageType.DATA_INSERT
					|| req.getRequestType() == MessageType.DATA_UPDATE || req.getRequestType() == MessageType.DATA_REMOVE) {
				System.out.println("REQUEST TYPE = DATA REQUEST");
				String sessionID = state.getKey();
				try {
					serverManager.send(req, sessionID, DataConnection.class, ((ProxyConnectionState) state).getUserId());
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					conn.send(createBadConnectionResponse(req, DataConnection.class).toByteArray());
				}
				return;
			}
			return;
		}
	}

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	private static Request createBadConnectionResponse(Request req, Class<? extends WrapperConnection> connectionType) {
		Request.Builder response = Request.newBuilder();
		response.setRequestType(req.getRequestType());
		response.setResponseText("A server with connection type: " + connectionType.getSimpleName() +" Is not connected correctly");
		return response.build();
	}
	
	/**
	 * Returns a number that should be unique.
	 */
	@Override
	public LoginConnectionState getUniqueState() {
		return new ProxyConnectionState(Encoder.nextID().toString());
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Proxy Server: Version 1.0.2.lemur");
		WebSocketImpl.DEBUG = true;
		int port = 8888; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ProxyServer s = new ProxyServer( port );
		s.start();
		System.out.println( "Proxy Server Started. Port: " + s.getPort() );
		
		//attempt to connect to recognition
		//attempt to connect to answer server
		//attempt to connect to user database
		
		System.out.println("Connecting to servers...");
		//serverManager.connectServers(s);
		s.reConnect();
		
		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			if( in.equals( "exit" ) ) {
				System.out.println("Closing down server");
				s.stop();
				s.serverManager.dropAllConnection(true, true);
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			} else if( in.equals( "reconnect")) {
				System.out.println("Attempting to recoonect");
				s.reConnect();
			}
		}
		System.out.println("Closing down server! Forcefully");
		System.exit(0);
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
	
}
