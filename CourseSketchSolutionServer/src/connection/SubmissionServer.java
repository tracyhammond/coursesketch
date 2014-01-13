package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;

import protobuf.srl.request.Message.Request;
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

	UpdateHandler updateHandler = new UpdateHandler();
	MultiConnectionManager internalConnections = new MultiConnectionManager(this);

	static int numberOfConnections = Integer.MIN_VALUE;
	public SubmissionServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public SubmissionServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);
		String sessionInfo = req.getSessionInfo();
		if (req == null) {
			System.out.println("protobuf error");
			return;
		}

		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			try {
				String resultantId = null;
				if (updateHandler.addRequest(req)) {
					System.out.println("Update is finished building!");
					ByteString data = null;
					if (updateHandler.isSolution(sessionInfo)) {
						if (updateHandler.hasSubmissionId(sessionInfo)) {
							resultantId = updateHandler.getSubmissionId(sessionInfo);
							DatabaseClient.updateSolution(resultantId);
						} else {
							resultantId = DatabaseClient.saveSolution(updateHandler.getSolution(sessionInfo));
							if (resultantId != null) {
								SrlSolution.Builder builder = SrlSolution.newBuilder(updateHandler.getSolution(sessionInfo));
								builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
								data = builder.build().toByteString();
							}
						}
					} else {
						if (updateHandler.hasSubmissionId(sessionInfo)) {
							resultantId = updateHandler.getSubmissionId(sessionInfo);
							DatabaseClient.updateExperiment(resultantId);
						} else {
							System.out.println("Saving experiment");
							resultantId = DatabaseClient.saveExperiment(updateHandler.getExperiment(sessionInfo));
							if (resultantId != null) {
								SrlExperiment.Builder builder = SrlExperiment.newBuilder(updateHandler.getExperiment(sessionInfo));
								builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
								data = builder.build().toByteString();
							}
						}
					}
					if (resultantId != null) {
						// it can be null if this solution has already been stored
						Request.Builder build = Request.newBuilder(req);
						build.setResponseText(resultantId);
						build.clearOtherData();
						conn.send(build.build().toByteArray());
						if (data != null) {
							// passes the data to the database for connecting
							build.setOtherData(data);
							internalConnections.send(build.build(), "", DataConnection.class);
						}
					}
				} 
				//ItemResult 
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			//SrlSolution solution = storage.getSolution();// something we send in.
			//Request.Builder build = Request.newBuilder(req);
			//build.setOtherData(solution.toByteString());
			//conn.send(build.build().toByteArray());
		}
		// CODE GOES HERE
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Submission Server: Version 0.0.2.ant");
		WebSocketImpl.DEBUG = false;
		int port = 8883; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		SubmissionServer s = new SubmissionServer( port );
		s.start();
		System.out.println( "Submission Server started on port: " + s.getPort() );

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
