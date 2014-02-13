package connection;

import internalConnection.AnswerConnectionManager;
import internalConnection.AnswerConnectionState;
import internalConnection.SolutionConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
public class AnswerCheckerServer extends MultiInternalConnectionServer {

	List<WebSocket> connections = new LinkedList<WebSocket>();
	AnswerConnectionManager internalConnections;
	// has some sort of client!

	static int numberOfConnections = Integer.MIN_VALUE;
	public AnswerCheckerServer(int port, boolean connectLocally) {
		this( new InetSocketAddress( port ), connectLocally );
	}

	public AnswerCheckerServer( InetSocketAddress address, boolean connectLocally ) {
		super( address );
		internalConnections = new AnswerConnectionManager(this, connectLocally);
		//internalConnections.createAndAddConnection(this, false, 9000, SolutionConnection.class);
	}

	/**
	 * Returns a number that should be unique.
	 */
	public AnswerConnectionState getUniqueState() {
		return new AnswerConnectionState(Encoder.nextID().toString());
	}

	@Override
	public void reconnect() {
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
					SrlUpdate update = SrlUpdate.parseFrom(req.getOtherData());
					System.out.println("Parsing as an update");
					internalConnections.send(req, req.getSessionInfo() + "+" + state.getKey(), SolutionConnection.class);
					return;
				} catch (InvalidProtocolBufferException e) {
					System.out.println("Parsing as an experiment");
					SrlExperiment student = null;
					try {
						student = SrlExperiment.parseFrom(req.getOtherData());
					} catch (InvalidProtocolBufferException e1) {
						e1.printStackTrace();
					}
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
					//internalConnections.send(builder.setOtherData(itemRequest.build().toByteString()).build(), state.getKey(), SolutionConnection.class);
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
		boolean connectLocal = false;
		if (args.length == 1) {
			if (args[0].equals("local")) {
				connectLocal = MultiConnectionManager.CONNECT_LOCALLY;
			}
		}

		int port = 8884; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		AnswerCheckerServer s = new AnswerCheckerServer( port, connectLocal );
		s.start();
		System.out.println( "Answer Server started on port: " + s.getPort() );

		System.out.println("Connecting to servers...");
		s.reconnect();

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			try {
				s.parseCommand(in, sysin);
			} catch (Exception e) {
				e.printStackTrace();
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
