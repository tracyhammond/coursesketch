package multiConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import protobuf.srl.request.Message.Request;

import com.google.protobuf.InvalidProtocolBufferException;

public abstract class MultiInternalConnectionServer extends WebSocketServer {
	public static final int MAX_CONNECTIONS = 80;
	public static final int STATE_SERVER_FULL = 4001;
	static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";

	protected HashMap<WebSocket, MultiConnectionState> connectionToId = new HashMap<WebSocket, MultiConnectionState>();
	protected HashMap<MultiConnectionState, WebSocket> idToConnection = new HashMap<MultiConnectionState, WebSocket>();
	protected HashMap<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();

	public MultiInternalConnectionServer( InetSocketAddress address ) {
		super( address );
	}

	public MultiInternalConnectionServer( int port ) {
		this( new InetSocketAddress( port ) );
	}

	/**
	 * When a new websocket connects to the server this is called.
	 *
	 * This method adds a connection to a list so that it can be recalled later if needed.
	 */
	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		if (connectionToId.size() >= MAX_CONNECTIONS) {
			// Return negatative state.
			conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
			System.out.println("FULL SERVER"); // send message to someone?
			return;
		}
		MultiConnectionState id = getUniqueState();
		connectionToId.put(conn, id);
		getIdToConnection().put(id, conn);
		System.out.println("Session Key " + id.getKey());
		getIdToState().put(id.getKey(), id);
		System.out.println("ID ASSIGNED");
	}

	/**
	 * Returns a new connection with an id.
	 *
	 * This can be overwritten to make a more advance connection.
	 * This is only called in {@link MultiInternalConnectionServer#onOpen(WebSocket, ClientHandshake)}
	 */
	public MultiConnectionState getUniqueState() {
		return new MultiConnectionState(Encoder.nextID().toString());
	}

	/**
	 * Removes the connections and id from the map
	 */
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has disconnected from The Server." +
				(remote ? "The connection was closed remotely" : "we closed the connection"));
		MultiConnectionState id = connectionToId.remove(conn);
		if (id != null) {
			idToConnection.remove(id);
			idToState.remove(id.getKey());
		} else {
			System.out.println("Connection Id can not be found");
		}
	}

	/**
	 * This is called when the reconnect command is executed.
	 *
	 * By default this command does nothing.
	 */
	public abstract void reconnect();

	/**
	 * Handles commands that can be used to perform certain functionality.
	 *
	 * This method can and in some cases should be overwritten.
	 * We <b>strongly</b> suggest that you call super first then check to see if it is true and then call your overwritten method.
	 * @param command The command that is parsed to provide functionality.
	 * @param sysin Used if additional input is needed for the command.
	 * @return true if the command is an accepted command and is used by the server
	 * @throws Exception 
	 */
	public boolean parseCommand(String command, BufferedReader sysin) throws Exception {
		if (command.equals( "exit" )) {
			System.out.println("Are you sure you want to exit? [y/n]");
			if (sysin.readLine().equalsIgnoreCase("y")) {
				this.stop();
				// TODO: prompt for confirmation!
				System.exit(0);
			}
			return true;
		} else if (command.equals("restart")) {
			throw new Exception("This command is not yet supported");
		} else if (command.equals("reconnect")) {
			this.reconnect();
			return true;
		}
		return false;
	}

	/**
	 * Overwritten to prevent clutter in the clients!
	 */
	@Override
	public void onMessage( WebSocket conn, String message ) {
	}

	protected HashMap<String, MultiConnectionState> getIdToState() {
		return idToState;
	}

	protected HashMap<MultiConnectionState, WebSocket> getIdToConnection() {
		return idToConnection;
	}

	public static final class Decoder {
		/**
		 * Returns a {@link Request} as it is parsed from the ByteBuffer.
		 *
		 * Returns null if the ByteBuffer does not exist.
		 * @param buffer
		 * @return
		 */
		public static Request parseRequest(ByteBuffer buffer) {
			try {
				return Request.parseFrom(buffer.array());
			} catch (InvalidProtocolBufferException e) {
				//e.printStackTrace();
				return null;
			}
		}
	}

	public static final class Encoder {
		/**
		 * counter will be incremented by 0x10000 for each new SComponent that is
		 * created counter is used as the most significant bits of the UUID
		 * 
		 * initialized to 0x4000 (the version -- 4: randomly generated UUID) along
		 * with 3 bytes of randomness: Math.random()*0x1000 (0x0 - 0xFFF)
		 * 
		 * the randomness further reduces the chances of collision between multiple
		 * sketches created on multiple computers simultaneously
		 * 
		 * (taken from SCComponent)
		 */
		public static long counter = 0x4000L | (long) (Math.random() * 0x1000);

		/**
		 * Returns a {@link Request} that contains the sessionInfo and the time that the message was sent.
		 *
		 * Returns itself if the sessionInfo is null.
		 */
		public static Request requestIDBuilder(Request req, String sessionInfo) {
			if (sessionInfo == null) 
				return req;
			Request.Builder breq = Request.newBuilder();
			breq.mergeFrom(req);
			breq.setSessionInfo(sessionInfo);
			if (!breq.hasMessageTime()) {
				breq.setMessageTime(System.currentTimeMillis());
			}
			return breq.build();
		}

		public static UUID nextID() {
			counter += 0x10000L; // Overflow is perfectly fine.
			return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
		}
	}
}
