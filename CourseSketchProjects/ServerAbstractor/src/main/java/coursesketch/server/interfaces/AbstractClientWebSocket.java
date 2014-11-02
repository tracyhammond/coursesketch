package coursesketch.server.interfaces;

import utilities.ConnectionException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps around a basic client and maintains a sessions to a single server.
 *
 * If The ConnectionWrapper is not connect it will attempt to connect when the
 * next message is sent queuing up messages while nothing is happening. This
 * allows us to have connections that timeout and save bandwidth and wait till
 * they are actually being used. The downside of this is that the first message
 * takes much longer to send.
 * Created by gigemjt on 10/19/14.
 */
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField", "PMD.TooManyMethods" })
public abstract class AbstractClientWebSocket {
    /**
     * A websocket can only have a maximum of 10 failed starts.
     */
    protected static final int MAX_FAILED_STARTS = 10;

    /**
     * The amount of time to sleep between connection attempts.
     */
    protected static final int MIN_SLEEP_TIME = 1000;

    /**
     * The code denoting that a websocket closed abnormally.
     */
    protected static final int CLOSE_ABNORMAL = 1006;

    /**
     * A string representing that the websocket was closed due to an end of
     * file.
     */
    protected static final String CLOSE_EOF = "(EOF)*";

    /**
     * This is the manager that holds an instance of this connection wrapper.
     */
    private MultiConnectionManager parentManager;

    /**
     * This is the server that is running the connection wrapper.
     */
    private final AbstractServerWebSocketHandler parentServer;

    /**
     * The active session of the current connection wrapper.
     */
    private SocketSession session;

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
    private final List<ByteBuffer> queuedMessages = new ArrayList<ByteBuffer>();

    /**
     * A listener that is called when the socket fails to connect properly.
     */
    private ActionListener socketFailedListener;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link AbstractClientWebSocket#connect()} or call
     * {@link AbstractClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param iDestination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param iParentServer
     *            The server that is using this connection wrapper.
     */
    protected AbstractClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        this.parentServer = iParentServer;
        this.destination = iDestination;
        started = false;
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws ConnectionException Throws an exception if an error occurs during the connection attempt.
     */
    protected abstract void connect() throws ConnectionException;

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
    public final void onClose(final int statusCode, final String reason) {
        connected = false;
        System.out.printf("%s closed: %d - %s%n", this.getClass().getSimpleName(), statusCode, reason);
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
    public final void onOpen(final SocketSession iSession) {
        failedStarts = 0;
        started = true;
        reachedEof = false;
        connected = true;
        queing = false;
        this.session = iSession;
        System.out.println("Connection was succesful for: " + this.getClass().getSimpleName());
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
    public final void onError(final SocketSession erroredSession, final Throwable cause) {
        if (cause instanceof java.io.EOFException || cause instanceof java.net.SocketTimeoutException) {
            reachedEof = true;
            System.out.println("This websocket timed out!");
        }
        if (erroredSession == null) {
            System.err.println("Socket: " + this.getClass().getSimpleName() + " Error: " + cause);
            if (cause instanceof java.net.ConnectException) {
                if (cause.getMessage().trim().equalsIgnoreCase("Connection refused")) {
                    // System.out.println("This error is caused by a server not being open yet!");
                    refusedConnection = true;
                    System.err.println("Connection was refused");
                }
                System.err.println("Connection had issues!");
            }
        } else {
            System.err.println("Socket: " + this.getClass().getSimpleName() + "Session Error: " + cause);
        }
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer The message that is received by this object.
     */
    protected abstract void onMessage(ByteBuffer buffer);

    /**
     * Sends a binary message over the connection.
     *
     * If the connection fails then a reconnect is attempted. If the attempt
     * fails more than {@link AbstractClientWebSocket#MAX_FAILED_STARTS} times then an
     * exception is thrown
     *
     * @param buffer
     *            The binary message that is being sent out.
     * @throws ConnectionException
     *             Thrown if the number of connection attempts exceeds
     *             {@link AbstractClientWebSocket#MAX_FAILED_STARTS}
     */
    public final void send(final ByteBuffer buffer) throws ConnectionException {
        if (isConnected()) {
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
        System.out.println("Sending message to: " + this.getClass().getSimpleName());
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
     * @return {@link java.net.URI} a copy of the URI that is also normalized.
     */
    public final URI getURI() {
        return destination.normalize();
    }

    /**
     * @return true if the socket is currently connected.
     */
    public final boolean isConnected() {
        return connected;
    }

    /**
     * @return The parent server for this specific connection.
     */
    protected final AbstractServerWebSocketHandler getParentServer() {
        return parentServer;
    }

    /**
     * Sets the listener for when a connection fails.
     *
     * @param listen
     *            The listener that is called when a connection fails.
     */
    public final void setFailedSocketListener(final ActionListener listen) {
        socketFailedListener = listen;
    }

    /**
     * @param key
     *            The key that maps to a specific state.
     * @return a MultiConnectionState given a key representing that state.
     */
    protected final MultiConnectionState getStateFromId(final String key) {
        if (this.getParentServer() == null) {
            System.out.println("null parent");
            return null;
        }
        return this.getParentServer().getIdToState().get(key);
    }

    /**
     * @param state
     *            A specific server that is connected by this connection
     *            wrapper.
     * @return Given a state grab the session associated with that state.
     */
    protected final SocketSession getConnectionFromState(final MultiConnectionState state) {
        if (this.getParentServer() == null) {
            System.out.println("null parent");
            return null;
        }
        return this.getParentServer().getIdToConnection().get(state);
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
            throw new IllegalStateException("This field is immutable and can only be set once.");
        }
        parentManager = multiConnectionManager;
    }

    /**
     * @return the session represented by this current connection.
     */
    protected final SocketSession getSession() {
        return session;
    }
}
