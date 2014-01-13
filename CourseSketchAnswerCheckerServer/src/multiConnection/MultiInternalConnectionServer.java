package multiConnection;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

	List<WebSocket> connections = new LinkedList<WebSocket>();
	protected HashMap<WebSocket, MultiConnectionState> connectionToId = new HashMap<WebSocket, MultiConnectionState>();
	protected HashMap<MultiConnectionState, WebSocket> idToConnection = new HashMap<MultiConnectionState, WebSocket>();
	protected HashMap<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();

	public MultiInternalConnectionServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

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
	 * Returns a number that should be unique.
	 */
	public MultiConnectionState getUniqueState() {
		return new MultiConnectionState(Encoder.nextID().toString());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " has disconnected from Recognition.");
		connections.remove(conn);
		MultiConnectionState id = connectionToId.remove(conn);
		idToConnection.remove(id);
		idToState.remove(id.getKey());
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
	}

	public MultiInternalConnectionServer( InetSocketAddress address ) {
		super( address );
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
		 * Returns a {@link Request} that contains the sessionInfo.
		 *
		 * Returns itself if the sessionInfo is null.
		 */
		public static Request requestIDBuilder(Request req, String sessionInfo) {
			if (sessionInfo == null) 
				return req;
			Request.Builder breq = Request.newBuilder();
			breq.mergeFrom(req);
			breq.setSessionInfo(sessionInfo);
			return breq.build();
		}

		public static UUID nextID() {
			counter += 0x10000L; // Overflow is perfectly fine.
			return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
		}
	}
}
