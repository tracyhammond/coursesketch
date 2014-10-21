package coursesketch.jetty.multiconnection;

import java.nio.ByteBuffer;

import interfaces.AbstractServerWebSocketHandler;
import interfaces.ISocketInitializer;
import interfaces.MultiConnectionManager;
import interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;

/**
 * A connection server.
 * @author gigemjt
 */
@WebSocket()
@SuppressWarnings("PMD.TooManyMethods")
public class ServerWebSocketHandler extends AbstractServerWebSocketHandler {

    /**
     * A constructor that accepts a servlet.
     * @param parent The parent servlet of this server.
     */
    public ServerWebSocketHandler(final ISocketInitializer parent) {
        super(parent);
    }

    /**
     * Called when the connection is closed.
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    @OnWebSocketClose
    public final void jettyClose(final Session conn, final int statusCode, final String reason) {
        onClose(new JettySession(conn), statusCode, reason);
    }

    /**
     * Called every time the connection is formed.
     *
     * @param conn The connection that is being opened.
     */
    @OnWebSocketConnect
    public final void jettyOpen(final Session conn) {
        onOpen(new JettySession(conn));
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn the connection that is being opened.
     */
    protected void openSession(final SocketSession conn) {
        // Does nothing by default.
    }

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    @OnWebSocketError
    @SuppressWarnings("static-method")
    public final void jettyError(final Session session, final Throwable cause) {
        onError(new JettySession(session), cause);
    }

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    @Override
    public final void onError(final SocketSession session, final Throwable cause) {
        System.err.println("Session: " + session.getRemoteAddress() + "\ncaused:" + cause);
    }

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param data The bytes that sent the message.
     * @param offset The offset at which the message occurs.
     * @param length The length of the message.
     */
    @OnWebSocketMessage
    public final void jettyOnMessage(final Session session, final byte[] data, final int offset, final int length) {
        onMessage(new JettySession(session), ByteBuffer.wrap(data, offset, length));
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * By default it is an echo server, basically it echos what it receives.
     *
     * @param session
     *            the session object that created the message
     * @param req
     *            the message itself
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public void onMessage(final SocketSession session, final Request req) {
        send(session, req);
    }

    /**
     * A helper method for sending data given a session.
     * This is a non-blocking way to send messages to a server.
     *
     * @param session The session that the message is being sent with.
     * @param req The actual message that is being sent.
     */
    @Override
    public final void send(final SocketSession session, final Request req) {
        session.send(ByteBuffer.wrap(req.toByteArray()));
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    public void onStop() {
        // Does nothing by default.
    }

    /**
     * @return The {@link MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected final MultiConnectionManager getConnectionManager() {
        return ((ServerWebSocketInitializer) getParentServer()).getManager();
    }
}
