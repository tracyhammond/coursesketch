package coursesketch.jetty.multiconnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import interfaces.IClientConnection;
import interfaces.IServerWebSocketHandler;
import interfaces.MultiConnectionState;
import interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;

import connection.CloseableWebsocketClient;
import utilities.ConnectionException;

/**
 * Wraps around a basic client and maintains a sessions to a single server.
 *
 * If The ConnectionWrapper is not connect it will attempt to connect when the
 * next message is sent queuing up messages while nothing is happening. This
 * allows us to have connections that timeout and save bandwidth and wait till
 * they are actually being used. The downside of this is that the first message
 * takes much longer to send.
 */
@WebSocket()
@SuppressWarnings("PMD.TooManyMethods")
public class ClientConnection extends IClientConnection {

    /**
     * The active session of the current connection wrapper.
     */
    private SocketSession session;

    /**
     * True if the object is currently connected to the remote server.
     */
    private boolean connected = false;

    /**
     * True if the wrapper closed due to an EOF and not due to some other
     * reason.
     */
    private boolean reachedEof = false;

    /**
     * True if the connection wrapper has been successfully started yet.
     */
    private boolean started = false;

    /**
     * True if the wrapper's connection was terminated due to a refused
     * connection.
     */
    private boolean refusedConnection = false;

    /**
     * The number of times that the connection has failed to start.
     */
    private int failedStarts = 0;

    /**
     * True if a connection is pending and is queing up messages to be sent when
     * the connection is established.
     */
    private boolean queing = false;

    /**
     * The messages that are queuing up and are waiting to be sent.
     */
    private final List<ByteBuffer> queuedMessages = new ArrayList<ByteBuffer>();

    /**
     * A listener that is called when the socket fails to connect properly.
     */
    private ActionListener socketFailedListener;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ClientConnection#connect()} or call
     * {@link ClientConnection#send(java.nio.ByteBuffer)}.
     *
     * @param iDestination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param iParentServer
     *            The server that is using this connection wrapper.
     */
    public ClientConnection(final URI iDestination, final ServerWebSocketHandler iParentServer) {
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
        try (final CloseableWebsocketClient client = new CloseableWebsocketClient()) {
            client.start();
            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(this, this.getURI(), request);
        } catch (IOException e) {
            e.printStackTrace();
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
     * @see {@link ClientConnection#onMessage(ByteBuffer)}
     */
    @OnWebSocketMessage
    public final void jettyOnMessage(final byte[] data, final int offset, final int length) {
        onMessage(ByteBuffer.wrap(data, offset, length));
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
        final MultiConnectionState state = getStateFromId(IServerWebSocketHandler.Decoder.parseRequest(buffer).getSessionInfo());
        session.send(buffer);
        getConnectionFromState(state).send(buffer);
    }

    /**
     * Sends a binary message over the connection.
     *
     * If the connection fails then a reconnect is attempted. If the attempt
     * fails more than {@link ClientConnection#MAX_FAILED_STARTS} times then an
     * exception is thrown
     *
     * @param buffer
     *            The binary message that is being sent out.
     * @throws ConnectionException
     *             Thrown if the number of connection attempts exceeds
     *             {@link ClientConnection#MAX_FAILED_STARTS}
     */
    @Override
    public final void send(final ByteBuffer buffer) throws ConnectionException {
        if (connected) {
            connectedSend(buffer);
        } else if ((started || reachedEof || refusedConnection) && failedStarts < MAX_FAILED_STARTS) {
            connectionEndedSend(buffer);
        } else if (failedStarts < MAX_FAILED_STARTS) {
            connectionNotEstablishedSend(buffer);
        } else if (failedStarts >= MAX_FAILED_STARTS) {
            queing = false;
            System.out.println(failedStarts);
            System.out.println(MAX_FAILED_STARTS);
            // adds this version because it has not been added before
            queuedMessages.add(buffer);
            System.err.println(this.getClass().getSimpleName() + " failed to connect after multiple tries");
            socketFailedListener.actionPerformed(new ActionEvent(queuedMessages, 0, this.getClass().getName()));
            // all messages are empty after the actions with the message are finished.
            queuedMessages.clear();
            throw new ConnectionException("" + this.getClass().getSimpleName() + " failed to connect after multiple tries");
        }
    }

    /**
     * Called if the connection is connected and working properly.
     * @param buffer The message that is trying to be sent.
     */
    private void connectedSend(final ByteBuffer buffer) {
        System.out.println("Sending message from: " + this.getClass().getSimpleName());
        session.send(buffer);
        if (queing) {
            while (!queuedMessages.isEmpty()) {
                session.send(queuedMessages.remove(0));
            }
            queing = false;
        }
    }

    /**
     * This is called when trying to send a message but it fails because a connection has not yet been established.
     * @param buffer The message that is trying to be sent.
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    private void connectionNotEstablishedSend(final ByteBuffer buffer) {
        System.err.println("Trying to wait on " + this.getClass().getSimpleName() + ". It has not connected yet.");
        if (!queing) {
            queing = true;
            failedStarts += 1;
            System.err.println("attempt " + failedStarts + " out of " + MAX_FAILED_STARTS);
            final Thread retryThread = new Thread() {
                @Override
                @SuppressWarnings("PMD.CommentRequired")
                public void run() {
                    try {
                        Thread.sleep(MIN_SLEEP_TIME);
                        queing = false;
                        send(buffer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }
            };
            retryThread.start();
        } else {
            System.err.println("Adding a queuedMessage");
            queuedMessages.add(buffer);
        }
    }

    /**
     * This is called when trying to send a message but it fails because the connection was terminated.
     * This could because a connection was refused or because the connection timed out.
     * @param buffer The message that is trying to be sent.
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    private void connectionEndedSend(final ByteBuffer buffer) {
        final String endReson = reachedEof || started ? "of a timeout" : refusedConnection ? "connection to the server was refused"
                : "We do not know why";
        System.err.println("Trying to reconnect " + this.getClass().getSimpleName() + ". It has ended because " + endReson);
        if (!queing) {
            failedStarts += 1;
            System.err.println("attempt " + failedStarts + " out of " + MAX_FAILED_STARTS);
            queing = true;
            // maybe try reconnecting here?
            final Thread retryThread = new Thread() {
                @Override
                @SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidCatchingGenericException" })
                public void run() {
                    try {
                        connect();
                        Thread.sleep(MIN_SLEEP_TIME);
                        queing = false;
                        send(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            retryThread.start();
        } else {
            System.err.println("Adding a queuedMessage");
            queuedMessages.add(buffer);
        }
    }
}
