package connection;

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
import org.java_websocket.framing.Framedata;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemSend;
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

	private boolean connectLocally = MultiConnectionManager.CONNECT_REMOTE;
	UpdateHandler updateHandler = new UpdateHandler();
	MultiConnectionManager internalConnections = new MultiConnectionManager(this);

	static int numberOfConnections = Integer.MIN_VALUE;
	public SubmissionServer(int port, boolean connectLocally) {
		this( new InetSocketAddress( port ), connectLocally );
	}

	public SubmissionServer( InetSocketAddress address, boolean connectLocally ) {
		super( address );
		this.connectLocally = connectLocally;
	}

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
		String sessionInfo = req.getSessionInfo();

		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			try {
				String resultantId = null;
				if (updateHandler.addRequest(req)) {
					System.out.println("Update is finished building!");
					ByteString data = null;
					if (updateHandler.isSolution(sessionInfo)) {
						if (updateHandler.hasSubmissionId(sessionInfo)) {
							resultantId = updateHandler.getSubmissionId(sessionInfo);
							DatabaseClient.updateSubmission(resultantId, updateHandler.getSolution(sessionInfo).getSubmission().getUpdateList());
							return;
						}
						resultantId = DatabaseClient.saveSolution(updateHandler.getSolution(sessionInfo));
						if (resultantId != null) {
							SrlSolution.Builder builder = SrlSolution.newBuilder(updateHandler.getSolution(sessionInfo));
							builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
							data = builder.build().toByteString();
						}
					} else {
						if (updateHandler.hasSubmissionId(sessionInfo)) {
							resultantId = updateHandler.getSubmissionId(sessionInfo);
							System.out.println("I already have an Id " + updateHandler.getSubmissionId(sessionInfo));
							DatabaseClient.updateSubmission(resultantId, updateHandler.getExperiment(sessionInfo).getSubmission().getUpdateList());
							return;
						}
						System.out.println("Saving experiment without an id");
						resultantId = DatabaseClient.saveExperiment(updateHandler.getExperiment(sessionInfo));
						if (resultantId != null) {
							SrlExperiment.Builder builder = SrlExperiment.newBuilder(updateHandler.getExperiment(sessionInfo));
							builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
							data = builder.build().toByteString();
						}
					}
					if (resultantId != null) {
						// it can be null if this solution has already been stored
						Request.Builder build = Request.newBuilder(req);
						build.setResponseText(resultantId);
						build.clearOtherData();
						build.setSessionInfo(req.getSessionInfo());
						System.out.println(req.getSessionInfo());
						// sends the response back to the answer checker which can then send it back to the client.
						conn.send(build.build().toByteArray());
						if (data != null) {
							// passes the data to the database for connecting
							build.setOtherData(data);
							internalConnections.send(build.build(), "", DataConnection.class);
						}
					}
					updateHandler.clearSubmission(req.getSessionInfo());
				} 
				//ItemResult 
			} catch (Exception e) {
				Request.Builder build = Request.newBuilder();
				build.setRequestType(Request.MessageType.ERROR);
				build.setResponseText(e.getMessage());
				build.setSessionInfo(req.getSessionInfo());
				conn.send(build.build().toByteArray());
				e.printStackTrace();
				updateHandler.clearSubmission(req.getSessionInfo());
			}
		}

		if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			DataRequest dataReq;
			try {
				dataReq = DataRequest.parseFrom(req.getOtherData());
				Request.Builder resultReq = Request.newBuilder(req);
				resultReq.clearOtherData();
				for(ItemRequest itemReq: dataReq.getItemsList()) {
					if (itemReq.getQuery() == ItemQuery.EXPERIMENT) {
						SrlExperiment experiment = DatabaseClient.getExperiment(itemReq.getItemId(0));
						ItemSend.Builder send = ItemSend.newBuilder();
						send.setQuery(ItemQuery.EXPERIMENT);
						SrlUpdateList list = SrlUpdateList.parseFrom(experiment.getSubmission().getUpdateList());
						for(SrlUpdate update: list.getListList()) {
							send.setData(update.toByteString());
							resultReq.setOtherData(send.build().toByteString());
							conn.send(resultReq.build().toByteArray());
						}
					}
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
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
		System.out.println("Submission Server: Version 0.0.2.hippo");
		WebSocketImpl.DEBUG = false;

		boolean connectLocal = true;
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
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			} else if( in.equals( "reconnect" ) ) {
				s.reconnect();
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
