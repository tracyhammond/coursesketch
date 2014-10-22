package coursesketch.server.base;

import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * Created by gigemjt on 10/19/14.
 */
public final class JettySession implements SocketSession {
    /**
     * A jetty session instance.
     */
    private final Session socketSession;

    /**
     * @param session a jetty session object.
     */
    public JettySession(final Session session) {
        socketSession = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteAddress() {
        return socketSession.getRemoteAddress().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        socketSession.close();
    }

    /**
     * Initiates the asynchronous transmission of a binary message. This method returns before the message is transmitted.
     * Developers may use the returned Future object to track progress of the transmission.
     *
     * @param buffer
     *            the data being sent
     * @return the Future object representing the send operation.
     */
    @Override
    public Future<Void> send(final ByteBuffer buffer) {
        return socketSession.getRemote().sendBytesByFuture(buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(final int statusCode, final String reason) {
        socketSession.close(statusCode, reason);
    }

    /**
     * @param other a different JettySession.
     * @return true if the {@link Session} are equal.
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof JettySession) {
            return socketSession.equals(((JettySession) other).socketSession);
        }
        return false;
    }

    /**
     * @return the hash code of the {@link Session}.
     */
    @Override
    public int hashCode() {
        return socketSession.hashCode();
    }
}
