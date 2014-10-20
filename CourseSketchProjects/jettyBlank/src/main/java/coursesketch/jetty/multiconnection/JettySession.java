package coursesketch.jetty.multiconnection;

import interfaces.SocketSession;
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

    @Override
    public String getRemoteAddress() {
        return socketSession.getRemoteAddress().toString();
    }

    @Override
    public void close() {
        socketSession.close();
    }


    @Override
    public Future<Void> send(final ByteBuffer buffer) {
        return socketSession.getRemote().sendBytesByFuture(buffer);
    }

    @Override
    public void close(final int statusCode, final String args) {
        socketSession.close(statusCode, args);
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
