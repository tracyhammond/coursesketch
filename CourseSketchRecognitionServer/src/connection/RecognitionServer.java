package connection;

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

import main.Response;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.commands.Commands.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.Update;
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
public class RecognitionServer extends WebSocketServer {

	public static final int MAX_CONNECTIONS = 20;
	public static final int STATE_SERVER_FULL = 4001;
	static final String FULL_SERVER_MESSAGE = "Sorry, the RECOGNITION server is full";
	
	List<WebSocket> connections = new LinkedList<WebSocket>();
	HashMap<String, Response> idToResponse = new HashMap<String, Response>();	

	static int numberOfConnections = Integer.MIN_VALUE;
	public RecognitionServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public RecognitionServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		if (connections.size() >= MAX_CONNECTIONS) {
			// Return negatative state.
			conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
			System.out.println("FULL SERVER"); // send message to someone?
			return;
		}
		ConnectionState id = getUniqueId();
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

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		Request req = Decoder.parseRequest(buffer);
		if (req == null) {
			System.out.println("protobuf error");
			// we need to somehow send an error to the client here
			return;
		}
		if (req.getRequestType() == Request.MessageType.RECOGNITION) {
			ByteString rawUpdateData = req.getOtherData();
			Update savedUpdate = Decoder.parseNextUpdate(rawUpdateData);
			req.getSessionInfo();
			Response r = null;
			//if it does not exist, add it to map
			if(!idToResponse.containsValue(req.getSessionInfo())){
				r = new Response();
				idToResponse.put(req.getSessionInfo(), r);
			}
			else{
				r=idToResponse.get(req.getSessionInfo());
			}
			//r.interpret(savedUpdate);
			//pass to them
			//use a function that they will give
			Request result = null;

			try {
				// TODO: move these inside the class itself.
				r.print(savedUpdate);
				SrlShape shape = r.interpret(savedUpdate);
				SrlStroke stroke = r.mirror(savedUpdate);
				//post function they will give (package the information received)
				Command com1 = Encoder.createCommandFromBytes(stroke.toByteString(), CommandType.ADD_STROKE);
				Command com2 = Encoder.createCommandFromBytes(shape.toByteString(), CommandType.ADD_SHAPE);
				
				
				result = Encoder.createRequestFromCommands(req.getSessionInfo(), com1, com2);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			conn.send(result.toByteArray());
		}
		return;
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	/**
	 * Returns a number that should be unique.
	 */
	public ConnectionState getUniqueId() {
		// TODO: Assign ID using a linked list so they can be used multiple times.  O(1) when used as a Queue
		return new ConnectionState(numberOfConnections++);
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Recognition Server: Version 1.0.2");
		WebSocketImpl.DEBUG = true;
		int port = 8888; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		RecognitionServer s = new RecognitionServer( port );
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
}
