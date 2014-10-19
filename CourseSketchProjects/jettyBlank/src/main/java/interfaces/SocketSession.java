package interfaces;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * Created by gigemjt on 10/19/14.
 */
public interface SocketSession  extends Closeable {
    /**
     * Get the address of the remote side.
     *
     * @return the remote side address
     */
    String getRemoteAddress();

    /**
     * Request a close of the current conversation with a normal status code and no reason phrase.
     * <p>
     * This will enqueue a graceful close to the remote endpoint.
     *
     * @see #close(int, String)
     */
    void close();

    /**
     * Initiates the asynchronous transmission of a binary message. This method returns before the message is transmitted.
     * Developers may use the returned Future object to track progress of the transmission.
     *
     * @param buffer
     *            the data being sent
     * @return the Future object representing the send operation.
     */
    Future<Void> send(ByteBuffer buffer);

    /**
     * Send a websocket Close frame, with status code.
     * <p>
     * This will enqueue a graceful close to the remote endpoint.
     *
     * @param statusCode
     *            the status code
     * @param reason
     *            the (optional) reason. (can be null for no reason)
     *
     * @see #close()
     */
    void close(int statusCode, String reason);
}
