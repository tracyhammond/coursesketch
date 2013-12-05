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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class ProxyServer extends MultiInternalConnectionServer {

	
	public static final int STATE_INVALID_LOGIN = 4002;
	public static final int MAX_LOGIN_TRIES = 5;
	public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";
	
	private boolean pendingLogin;
	
	//private ExampleClient login = connectLogin(this, connectionType);
	private ProxyConnectionManager serverManager=null;

	static int numberOfConnections = Integer.MIN_VALUE;
	public ProxyServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public ProxyServer( InetSocketAddress address ) {
		super( address );
	}

	public void reConnect() {
		//recognition = ClientManager.connectRecognition(this, connectionType);
		//logindata = ClientManager.connectData(this, connectionType);
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
			String userID = state.getKey();
			serverManager.send(req, userID, LoginConnection.class);
			return;
		} else {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			if(req.getRequestType() == MessageType.RECOGNITION){
				System.out.println("REQUEST TYPE = RECOGNITION");
				String userID = state.getKey();
				serverManager.send(req, userID, RecognitionConnection.class);
			}
			if(req.getRequestType() == MessageType.SUBMISSION){
				System.out.println("REQUEST TYPE = SUBMISSION");
				String userID = state.getKey();
				serverManager.send(req, userID, AnswerConnection.class);
			}
			if(req.getRequestType() == MessageType.DATA_REQUEST){
				System.out.println("REQUEST TYPE = DATA REQUEST");
				String userID = state.getKey();
				serverManager.send(req, userID, DataConnection.class);
			}
			return;
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	/**
	 * Returns a number that should be unique.
	 */
	public LoginConnectionState getUniqueState() {
		return new LoginConnectionState(Encoder.nextID().toString());
	}

	public static void main( String[] args ) throws InterruptedException , IOException, URISyntaxException {
		
		System.out.println("Proxy Server: Version 1.0.2.boa");
		WebSocketImpl.DEBUG = true;
		int port = 8888; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ProxyServer s = new ProxyServer( port );
		s.start();
		ProxyConnectionManager serverManager = new ProxyConnectionManager(s);
		System.out.println( "Proxy Server Started. Port: " + s.getPort() );
		
		//attempt to connect to recognition
		//attempt to connect to answer server
		//attempt to connect to user database
		
		System.out.println("Connetcting to servers...");
		serverManager.connectServers(s);
		
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
			} else if( in.equals( "reconnect")) {
				s.reConnect();
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
	
}
