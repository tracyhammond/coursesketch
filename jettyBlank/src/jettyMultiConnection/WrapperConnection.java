package jettyMultiConnection;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import multiConnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * Basic Echo Client Socket
 */
@WebSocket(maxIdleTime = 30 * 60 * 60) // 30 minutes
public class WrapperConnection {

	protected MultiInternalConnectionServer parentServer;
	protected MultiConnectionManager parentManager;
	private final CountDownLatch closeLatch;

    private Session session;
    private URI destination;

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

	public WrapperConnection(URI destination) {
    	this.closeLatch = new CountDownLatch(1);
    }

	public void connect() throws Throwable {
		WebSocketClient client = new WebSocketClient();
		try {
			client.start();
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect((Object)this, destination, request);
			System.out.printf("Connecting to : %s%n", destination);
			this.awaitClose(5, TimeUnit.SECONDS);
			} catch (Throwable t) {
				t.printStackTrace();
				throw t;
			} finally {
				try {
					client.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}
	
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
        System.out.println( "Open Wrapper Connection" );
    }

    @OnWebSocketMessage
    public final void onMessage(Session session, byte[] data, int offset, int length) {
    	onMessage(ByteBuffer.wrap(data));
    }

    /**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
    public void onMessage(ByteBuffer buffer) {
    	MultiConnectionState state = getStateFromId(MultiInternalConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());
    	session.getRemote().sendBytesByFuture(buffer);
		getConnectionFromState(state).getRemote().sendBytesByFuture(buffer);
	}

    protected MultiConnectionState getStateFromId(String key) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToState() == null) {
			System.out.println("null getIdToState");
		}
		return parentServer.getIdToState().get(key);
	}

	protected Session getConnectionFromState(MultiConnectionState state) {
		if (parentServer == null) {
			System.out.println("null parent");
		}
		if (parentServer.getIdToConnection() == null) {
			System.out.println("null IdToConnection");
		}
		return parentServer.getIdToConnection().get(state);
	}

	/**
	 * Sends a binary message over the connection
	 * @param buffer
	 */
	public void send(ByteBuffer buffer) {
		session.getRemote().sendBytesByFuture(buffer);
	}

	/**
	 * Sends a binary message over the connection
	 * @param bytes
	 */
	public void send(byte[] bytes) {
		send(ByteBuffer.wrap(bytes));
	}

	public void close() {
		session.close();
	}

	public void close(int statusCode, String args) {
		session.close(statusCode, args);
	}

	/**
	 * Returns a copy of the URI that is also normalized
	 * @return
	 */
	public URI getURI() {
		return destination.normalize();
	}
}
