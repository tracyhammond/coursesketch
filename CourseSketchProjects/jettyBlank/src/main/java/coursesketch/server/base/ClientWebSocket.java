package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import utilities.ConnectionException;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * Wraps around a basic client and maintains a sessions to a single server.
 *
 * If The ConnectionWrapper is not connect it will attempt to connect when the
 * next message is sent queuing up messages while nothing is happening. This
 * allows us to have connections that timeout and save bandwidth and wait till
 * they are actually being used. The downside of this is that the first message
 * takes much longer to send.
 */
@SuppressWarnings("PMD.TooManyMethods")
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public class ClientWebSocket extends AbstractClientWebSocket {

    /**
     * Declaration/Definition of Logger.
     */

    private static final Logger LOG = LoggerFactory.getLogger(ClientWebSocket.class);

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ClientWebSocket#connect()} or call
     * {@link ClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param iDestination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param iParentServer
     *            The server that is using this connection wrapper.
     */
    public ClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws ConnectionException Throws an exception if an error occurs during the connection attempt.
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public final void connect() throws ConnectionException {
        try {
            final CloseableWebSocketClient client = new CloseableWebSocketClient();
            client.start();
            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.setMaxBinaryMessageBufferSize(AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE);
            client.connect(this, this.getURI(), request);
        } catch (IOException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            throw new ConnectionException("an exception connecting", e);
        } catch (Exception e) {
            throw new ConnectionException("something went wrong when starting the client", e);
        }
    }

    /**
     * Called when the connection is closed.
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    @OnWebSocketClose
    public final void jettyClose(final int statusCode, final String reason) {
        onClose(statusCode, reason);
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
     * Called when a message occurs and then wraps the message in a {@link ByteBuffer}.
     * @param data The actual bytes that contain the message.
     * @param offset The offset at which the message occurs.
     * @param length The length of the message itself.
     * @see {@link ClientWebSocket#onMessage(ByteBuffer)}
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @OnWebSocketMessage
    public final void jettyOnMessage(final byte[] data, final int offset, final int length) {
        try {
            onMessage(ByteBuffer.wrap(data, offset, length));
        } catch (Exception e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE);
        }
    }

    /**
     * Called when an error occurs.
     *
     * @param erroredSession
     *            The session with which the error occurred. This may not be the
     *            same session that is held by this object if the connection has
     *            not opened yet.
     * @param cause
     *            A {@link Throwable} representing the actual error that occurred.
     */
    @OnWebSocketError
    @SuppressWarnings("static-method")
    public final void jettyError(final Session erroredSession, final Throwable cause) {
        onError(new JettySession(erroredSession), cause);
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer The message that is received by this object.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void onMessage(final ByteBuffer buffer) {
        final MultiConnectionState state = getStateFromId(AbstractServerWebSocketHandler.Decoder.parseRequest(buffer).getSessionInfo());
        getConnectionFromState(state).send(buffer);
    }
}
