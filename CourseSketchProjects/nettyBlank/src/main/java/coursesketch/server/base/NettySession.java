package coursesketch.server.base;

import coursesketch.server.interfaces.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import protobuf.srl.request.Message;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * Created by gigemjt on 10/19/14.
 */
public final class NettySession implements SocketSession {
    private final ChannelHandlerContext session;
    public NettySession(final ChannelHandlerContext context) {
        this.session = context;
    }

    /**
     * Get the address of the remote side.
     *
     * @return the remote side address
     */
    @Override
    public String getRemoteAddress() {
        return session.channel().remoteAddress().toString();
    }

    /**
     * Request a close of the current conversation with a normal status code and no reason phrase.
     * <p/>
     * This will enqueue a graceful close to the remote endpoint.
     *
     * @see #close(int, String)
     */
    @Override
    public void close() {
        session.channel().close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Void> send(final Message.Request req) {
        return send(ByteBuffer.wrap(req.toByteArray()));
    }

    /**
     * Initiates the asynchronous transmission of a binary message. This method returns before the message is transmitted.
     * Developers may use the returned Future object to track progress of the transmission.
     *
     * @param buffer the data being sent
     * @return the Future object representing the send operation.
     */
    @Override
    public Future<Void> send(final ByteBuffer buffer) {
        return null;
    }

    /**
     * Send a websocket Close frame, with status code.
     * <p/>
     * This will enqueue a graceful close to the remote endpoint.
     *
     * @param statusCode the status code
     * @param reason     the (optional) reason. (can be null for no reason)
     * @see #close()
     */
    @Override
    public void close(int statusCode, String reason) {
        session.channel().close();
    }

    /**
     * @param other a different JettySession.
     * @return true if the {@link org.eclipse.jetty.websocket.api.Session} are equal.
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof NettySession) {
            return session.equals(((NettySession) other).session);
        }
        return false;
    }

    /**
     * @return the hash code of the {@link org.eclipse.jetty.websocket.api.Session}.
     */
    @Override
    public int hashCode() {
        return session.hashCode();
    }
}
