package proxyServer;

import internalConnections.ManagerConnection;

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
public class ProxyServer extends WebSocketServer {

	public static final int MAX_CONNECTIONS = 20;
	public static final int STATE_SERVER_FULL = 4001;
	public static final int STATE_INVALID_LOGIN = 4002;
	public static final int MAX_LOGIN_TRIES = 5;
	public static final String FULL_SERVER_MESSAGE = "Sorry the server is full";
	public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";
	
	private boolean pendingLogin;

	HashMap<WebSocket, ConnectionState> connectionToId = new HashMap<WebSocket, ConnectionState>();
	private HashMap<ConnectionState, WebSocket> idToConnection = new HashMap<ConnectionState, WebSocket>();
	private HashMap<String, ConnectionState> idToState = new HashMap<String, ConnectionState>();
	
	//private ExampleClient login = connectLogin(this, connectionType);
	private ManagerConnection serverManager=null;

	static int numberOfConnections = Integer.MIN_VALUE;
	public ProxyServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public ProxyServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		if (connectionToId.size() >= MAX_CONNECTIONS) {
			// Return negatative state.
			conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
			System.out.println("FULL SERVER"); // send message to someone?
			return;
		}
		ConnectionState id = getUniqueId();
		connectionToId.put(conn, id);
		getIdToConnection().put(id, conn);
		System.out.println("Session Key " + id.getKey());
		getIdToState().put(id.getKey(), id);
		System.out.println("ID ASSIGNED");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has disconnected from Proxy");
		getIdToConnection().remove(connectionToId.remove(conn));
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
	}

	public void reConnect() {
		//recognition = ClientManager.connectRecognition(this, connectionType);
		//logindata = ClientManager.connectData(this, connectionType);
	}
	
	/*public static ExampleClient connectLogin(ProxyServer serv, boolean local) {
		ExampleClient c=null;
		String location = local ? "ws://localhost:8888" : "ws://goldberglinux02.tamu.edu:8886";
		try {
			c = new ExampleClient( new URI( location ), new Draft_10() , serv);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		return c;
	}*/
	
	

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);
		ConnectionState state = connectionToId.get(conn);

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
			//Request response = LoginChecker.checkLogin(req, state);
			//System.out.println("Not Logged In!");
			//System.out.println("REQUEST TYPE = CLIENT LOGIN");
			String userID = state.getKey();
			Request packagedRequest = Encoder.requestIDBuilder(req, userID);
			serverManager.getLoginClient().send(packagedRequest.toByteArray());
			return;
		} else {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			if(req.getRequestType() == MessageType.RECOGNITION){
				System.out.println("REQUEST TYPE = RECOGNITION");
				String userID = state.getKey();
				serverManager.send(req, userID);
			}
			/*if(req.getRequestType() == MessageType.DATA_SENDING){
				logindata.connection = state;
				System.out.println("REQUEST TYPE = DATA SENDING");
				String userID = state.getKey();
				Request packagedRequest = Encoder.requestIDBuilder(req, userID);
				logindata.send(packagedRequest.toByteArray());
			}*/
			// Parse message.
			conn.send(buffer);
			return;
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	/**
	 * Returns a number that should be unique.
	 */
	public ConnectionState getUniqueId() {
		return new ConnectionState(Encoder.nextID().toString());
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
		ManagerConnection serverManager = new ManagerConnection(s);
		System.out.println( "Proxy Server Started. Port: " + s.getPort() );

		//attempt to connect to recognition
		//attempt to connect to answer server
		//attempt to connect to user database
		
		System.out.println("Connetcting to servers...");
		serverManager.connectServers(s);

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

	public HashMap<String, ConnectionState> getIdToState() {
		return idToState;
	}

	public HashMap<ConnectionState, WebSocket> getIdToConnection() {
		return idToConnection;
	}
	
}
