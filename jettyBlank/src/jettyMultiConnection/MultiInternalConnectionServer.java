package jettyMultiConnection;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import multiConnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import protobuf.srl.request.Message.Request;

import com.google.protobuf.InvalidProtocolBufferException;

@WebSocket(maxIdleTime = 30 * 60 * 60) // 30 minutes
public class MultiInternalConnectionServer {
	
	public static final int MAX_CONNECTIONS = 80;
	public static final int STATE_SERVER_FULL = 4001;
	static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";

	protected HashMap<Session, MultiConnectionState> connectionToId = new HashMap<Session, MultiConnectionState>();
	protected HashMap<MultiConnectionState, Session> idToConnection = new HashMap<MultiConnectionState, Session>();
	protected HashMap<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();
	protected MainServlet parentServer = null;
	
	public MultiInternalConnectionServer(MainServlet parent) {
		parentServer = parent;
    }

    @OnWebSocketClose
    public void onClose(Session conn, int statusCode, String reason) {
    	System.out.println( conn + " has disconnected from The Server." +
				(true ? "The connection was closed remotely" : "we closed the connection")); // TODO: find out how to see if the connection is closed by us or them.
		MultiConnectionState id = connectionToId.remove(conn);
		if (id != null) {
			idToConnection.remove(id);
			idToState.remove(id.getKey());
		} else {
			System.err.println("Connection Id can not be found");
		}
    }

    /**
     * Called every time the connection is formed.
     * @param conn
     */
    @OnWebSocketConnect
    public void onOpen(Session conn) {
    	if (connectionToId.size() >= MAX_CONNECTIONS) {
			// Return negatative state.
			System.out.println("FULL SERVER"); // send message to someone?
			conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
		}

		MultiConnectionState id = getUniqueState();
		connectionToId.put(conn, id);
		getIdToConnection().put(id, conn);
		System.out.println("Session Key " + id.getKey());
		getIdToState().put(id.getKey(), id);
		System.out.println("ID ASSIGNED");

		System.out.println("Recieving connection " + connectionToId.size());
    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
    	System.err.println(cause);
    }

    @OnWebSocketMessage
    public final void onMessage(Session session, byte[] data, int offset, int length) {
    	onMessage(session, ByteBuffer.wrap(data));
    }

    /**
	 * A blank binary onMessage called every time data is sent.
	 */
    public void onMessage(Session session, ByteBuffer buffer) {
	}

    final void stop() {
    	for (Session sesh : connectionToId.keySet()) {
    		sesh.close();
    	}
    	connectionToId.clear();
    	idToConnection.clear();
    	idToState.clear();
    }

    /**
     * Available for people to call
     */
    public void onStop() {

    }

    /**
	 * Returns a new connection with an id.
	 *
	 * This can be overwritten to make a more advance connection.
	 * This is only called in {@link MultiInternalConnectionServer#onOpen(WebSocket, ClientHandshake)}
	 */
	@SuppressWarnings("static-method")
	public MultiConnectionState getUniqueState() {
		return new MultiConnectionState(Encoder.nextID().toString());
	}

	protected HashMap<String, MultiConnectionState> getIdToState() {
		return idToState;
	}

	protected HashMap<MultiConnectionState, Session> getIdToConnection() {
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
