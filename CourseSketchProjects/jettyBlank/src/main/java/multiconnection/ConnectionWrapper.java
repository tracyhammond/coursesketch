package multiconnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import connection.ConnectionException;

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
public class ConnectionWrapper {

    /**
     * The code denoting that a websocket closed abnormally.
     */
    public static final int CLOSE_ABNORMAL = 1006;

    /**
     * A string representing that the websocket was closed due to an end of
     * file.
     */
    public static final String CLOSE_EOF = "(EOF)*";

    /**
     * A websocket can only have a maximum of 10 failed starts.
     */
    private static final int MAX_FAILED_STARTS = 10;

    /**
     * The amount of time to sleep between connection attempts.
     */
    private static final int MIN_SLEEP_TIME = 1000;

    /**
     * This is the server that is running the connection wrapper.
     */
    private final GeneralConnectionServer parentServer;

    /**
     * This is the manager that holds an instance of this connection wrapper.
     */
    private MultiConnectionManager parentManager;

    /**
     *
     */
    private WebSocketClient client;

    /**
     * The active session of the current connection wrapper.
     */
    private Session session;

    /**
     * The location of the server that this object is connected to.
     */
    private final URI destination;

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
    private ArrayList<ByteBuffer> queuedMessages = new ArrayList<ByteBuffer>();

    /**
     * A listener that is called when the socket fails to connect properly.
     */
    private ActionListener socketFailedListener;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ConnectionWrapper#connect()} or call
     * {@link ConnectionWrapper#send(byte[])}.
     *
     * @param iDestination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param iParentServer
     *            The server that is using this connection wrapper.
     */
    public ConnectionWrapper(final URI iDestination, final GeneralConnectionServer iParentServer) {
        this.parentServer = iParentServer;
        this.destination = iDestination;
        started = false;
    }

    /**
     * Sets the listener for when a connection fails.
     *
     * @param listen
     *            The listener that is called when a connection fails.
     */
    protected final void setFailedSocketListener(final ActionListener listen) {
        socketFailedListener = listen;
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws Exception Throws an exception if an error occurs during the connection attempt.
     */
    public final void connect() throws Exception {
        client = new WebSocketClient();
        try {
            client.start();
            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(this, destination, request);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Occurs when the remote connection closes the socket.
     *
     * @param statusCode
     *            The status code reason that the socket closed. Look up socket
     *            status codes for more information.
     * @param reason
     *            The string reason or message that occurs when the socket
     *            closes.
     */
    @OnWebSocketClose
    public final void onClose(final int statusCode, final String reason) {
        connected = false;
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        if (statusCode == CLOSE_ABNORMAL && reason.matches(CLOSE_EOF) || reachedEof) {
            reachedEof = true;
        }
        this.session = null;
    }

    /**
     * @param iSession
     *            The session handed to this object when the connection is
     *            opened and has succeeded.
     */
    @OnWebSocketConnect
    public final void onOpen(final Session iSession) {
        failedStarts = 0;
        started = true;
        reachedEof = false;
        connected = true;
        queing = false;
        this.session = iSession;
        System.out.println("Connection was succesful for: " + this.getClass().getSimpleName());
    }

    /**
     * Called when a message occurs and then wraps the message in a {@link ByteBuffer}.
     * @param data The actual bytes that contain the message.
     * @param offset The offset at which the message occurs.
     * @param length The length of the message itself.
     * @see {@link ConnectionWrapper#onMessage(ByteBuffer)}
     */
    @OnWebSocketMessage
    public final void onMessage(final byte[] data, final int offset, final int length) {
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
    public final void onError(final Session erroredSession, final Throwable cause) {
        if (cause instanceof java.io.EOFException || cause instanceof java.net.SocketTimeoutException) {
            reachedEof = true;
            System.out.println("This websocket timed out!");
        }
        if (erroredSession != null) {
            System.err.println("Socket: " + this.getClass().getSimpleName() + "Session Error: " + cause);
        } else {
            System.err.println("Socket: " + this.getClass().getSimpleName() + " Error: " + cause);
            if (cause instanceof java.net.ConnectException) {
                if (cause.getMessage().trim().equalsIgnoreCase("Connection refused")) {
                    // System.out.println("This error is caused by a server not being open yet!");
                    refusedConnection = true;
                    System.err.println("Connection was refused");
                }
                System.err.println("Connection had issues!");
            }
        }
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer The message that is received by this object.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void onMessage(final ByteBuffer buffer) {
        final MultiConnectionState state = getStateFromId(GeneralConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());
        session.getRemote().sendBytesByFuture(buffer);
        getConnectionFromState(state).getRemote().sendBytesByFuture(buffer);
    }

    /**
     * @param key
     *            The key that maps to a specific state.
     * @return a MultiConnectionState given a key representing that state.
     */
    protected final MultiConnectionState getStateFromId(final String key) {
        if (parentServer == null) {
            System.out.println("null parent");
            return null;
        }
        return parentServer.getIdToState().get(key);
    }

    /**
     * @param state
     *            A specific server that is connected by this connection
     *            wrapper.
     * @return Given a state grab the session associated with that state.
     */
    protected final Session getConnectionFromState(final MultiConnectionState state) {
        if (parentServer == null) {
            System.out.println("null parent");
            return null;
        }
        return parentServer.getIdToConnection().get(state);
    }

    /**
     * Sends a binary message over the connection.
     *
     * If the connection fails then a reconnect is attempted. If the attempt
     * fails more than {@link ConnectionWrapper#MAX_FAILED_STARTS} times then an
     * exception is thrown
     *
     * @param buffer
     *            The binary message that is being sent out.
     * @throws ConnectionException
     *             Thrown if the number of connection attempts exceeds
     *             {@link ConnectionWrapper#MAX_FAILED_STARTS}
     */
    public final void send(final ByteBuffer buffer) throws ConnectionException {
        if (connected) {
            System.out.println("Sending message from: " + this.getClass().getSimpleName());
            session.getRemote().sendBytesByFuture(buffer);
            if (queing) {
                while (queuedMessages.size() > 0) {
                    session.getRemote().sendBytesByFuture(queuedMessages.remove(0));
                }
                queing = false;
            }
        } else if ((started || reachedEof || refusedConnection) && failedStarts < MAX_FAILED_STARTS) {
            final String endReson = reachedEof || started ? "of a timeout" : refusedConnection ? "connection to the server was refused"
                    : "We do not know why";
            System.err.println("Trying to reconnect " + this.getClass().getSimpleName() + ". It has ended because " + endReson);
            if (!queing) {
                failedStarts += 1;
                System.err.println("attempt " + failedStarts + " out of " + MAX_FAILED_STARTS);
                queing = true;
                // maybe try reconnecting here?
                final Thread d = new Thread() {
                    @Override
                    public void run() {
                        try {
                            connect();
                            Thread.sleep(MIN_SLEEP_TIME);
                            queing = false;
                            send(buffer);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };
                d.start();
            } else {
                System.err.println("Adding a queuedMessage");
                queuedMessages.add(buffer);
            }
        } else if (failedStarts < MAX_FAILED_STARTS) { // connections has not
                                                       // been established yet
            System.err.println("Trying to wait on " + this.getClass().getSimpleName() + ". It has not connected yet.");
            if (!queing) {
                queing = true;
                failedStarts += 1;
                System.err.println("attempt " + failedStarts + " out of " + MAX_FAILED_STARTS);
                final Thread d = new Thread() {
                    @Override
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
                d.start();
            } else {
                System.err.println("Adding a queuedMessage");
                queuedMessages.add(buffer);
            }
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
     * Sends a binary message over the connection.
     *
     * @param bytes The binary message being sent.
     * @throws ConnectionException Thrown if an the connection fails
     * @see {@link ConnectionWrapper#send(ByteBuffer)}
     */
    public final void send(final byte[] bytes) throws ConnectionException {
        send(ByteBuffer.wrap(bytes));
    }

    /**
     * Closes out the connection.
     */
    public final void close() {
        System.out.println("Closing connection: " + this.getClass().getSimpleName());
        if (session != null) {
            session.close();
        }
    }

    /**
     * Closes out the connection with the given statusCode and arguments.
     *
     * @param statusCode
     *            The statusCode that is used to close out the Websocket (look
     *            up socket close statuses for more information)
     * @param args
     *            The additional arguments attached to the close command. (This
     *            is usually a message with details).
     */
    public final void close(final int statusCode, final String args) {
        System.out.println("Closing connection: " + this.getClass().getSimpleName());
        if (session != null) {
            session.close(statusCode, args);
        }
    }

    /**
     * @return {@link URI} a copy of the URI that is also normalized.
     */
    public final URI getURI() {
        return destination.normalize();
    }

    /**
     * @return true if the ConnectionWrapper is currently connected.
     */
    public final boolean isConnected() {
        return connected;
    }

    /**
     * @return The parent server for this specific connection.
     */
    protected final GeneralConnectionServer getParentServer() {
        return parentServer;
    }

    /**
     * @return The parent manager for this specific connection.
     */
    protected final MultiConnectionManager getParentManager() {
        return parentManager;
    }

    /**
     * @param multiConnectionManager The Parent manager for this specific connection.
     */
    /* package-private */ final void setParentManager(final MultiConnectionManager multiConnectionManager) {
        if (this.parentManager != null) {
            throw new RuntimeException("This field is immutable and can only be set once.");
        }
        parentManager = multiConnectionManager;
    }
}
