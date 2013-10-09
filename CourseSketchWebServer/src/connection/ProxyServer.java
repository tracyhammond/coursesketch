package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class ProxyServer extends WebSocketServer {

	// id
	HashMap<WebSocket, ConnectionState> connectionToId = new HashMap<WebSocket, ConnectionState>();
	HashMap<ConnectionState, WebSocket> idToConnection = new HashMap<ConnectionState, WebSocket>();
	public static final int MAX_CONNECTIONS = 20;
	static int numberOfConnections = Integer.MIN_VALUE;
	public ProxyServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public ProxyServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		if(connectionToId.size() >= MAX_CONNECTIONS) {
			//return negatative state
			return;
		}
		ConnectionState id = getUniqueId();
		connectionToId.put(conn, id);
		idToConnection.put(id, conn);
		
		//return positive status 

		
		//	this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
		//	System.out.println("new connection: " + handshake.getResourceDescriptor());
		//	System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has left the room!" );
		idToConnection.remove(connectionToId.remove(conn));
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		ConnectionState state = connectionToId.get(conn);
		if(!state.isLoggedIn()) {
			// Check log in information
			// return failed log in information on failure
			boolean goodLogin = true;
			if (goodLogin) {
				state.logIn();
				return;
			} else {
				return;
			}
		} else {
			// Parse message
			conn.send(message);
			return;
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		if(!connectionToId.get(conn).isLoggedIn()) {
			// Check log in information
			// return failed log in information on failure
		} else {
			// Parse message
			conn.send(buffer);
			return;
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		System.out.println( "received fragment: " + fragment );
	}

	/**
	 * Returns a number that should be unique.
	 */
	public ConnectionState getUniqueId() {
		return new ConnectionState(numberOfConnections++);
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ProxyServer s = new ProxyServer( port );
		s.start();
		System.out.println( "ChatServer started on port: " + s.getPort() );

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
}
