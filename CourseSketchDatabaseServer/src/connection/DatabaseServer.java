package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import protobuf.srl.request.Message.Request;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class DatabaseServer extends MultiInternalConnectionServer {

	public static final int MAX_CONNECTIONS = 20;
	UpdateHandler updateHandler = new UpdateHandler();
	Database database = new Database();

	static int numberOfConnections = Integer.MIN_VALUE;
	public DatabaseServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public DatabaseServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);

		if (req == null) {
			System.out.println("protobuf error");
			//this.
			// we need to somehow send an error to the client here
			return;
		}
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			updateHandler.addRequest(req);
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Database Server: Version 1.0.2");
		WebSocketImpl.DEBUG = true;
		int port = 8885; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		DatabaseServer s = new DatabaseServer( port );
		s.start();
		System.out.println( "Database Server started on port: " + s.getPort() );

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
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
}
