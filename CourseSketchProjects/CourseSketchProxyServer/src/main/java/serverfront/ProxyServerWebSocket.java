package serverfront;

import interfaces.IConnectionWrapper;
import internalconnections.AnswerConnection;
import internalconnections.DataConnection;
import internalconnections.LoginConnection;
import internalconnections.LoginConnectionState;
import internalconnections.ProxyConnectionManager;
import internalconnections.RecognitionConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Set;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import multiconnection.ServerWebSocket;
import multiconnection.GeneralConnectionServlet;
import interfaces.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import connection.ConnectionException;
import connection.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public final class ProxyServerWebSocket extends ServerWebSocket {

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
    public ProxyServerWebSocket(final GeneralConnectionServlet parent) {
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
                    getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, LoginConnection.class);
                } catch (ConnectionException e1) {
                    e1.printStackTrace();
                }
                try {
                    getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, AnswerConnection.class);
                } catch (ConnectionException e1) {
                    e1.printStackTrace();
                }
                /*
                 * try {
                 * getConnectionManager().send(TimeManager.serverSendTimeToClient
                 * (), null, RecognitionConnection.class); } catch
                 * (ConnectionException e1) { e1.printStackTrace(); } //
                 */
                final Set<Session> conns = getConnectionToId().keySet();
                for (Session conn : conns) {
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
    public void openSession(final Session conn) {
        send(conn, TimeManager.serverSendTimeToClient());
    }

    /**
     * {@inheritDoc} Routes the given request to the correct server.
     */
    @Override
    public void onMessage(final Session conn, final Request req) {
        final LoginConnectionState state = (LoginConnectionState) getConnectionToId().get(conn);

        // the connection is waiting to login
        if (state.isPending()) {
            // conn.send(pending);
            return;
        }
        if (!state.isLoggedIn()) {
            if (state.getTries() > MAX_LOGIN_TRIES) {
                conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
                return;
            }
            final String sessionID = state.getKey();
            System.out.println("Request type is " + req.getRequestType().name());
            try {
                this.getConnectionManager().send(req, sessionID, LoginConnection.class);
            } catch (ConnectionException e) {
                send(conn, createBadConnectionResponse(req, LoginConnection.class));
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
    private void messageRouter(final Session conn, final Request req, final MultiConnectionState state) {
        final String sessionID = state.getKey();
        if (req.getRequestType() == MessageType.RECOGNITION) {
            System.out.println("REQUEST TYPE = RECOGNITION");
            try {
                // No userId is sent for security reasons.
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, RecognitionConnection.class);
            } catch (ConnectionException e) {
                System.err.println("Recognition error!");
                send(conn, createBadConnectionResponse(req, RecognitionConnection.class));
            }
        } else if (req.getRequestType() == MessageType.SUBMISSION) {
            System.out.println("REQUEST TYPE = SUBMISSION");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, AnswerConnection.class,
                        ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                e.printStackTrace();
                send(conn, createBadConnectionResponse(req, AnswerConnection.class));
            }
        } else if (req.getRequestType() == MessageType.DATA_REQUEST || req.getRequestType() == MessageType.DATA_INSERT
                || req.getRequestType() == MessageType.DATA_UPDATE || req.getRequestType() == MessageType.DATA_REMOVE) {
            System.out.println("REQUEST TYPE = DATA REQUEST");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, DataConnection.class,
                        ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                e.printStackTrace();
                send(conn, createBadConnectionResponse(req, DataConnection.class));
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
                        final Class<? extends IConnectionWrapper> classType = (Class<? extends IConnectionWrapper>)
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
        this.getConnectionManager().setFailedSocketListener(listen, AnswerConnection.class);
        this.getConnectionManager().setFailedSocketListener(listen, DataConnection.class);
        this.getConnectionManager().setFailedSocketListener(listen, LoginConnection.class);
    }
}
