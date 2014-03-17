package connection;

import handlers.DataRequestHandler;
import handlers.SubmissionRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSolution;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import database.DatabaseClient;
import database.UpdateHandler;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
public class SubmissionServer extends MultiInternalConnectionServer {

	private boolean connectLocally = MultiConnectionManager.CONNECT_REMOTE;
	
	MultiConnectionManager internalConnections = new MultiConnectionManager(this);

	static int numberOfConnections = Integer.MIN_VALUE;
	public SubmissionServer(int port, boolean connectLocally) {
		this( new InetSocketAddress( port ), connectLocally );
	}

	public SubmissionServer( InetSocketAddress address, boolean connectLocally ) {
		super( address );
		this.connectLocally = connectLocally;
	}

	@Override
	public void reconnect() {
		internalConnections.dropAllConnection(true, false);
		try {
			internalConnections.createAndAddConnection(this, connectLocally, "srl04.tamu.edu", 8885, DataConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);

		/**
		 * Attempts to save the submission, which can be either a solution or an experiment.
		 * If it is an insertion and not an update then it will send the key to the database
		 */
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			Request result = SubmissionRequestHandler.handleRequest(req, internalConnections);
			if (result != null) {
				conn.send(result.toByteArray());
			}
		}

		if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			Request result = DataRequestHandler.handleRequest(req);
			if (result != null) {
				conn.send(result.toByteArray());
			}
		}
	}

	public static void main( String[] args ) throws IOException {
		System.out.println("Submission Server: Version 0.0.1");
		WebSocketImpl.DEBUG = false;

		boolean connectLocal = false;
		if (args.length == 1) {
			if (args[0].equals("local")) {
				connectLocal = MultiConnectionManager.CONNECT_LOCALLY;
				new DatabaseClient(false); // makes the database point locally
			}
		}

		int port = 8883; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		SubmissionServer s = new SubmissionServer( port, connectLocal );
		s.start();
		s.reconnect();
		System.out.println( "Submission Server started on port: " + s.getPort() );

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
