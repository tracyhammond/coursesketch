package connection;

import internalConnection.AnswerConnectionManager;
import internalConnection.AnswerConnectionState;
import internalConnection.SolutionConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;

import protobuf.srl.request.Message.Request;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
public class AnswerCheckerServer extends MultiInternalConnectionServer {

	public static final int MAX_CONNECTIONS = 20;
	public static final int STATE_SERVER_FULL = 4001;
	static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";
	
	List<WebSocket> connections = new LinkedList<WebSocket>();
	AnswerConnectionManager internalConnections = new AnswerConnectionManager(this);
	// has some sort of client!

	static int numberOfConnections = Integer.MIN_VALUE;
	public AnswerCheckerServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public AnswerCheckerServer( InetSocketAddress address ) {
		super( address );
		internalConnections.addConnection(internalConnections.createConnection(this, false, 9000, SolutionConnection.class));
	}

	/**
	 * Returns a number that should be unique.
	 */
	public AnswerConnectionState getUniqueState() {
		return new AnswerConnectionState(Encoder.nextID().toString());
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
			// then we submit!
			try {
				SrlExperiment student = SrlExperiment.parseFrom(req.getOtherData());
				MultiConnectionState state = connectionToId.get(conn);
				((AnswerConnectionState) state).addPendingExperiment(req.getSessionInfo(),student);
				internalConnections.send(req, req.getSessionInfo() + "+" + state.getKey(), 0);
				return;
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				SrlSolution instructor = SrlSolution.parseFrom(req.getOtherData());
				// I know it is a solution...
				internalConnections.send(req, req.getSessionInfo(), 0);
				return;
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// CODE GOES HERE
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Recognition Server: Version 1.0.2");
		WebSocketImpl.DEBUG = true;
		int port = 8888; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		AnswerCheckerServer s = new AnswerCheckerServer( port );
		s.start();
		System.out.println( "Recognition Server started on port: " + s.getPort() );

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
