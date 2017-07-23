package coursesketch.server.base;

import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;
import utilities.LoggingConstants;

import java.nio.ByteBuffer;

/**
 * A connection server.
 * @author gigemjt
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
@SuppressWarnings("PMD.TooManyMethods")
public class ServerWebSocketHandler extends AbstractServerWebSocketHandler {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent The parent servlet of this server.
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public ServerWebSocketHandler(final ISocketInitializer parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
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
        LOG.error("Session: {} \n caused: {}", session.getRemoteAddress(), cause);
    }

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param data The bytes that sent the message.
     * @param offset The offset at which the message occurs.
     * @param length The length of the message.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @OnWebSocketMessage
    public final void jettyOnMessage(final Session session, final byte[] data, final int offset, final int length) {
        try {
            onMessage(new JettySession(session), ByteBuffer.wrap(data, offset, length));
        } catch (Exception e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * By default it is an echo server, basically it echos what it receives.
     *
     * @param session
     *            The session object that created the message.
     * @param req
     *            The message itself.
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public void onMessage(final SocketSession session, final Request req) {
        send(session, req);
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    public void onStop() {
        // Does nothing by default.
    }

    /**
     * {@inheritDoc}
     */
    protected final MultiConnectionManager getConnectionManager() {
        return ((ServerWebSocketInitializer) getParentServer()).getManager();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override protected AbstractCourseSketchDatabaseReader createDatabaseReader(final ServerInfo info) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override protected void onInitialize() {
        // Does nothing by default
    }
}
