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
		String sessionInfo = req.getSessionInfo();

		/**
		 * Attempts to save the submission, which can be either a solution or an experiment.
		 * If it is an insertion and not an update then it will send the key to the database
		 */
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			try {
				String resultantId = null;
				if (updateHandler.addRequest(req)) {
					System.out.println("Update is finished building!");
					ByteString data = null;
					if (updateHandler.isSolution(sessionInfo)) {
						resultantId = DatabaseClient.saveSolution(updateHandler.getSolution(sessionInfo));
						if (resultantId != null) {
							SrlSolution.Builder builder = SrlSolution.newBuilder(updateHandler.getSolution(sessionInfo));
							builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
							data = builder.build().toByteString();
						}
					} else {
						System.out.println("Saving experiment");
						resultantId = DatabaseClient.saveExperiment(updateHandler.getExperiment(sessionInfo));
						if (resultantId != null) {
							SrlExperiment.Builder builder = SrlExperiment.newBuilder(updateHandler.getExperiment(sessionInfo));
							builder.setSubmission(SrlSubmission.newBuilder().setId(resultantId));
							data = builder.build().toByteString();
						}
					}
					Request.Builder build = Request.newBuilder(req);
					build.setResponseText("Submission Succesful!");
					build.clearOtherData();
					build.setSessionInfo(req.getSessionInfo());
					System.out.println(req.getSessionInfo());
					// sends the response back to the answer checker which can then send it back to the client.
					conn.send(build.build().toByteArray());
					if (resultantId != null) {
						// it can be null if this solution has already been stored

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
			System.out.println("Parsing data request!");
			DataRequest dataReq;
			try {
				dataReq = DataRequest.parseFrom(req.getOtherData());
				Request.Builder resultReq = Request.newBuilder(req);
				resultReq.clearOtherData();
				for(ItemRequest itemReq: dataReq.getItemsList()) {
					if (itemReq.getQuery() == ItemQuery.EXPERIMENT) {
						System.out.println("attempting to get an experiment!");
						SrlExperiment experiment = null;
						String errorMessage = "";
						try {
							experiment = DatabaseClient.getExperiment(itemReq.getItemId(0));
						} catch (Exception e) {
							errorMessage = e.getMessage();
							e.printStackTrace();
						}
						DataResult.Builder builder = DataResult.newBuilder();
						ItemResult.Builder send = ItemResult.newBuilder();
						send.setQuery(ItemQuery.EXPERIMENT);

						if (experiment != null) {
							send.setData(experiment.toByteString());
						} else {
							send.setNoData(true);
							send.setErrorMessage(errorMessage);
							//error stuff
						}
						builder.addResults(send);

						resultReq.setOtherData(builder.build().toByteString());
						resultReq.setRequestType(MessageType.DATA_REQUEST);
						conn.send(resultReq.build().toByteArray());

						/*
						SrlUpdateList list = SrlUpdateList.parseFrom(experiment.getSubmission().getUpdateList());
						for(SrlUpdate update: list.getListList()) {
							send.setData(update.toByteString());
							resultReq.setOtherData(send.build().toByteString());
							conn.send(resultReq.build().toByteArray());
						}
						*/
					}
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
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
