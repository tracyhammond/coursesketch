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

import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
public class AnswerCheckerServer extends MultiInternalConnectionServer {
	
	List<WebSocket> connections = new LinkedList<WebSocket>();
	AnswerConnectionManager internalConnections = new AnswerConnectionManager(this);
	// has some sort of client!

	static int numberOfConnections = Integer.MIN_VALUE;
	public AnswerCheckerServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public AnswerCheckerServer( InetSocketAddress address ) {
		super( address );
		//internalConnections.createAndAddConnection(this, false, 9000, SolutionConnection.class);
	}

	/**
	 * Returns a number that should be unique.
	 */
	public AnswerConnectionState getUniqueState() {
		return new AnswerConnectionState(Encoder.nextID().toString());
	}

	public void reConnect() {
		internalConnections.dropAllConnection(false, true);
		internalConnections.connectServers(this);
	}
	
	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		//System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);

		if (req == null) {
			System.out.println("protobuf error");
			//this.
			// we need to somehow send an error to the client here
			return;
		}
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			// then we submit!
			if (req.getResponseText().equals("student")) {
				MultiConnectionState state = connectionToId.get(conn);
				try {
					SrlExperiment student = SrlExperiment.parseFrom(req.getOtherData());
					((AnswerConnectionState) state).addPendingExperiment(req.getSessionInfo(), student);
					System.out.println("Student exp " + student);
					internalConnections.send(req, req.getSessionInfo() + "+" + state.getKey(), SolutionConnection.class); // pass submission on

					// request the solution for checking  NOSHIP: need to actually retrieve answer.
					Request.Builder builder = Request.newBuilder();
					builder.setRequestType(MessageType.DATA_REQUEST);
					builder.setSessionInfo(req.getSessionInfo() + "+" + state.getKey());
					ItemRequest.Builder itemRequest = ItemRequest.newBuilder();
					itemRequest.setQuery(ItemQuery.SOLUTION);
					itemRequest.addItemId(student.getProblemId());  // FIXME: this needs to change probably to make this work
					internalConnections.send(builder.setOtherData(itemRequest.build().toByteString()).build(), state.getKey(), SolutionConnection.class);

					return;
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
					// must be an update list!
					internalConnections.send(req, req.getSessionInfo() + "+" + state.getKey(), SolutionConnection.class);
					return;
				}
			} else {
				internalConnections.send(req, req.getSessionInfo(), SolutionConnection.class);
			}
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Answer Server: Version 0.0.1");
		WebSocketImpl.DEBUG = false;
		int port = 8884; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		AnswerCheckerServer s = new AnswerCheckerServer( port );
		s.start();
		System.out.println( "Answer Server started on port: " + s.getPort() );

		System.out.println("Connecting to servers...");
		s.reConnect();

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
				System.out.println("Attempting to recoonect");
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
