package jettyMultiConnection;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

/**
 * Basic Echo Client Socket
 */
@WebSocket(maxIdleTime = 30 * 60 * 60) // 30 minutes
public class WrapperConnection {

	private final CountDownLatch closeLatch;

    private Session session;
 
	public WrapperConnection() {
    	this.closeLatch = new CountDownLatch(1);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte[] data, int offset, int length) {
    	onMessage(ByteBuffer.wrap(data));
    }

    /**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
    public void onMessage(ByteBuffer buffer) {
	}
}
