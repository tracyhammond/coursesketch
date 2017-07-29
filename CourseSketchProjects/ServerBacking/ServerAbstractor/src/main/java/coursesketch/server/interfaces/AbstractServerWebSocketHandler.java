package coursesketch.server.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import coursesketch.utilities.ExceptionUtilities;
import utilities.Encoder;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages a socket on the server side if you want to know about the client side see {@link AbstractClientWebSocket}.
 *
 * One instance of this class manages many sockets at the same time as such it is
 *
 * Created by gigemjt on 10/19/14.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AbstractServerWebSocketHandler {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServerWebSocketHandler.class);

    /**
     * The max message size we will allow a message to support.
     */
    public static final int MAX_MESSAGE_SIZE = 10000000;

    /**
     * The maximum number of connections.
     * This can be overwritten to give the number of connections a new value.
     */
    private static final int MAX_CONNECTIONS = 80;
    /**
     * The name of the socket This can be hidden in a subclass.
     */
    public static final String NAME = "General Socket";
    /**
     * The state that represents the server being full.
     */
    private static final int STATE_SERVER_FULL = 4001;
    /**
     * The state representing that the client has closed the connection.
     */
    private static final int STATE_CLIENT_CLOSE = 4003;
    /**
     * The message representing that the client closed the connection.
     */
    private static final String CLIENT_CLOSE_MESSAGE = "The client closed the connection";

    /**
     * The message for when the server is full.
     */
    private static final String FULL_SERVER_MESSAGE = "Sorry, the " + NAME + "server is full";

    /**
     * Maps a Session to its MultiConnectionState. FUTURE: {@link org.cliffc.high_scale_lib.NonBlockingHashMap}
     */
    private final Map<SocketSession, MultiConnectionState> connectionToId = new HashMap<>();

    /**
     * Maps a MultiConnectionState to a Session. FUTUE: {@link org.cliffc.high_scale_lib.NonBlockingHashMap}
     */
    private final Map<MultiConnectionState, SocketSession> idToConnection = new HashMap<>();

    /**
     * Maps a String representing the connections ID to its MultiConnectionState.
     */
    private final Map<String, MultiConnectionState> idToState = new HashMap<>();

    /**
     * The parent servlet for this server.
     */
    private final ISocketInitializer parentServer;

    /**
     * Information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * An object that reads from the database.
     */
    private AbstractCourseSketchDatabaseReader databaseReader;

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent The parent servlet of this server.
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    protected AbstractServerWebSocketHandler(final ISocketInitializer parent, final ServerInfo serverInfo) {
        parentServer = parent;
        this.serverInfo = serverInfo;
    }

    /**
     * Called when the connection is closed.
     *
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    protected final void onClose(final SocketSession conn, final int statusCode, final String reason) {
        // FUTURE: find out how to see if the connection is closed by us or them.
        LOG.info("{} has disconnected from The Server. {} with reason : {}", conn.getRemoteAddress(), statusCode, reason);
        final MultiConnectionState stateId = getConnectionToId().remove(conn);
        if (stateId != null) {
            idToConnection.remove(stateId);
            idToState.remove(stateId.getSessionId());
        } else {
            LOG.error("Connection Id can not be found");
        }
    }

    /**
     * Called every time the connection is formed.
     *
     * @param conn The connection that is being opened.
     */
    protected final void onOpen(final SocketSession conn) {
        if (getConnectionToId().size() >= MAX_CONNECTIONS) {
            // Return negatative state.
            LOG.info("FULL SERVER"); // send message to someone?
            conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
        }

        final MultiConnectionState uniqueState = getUniqueState();
        // uses actual variables as get methods produce unmodifiable maps
        connectionToId.put(conn, uniqueState);
        idToConnection.put(uniqueState, conn);
        LOG.debug("Session Key {}", uniqueState.getSessionId());
        idToState.put(uniqueState.getSessionId(), uniqueState);
        LOG.info("ID ASSIGNED");

        LOG.info("Recieving connection {}", getConnectionToId().size());
        openSession(conn);
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn the connection that is being opened.
     */
    protected abstract void openSession(final SocketSession conn);

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    protected abstract void onError(SocketSession session, Throwable cause);

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param buffer The bytes that sent the message.
     */
    protected final void onMessage(final SocketSession session, final ByteBuffer buffer) {
        final Request req = Decoder.parseRequest(buffer);
        if (req == null) {
            send(session, createBadConnectionResponse(null, AbstractClientWebSocket.class));
            LOG.info("protobuf error");
            // this.
            // we need to somehow send an error to the client here
            return;
        }

        if (req.getRequestType().equals(Request.MessageType.CLOSE)) {
            LOG.info("CLOSE THE SERVER FROM THE CLIENT");
            session.close(STATE_CLIENT_CLOSE, CLIENT_CLOSE_MESSAGE);
            return;
        }

        onMessage(session, req);
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * @param session
     *            the session object that created the message
     * @param req
     *            the message itself
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected abstract void onMessage(final SocketSession session, final Request req);

    /**
     * A helper method for sending data given a session.
     * This is a non-blocking way to send messages to a server.
     *
     * @param session The session that the message is being sent with.
     * @param req The actual message that is being sent.
     */
    public final void send(final SocketSession session, final Request req) {
        LOG.debug("Sending Request {}", req.getRequestId());
        session.send(ByteBuffer.wrap(req.toByteArray()));
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    protected abstract void onStop();

    /**
     * @return The {@link AbstractServerWebSocketHandler#NAME} of the connection should be overwritten to give it a new name.
     */
    public final String getName() {
        return NAME;
    }

    /**
     * @return The hostName of the server.
     * @see ServerInfo#hostName
     */
    public final String getHostName() {
        return serverInfo.getHostName();
    }

    /**
     * @return The port that the server is running on.
     * @see ServerInfo#port
     */
    public final int getHostPort() {
        return serverInfo.getPort();
    }

    /**
     * Returns a new connection with an id.
     *
     * This can be overwritten to make a more advance connection. This is only
     * called in {@link AbstractServerWebSocketHandler#onOpen(SocketSession)}
     *
     * @return an instance of {@link MultiConnectionState}.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public MultiConnectionState getUniqueState() {
        return new MultiConnectionState(Encoder.nextID().toString());
    }

    /**
     * Creates a response to a bad connection.
     *
     * This can be used to denote that the message will not get to its
     * destination server because one server in this chain is not connected
     * correctly.
     *
     * @param req The original request that was sent.
     * @param connectionType A class representing the connection that is not correctly connected.
     * @return {@link Request} with a message explaining what happened.
     */
    public final Request createBadConnectionResponse(final Request req, final Class<? extends AbstractClientWebSocket> connectionType) {
        final Message.ProtoException exception = ExceptionUtilities.createProtoException(new Exception("A server with connection type: "
                + connectionType.getSimpleName() + " Is not connected correctly"));
        return ExceptionUtilities.createExceptionRequest(req, exception);
    }

    /**
     * Stops the server and empties out all connections.
     */
    public final void stop() {
        for (SocketSession sesh : getConnectionToId().keySet()) {
            sesh.close();
        }
        getConnectionToId().clear();
        idToConnection.clear();
        idToState.clear();
        onStop();
    }

    /**
     * @return The servlet that represents this server.
     */
    protected final ISocketInitializer getParentServer() {
        return parentServer;
    }

    /**
     * @return The {@link MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected abstract MultiConnectionManager getConnectionManager();

    /**
     * Creates a CourseSketchDatabaseReader if it is needed.
     *
     * @param info Information about the server.
     * @return {@link AbstractCourseSketchDatabaseReader}.
     */
    protected abstract AbstractCourseSketchDatabaseReader createDatabaseReader(final ServerInfo info);

    /**
     * @return A map representing the Id to state. The returned map is read only.
     */
    protected final Map<String, MultiConnectionState> getIdToState() {
        return Collections.unmodifiableMap(idToState);
    }

    /**
     * @return A map representing the Id to Connection. The returned map is read only.
     */
    protected final Map<MultiConnectionState, SocketSession> getIdToConnection() {
        return Collections.unmodifiableMap(idToConnection);
    }

    /**
     * @return The current number of connections to the server.
     */
    public final int getCurrentConnectionNumber() {
        return getConnectionToId().size();
    }

    /**
     * @return the connectionToId
     */
    protected final Map<SocketSession, MultiConnectionState> getConnectionToId() {
        return connectionToId;
    }

    /**
     * Performs some initialization.  This is called before the server is started.
     */
    public final void initialize() {
        databaseReader = createDatabaseReader(this.serverInfo);
        try {
            startDatabase();
        } catch (DatabaseAccessException e) {
            LOG.error("An error was created starting the database for the server", e);
        }
        onInitialize();
    }

    /**
     * Performs some initialization.
     *
     * This is called before the server is started.
     * This is called by {@link #initialize()}.
     */
    protected abstract void onInitialize();

    /**
     * Starts the database if it exists.
     *
     * @throws DatabaseAccessException thrown if the database is unable to start.
     */
    private void startDatabase() throws DatabaseAccessException {
        final AbstractCourseSketchDatabaseReader reader = getDatabaseReader();
        if (reader != null) {
            reader.startDatabase();
        }
    }

    /**
     * @return {@link AbstractCourseSketchDatabaseReader}.  This may return null if one is not set.
     */
    protected final AbstractCourseSketchDatabaseReader getDatabaseReader() {
        return databaseReader;
    }

    /**
     * Parses a request from the given ByteBuffer.
     * @author gigemjt
     *
     */
    public static final class Decoder {

        /**
         * Empty constructor.
         */
        private Decoder() {
        }

        /**
         * Returns a {@link Request} as it is parsed from the ByteBuffer.
         *
         * @param buffer The message that is being deoced into a request.
         * @return null if the ByteBuffer does not exist or a Request otherwise.
         */
        public static Request parseRequest(final ByteBuffer buffer) {
            try {
                return Request.parseFrom(buffer.array());
            } catch (final InvalidProtocolBufferException e) {
                LOG.error("Error parsing request", e);
                return null;
            }
        }
    }
}
