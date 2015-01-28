package serverfront;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.SocketSession;
import internalconnections.AnswerClientWebSocket;
import internalconnections.DataClientWebSocket;
import internalconnections.LoginClientWebSocket;
import internalconnections.LoginConnectionState;
import internalconnections.ProxyConnectionManager;
import internalconnections.RecognitionClientWebSocket;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import utilities.ConnectionException;
import utilities.TimeManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public final class ProxyServerWebSocketHandler extends ServerWebSocketHandler {

    /**
     * The name of the socket.
     */
    public static final String NAME = "Proxy";

    /**
     * an overwrite of the max number of connections to increase it to 60.
     */
    @SuppressWarnings("hiding")
    public static final int MAX_CONNECTIONS = 60;

    /**
     * The code for when an invalid login has happened.
     */
    public static final int STATE_INVALID_LOGIN = 4002;

    /**
     * The number of times someone can attempt to login.
     */
    public static final int MAX_LOGIN_TRIES = 5;

    /**
     * A message that is sent when too many login attempts happen.
     */
    public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";

    /**
     * @param parent
     *            The servlet made for this server.
     */
    public ProxyServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
        final ActionListener listener = new ActionListener() {
            /**
             * Sends a message that sets up time sync to all connecting clients when the time is synced for this server.
             * @param event The event that triggers the time being set.
             */
            @SuppressWarnings("PMD.CommentRequired")
            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, LoginClientWebSocket.class);
                } catch (ConnectionException e1) {
                    e1.printStackTrace();
                }
                try {
                    getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, AnswerClientWebSocket.class);
                } catch (ConnectionException e1) {
                    e1.printStackTrace();
                }

                final Set<SocketSession> conns = getConnectionToId().keySet();
                for (SocketSession conn : conns) {
                    send(conn, TimeManager.serverSendTimeToClient());
                }

            }
        };
        TimeManager.setTimeEstablishedListener(listener);
    }

    /**
     * Tries to sync time with this new client.
     *
     * @param conn
     *            the connection that is being opened.
     */
    @Override
    public void openSession(final SocketSession conn) {
        send(conn, TimeManager.serverSendTimeToClient());
    }

    /**
     * {@inheritDoc} Routes the given request to the correct server.
     */
    @Override
    public void onMessage(final SocketSession conn, final Request req) {
        final LoginConnectionState state = (LoginConnectionState) getConnectionToId().get(conn);

        // the connection is waiting to login
        if (state.isPending()) {
            // conn.send(pending);
            return;
        }
        if (!state.isLoggedIn() && req.getRequestType()!= MessageType.TIME) {
            if (state.getTries() > MAX_LOGIN_TRIES) {
                conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
                return;
            }
            final String sessionID = state.getKey();
            System.out.println("Request type is " + req.getRequestType().name());
            try {
                this.getConnectionManager().send(req, sessionID, LoginClientWebSocket.class);
            } catch (ConnectionException e) {
                send(conn, createBadConnectionResponse(req, LoginClientWebSocket.class));
            }
        } else {
            if (state.getTries() > MAX_LOGIN_TRIES) {
                conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
                return;
            }
            messageRouter(conn, req, state);
        }
    }

    /**
     * Message handling for a user that is logged in.
     * Basically this routes the message to a specific server.
     * (KEEP THIS METHOD PRIVATE!)
     * @param conn
     *            the session object that created the message
     * @param req
     *            the message itself
     * @param state
     *            the state that represents the currently logged in user
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.UnusedPrivateMethod" })
    private void messageRouter(final SocketSession conn, final Request req, final MultiConnectionState state) {
        final String sessionID = state.getKey();
        if (req.getRequestType() == MessageType.RECOGNITION) {
            System.out.println("REQUEST TYPE = RECOGNITION");
            try {
                // No userId is sent for security reasons.
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, RecognitionClientWebSocket.class);
            } catch (ConnectionException e) {
                System.err.println("Recognition error!");
                send(conn, createBadConnectionResponse(req, RecognitionClientWebSocket.class));
            }
        } else if (req.getRequestType() == MessageType.SUBMISSION) {
            System.out.println("REQUEST TYPE = SUBMISSION");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, AnswerClientWebSocket.class,
                        ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                e.printStackTrace();
                send(conn, createBadConnectionResponse(req, AnswerClientWebSocket.class));
            }
        } else if (req.getRequestType() == MessageType.DATA_REQUEST || req.getRequestType() == MessageType.DATA_INSERT
                || req.getRequestType() == MessageType.DATA_UPDATE || req.getRequestType() == MessageType.DATA_REMOVE) {
            System.out.println("REQUEST TYPE = DATA REQUEST");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, DataClientWebSocket.class,
                        ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                e.printStackTrace();
                send(conn, createBadConnectionResponse(req, DataClientWebSocket.class));
            }
        }
    }

    /**
     * @return a number that should be unique.
     */
    @Override
    public MultiConnectionState getUniqueState() {
        return new ProxyConnectionState(Encoder.nextID().toString());
    }

    /**
     * Creates the listener that happens when the server fails to communicate to
     * another websocket.
     *
     * The listener sends a response to all of the clients to let them know of the issues.
     */
    public void initializeListeners() {
        System.out.println("Creating the socket failed listeners for the server");
        final ActionListener listen = new ActionListener() {
            /**
             * Called when the message fails to send correctly.
             * Sends a response to all of the clients to let them know of the issues.
             * @param event The event that is triggered.
             */
            @SuppressWarnings("PMD.CommentRequired")
            @Override
            public void actionPerformed(final ActionEvent event) {
                System.err.println("Looking at the failed messages");
                final ArrayList<ByteBuffer> failedMessages = (ArrayList<ByteBuffer>) event.getSource();
                for (ByteBuffer message : failedMessages) {
                    try {
                        final Request req = Request.parseFrom(message.array());
                        final MultiConnectionState state = getIdToState().get(req.getSessionInfo());
                        final Class<? extends AbstractClientWebSocket> classType = (Class<? extends AbstractClientWebSocket>)
                                Class.forName(event.getActionCommand());
                        final Request result = createBadConnectionResponse(req, classType);
                        send(getIdToConnection().get(state), result);
                    } catch (InvalidProtocolBufferException e1) {
                        e1.printStackTrace();
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        this.getConnectionManager().setFailedSocketListener(listen, AnswerClientWebSocket.class);
        this.getConnectionManager().setFailedSocketListener(listen, DataClientWebSocket.class);
        this.getConnectionManager().setFailedSocketListener(listen, LoginClientWebSocket.class);
    }
}
