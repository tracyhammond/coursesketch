package coursesketch.serverfront;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.server.frontend.ServerWebSocketHandler;
import coursesketch.server.frontend.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;
import coursesketch.server.interfaces.SocketSession;
import connection.AnswerClientWebSocket;
import connection.DataClientWebSocket;
import connection.LoginClientWebSocket;
import connection.LoginConnectionState;
import connection.ProxyConnectionManager;
import connection.RecognitionConnection;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import utilities.ConnectionException;
import utilities.CourseSketchException;
import coursesketch.utilities.ExceptionUtilities;
import utilities.LoggingConstants;
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
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServerWebSocketHandler.class);

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
        super(parent, parent.getServerInfo());
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
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                }
                try {
                    getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, AnswerClientWebSocket.class);
                } catch (ConnectionException e1) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
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
        if (!state.isLoggedIn() && req.getRequestType() != MessageType.TIME) {
            if (state.getTries() > MAX_LOGIN_TRIES) {
                conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
                return;
            }
            final String sessionID = state.getSessionId();
            LOG.info("Request type is {}", req.getRequestType().name());
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
        if (!req.hasRequestType()) {
            LOG.error("A request type was not specified for this request. This request will not be processed");
            LOG.error("Request that did not contain type: {}", req);
            send(conn, createBadConnectionResponse(req, RecognitionConnection.class));
        }
        final String sessionID = state.getSessionId();
        if (req.getRequestType() == MessageType.RECOGNITION) {
            LOG.info("REQUEST TYPE = RECOGNITION");
            try {
                // No userId is sent for security reasons.
                ((ProxyConnectionManager) this.getConnectionManager()).getBestConnection(RecognitionConnection.class)
                        .parseConnection(req, sessionID);
            } catch (CourseSketchException | RecognitionException e) {
                LOG.error("Recognition error!");
                send(conn, ExceptionUtilities.createExceptionRequest(req, ExceptionUtilities.createProtoException(e),
                        "Exception in proxy server sending recognition"));
            }
        } else if (req.getRequestType() == MessageType.SUBMISSION) {
            LOG.info("REQUEST TYPE = SUBMISSION");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, DataClientWebSocket.class,
                        ((ProxyConnectionState) state).getAuthId(), ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                send(conn, createBadConnectionResponse(req, AnswerClientWebSocket.class));
            }
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, AnswerClientWebSocket.class,
                        ((ProxyConnectionState) state).getAuthId(), ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                send(conn, createBadConnectionResponse(req, AnswerClientWebSocket.class));
            }
        } else if (req.getRequestType() == MessageType.DATA_REQUEST || req.getRequestType() == MessageType.DATA_INSERT
                || req.getRequestType() == MessageType.DATA_UPDATE || req.getRequestType() == MessageType.DATA_REMOVE) {
            LOG.info("REQUEST TYPE = DATA REQUEST");
            try {
                ((ProxyConnectionManager) this.getConnectionManager()).send(req, sessionID, DataClientWebSocket.class,
                        ((ProxyConnectionState) state).getAuthId(), ((ProxyConnectionState) state).getUserId());
            } catch (ConnectionException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
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
        LOG.info("Creating the socket failed listeners for the server");
        final ActionListener listen = new ActionListener() {
            /**
             * Called when the message fails to send correctly.
             * Sends a response to all of the clients to let them know of the issues.
             * @param event The event that is triggered.
             */
            @SuppressWarnings("PMD.CommentRequired")
            @Override
            public void actionPerformed(final ActionEvent event) {
                LOG.error("Looking at the failed messages");
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
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                    } catch (ClassNotFoundException e1) {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e1);
                    }
                }
            }
        };
        this.getConnectionManager().setFailedSocketListener(listen, AnswerClientWebSocket.class);
        this.getConnectionManager().setFailedSocketListener(listen, DataClientWebSocket.class);
        this.getConnectionManager().setFailedSocketListener(listen, LoginClientWebSocket.class);
    }
}
