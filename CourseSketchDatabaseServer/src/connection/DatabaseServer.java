package connection;

import handlers.DataInsertHandler;
import handlers.DataRequestHandler;
import handlers.UpdateHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiConnectionState;
import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.joda.time.DateTime;

import protobuf.srl.request.Message.Request;
import database.institution.Institution;
import database.user.UserClient;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class DatabaseServer extends MultiInternalConnectionServer {

	private boolean connectLocally = MultiConnectionManager.CONNECT_REMOTE;
	UpdateHandler updateHandler = new UpdateHandler();
	MultiConnectionManager internalConnections = new DatabaseConnectionManager(this);

	public DatabaseServer(int port, boolean connectLocally) {
		this( new InetSocketAddress( port ), connectLocally );
	}

	public DatabaseServer( InetSocketAddress address, boolean connectLocally ) {
		super( address );
		this.connectLocally = connectLocally;
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		super.onOpen(conn, handshake);
		System.out.println("Sending Time");
		System.out.println("Time:"+DateTime.now().getMillis());
		conn.send(TimeManager.serverSendTimeToClient().toByteArray());
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
			System.out.println("Submitting submission id");
			Institution.mongoInsertSubmission(req);
		} else if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			DataRequestHandler.handleRequest(req, conn, super.connectionToId.get(conn).getKey(), internalConnections);
		} else if (req.getRequestType() == Request.MessageType.DATA_INSERT) {
			DataInsertHandler.handleData(req, conn);
		} else if (req.getRequestType() == Request.MessageType.TIME) {
			Request rsp = TimeManager.decodeRequest(req);
			if (rsp != null) conn.send(rsp.toByteArray());
		}
		System.out.println("Finished looking at query");
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	@Override
	public void reconnect() {
		internalConnections.dropAllConnection(true, false);
		try {
			internalConnections.createAndAddConnection(this, connectLocally, "srl02.tamu.edu", 8883, SubmissionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Database Server: Version 1.0.3");
		WebSocketImpl.DEBUG = false;

		boolean connectLocal = false;
		if (args.length == 1) {
			if (args[0].equals("local")) {
				connectLocal = true;
				new Institution(false); // makes the database point locally
				new UserClient(false); // makes the database point locally
			}
		}

		int port = 8885; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		DatabaseServer s = new DatabaseServer( port, connectLocal);
		s.start();
		s.reconnect();
		System.out.println( "Database Server started on port: " + s.getPort() );

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
