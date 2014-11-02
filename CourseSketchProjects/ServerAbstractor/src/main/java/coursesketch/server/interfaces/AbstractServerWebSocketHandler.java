package coursesketch.server.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import utilities.TimeManager;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import protobuf.srl.request.Message.Request;
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
     * The maximum number of connections.
     * This can be overwritten to give the number of connections a new value.
     */
    public static final int MAX_CONNECTIONS = 80;
    /**
     * The name of the socket This can be hidden in a subclass.
     */
    public static final String NAME = "General Socket";
    /**
     * The state that represents the server being full.
     */
    public static final int STATE_SERVER_FULL = 4001;
    /**
     * The state representing that the client has closed the connection.
     */
    public static final int STATE_CLIENT_CLOSE = 4003;
    /**
     * The message representing that the client closed the connection.
     */
    public static final String CLIENT_CLOSE_MESSAGE = "The client closed the connection";

    /**
     * The message for when the server is full.
     */
    public static final String FULL_SERVER_MESSAGE = "Sorry, the " + NAME + "server is full";

    /**
     * Maps a Session to its MultiConnectionState. FUTURE: {@link org.cliffc.high_scale_lib.NonBlockingHashMap}
     */
    private final Map<SocketSession, MultiConnectionState> connectionToId = new HashMap<SocketSession, MultiConnectionState>();

    /**
     * Maps a MultiConnectionState to a Session. FUTUE: {@link org.cliffc.high_scale_lib.NonBlockingHashMap}
     */
    private final Map<MultiConnectionState, SocketSession> idToConnection = new HashMap<MultiConnectionState, SocketSession>();

    /**
     * Maps a String representing the connections ID to its MultiConnectionState.
     */
    private final Map<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();

    /**
     * The parent servlet for this server.
     */
    private final ISocketInitializer parentServer;

    /**
     * A constructor that accepts a servlet.
     * @param parent The parent servlet of this server.
     */
    protected AbstractServerWebSocketHandler(final ISocketInitializer parent) {
        parentServer = parent;
    }

    /**
     * Called when the connection is closed.
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    protected final void onClose(final SocketSession conn, final int statusCode, final String reason) {
        // FUTURE: find out how to see if the connection is closed by us or them.
        System.out.println(conn.getRemoteAddress() + " has disconnected from The Server." + statusCode + "with reason : " + reason);
        final MultiConnectionState stateId = getConnectionToId().remove(conn);
        if (stateId != null) {
            idToConnection.remove(stateId);
            idToState.remove(stateId.getKey());
        } else {
            System.err.println("Connection Id can not be found");
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
            System.out.println("FULL SERVER"); // send message to someone?
            conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
        }

        final MultiConnectionState uniqueState = getUniqueState();
        // uses actual variables as get methods produce unmodifiable maps
        connectionToId.put(conn, uniqueState);
        idToConnection.put(uniqueState, conn);
        System.out.println("Session Key " + uniqueState.getKey());
        idToState.put(uniqueState.getKey(), uniqueState);
        System.out.println("ID ASSIGNED");

        System.out.println("Recieving connection " + getConnectionToId().size());
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
            System.out.println("protobuf error");
            // this.
            // we need to somehow send an error to the client here
            return;
        }

        if (req.getRequestType().equals(Request.MessageType.CLOSE)) {
            System.out.println("CLOSE THE SERVER FROM THE CLIENT");
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
        session.send(ByteBuffer.wrap(req.toByteArray()));
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    protected abstract void onStop();

    /**
     * @return The {@link AbstractServerWebSocketHandler#NAME} of the connection should be overwritten to give it a new name.
     */
    @SuppressWarnings("static-method")
    public final String getName() {
        return NAME;
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
        final Request.Builder response = Request.newBuilder();
        if (req == null) {
            response.setRequestType(Request.MessageType.ERROR);
        } else {
            response.setRequestType(req.getRequestType());
        }
        response.setResponseText("A server with connection type: " + connectionType.getSimpleName() + " Is not connected correctly");
        return response.build();
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
                return null;
            }
        }
    }

    /**
     * Encodes messages with important information.
     * @author gigemjt
     */
    public static final class Encoder {

        /**
         * This number represents the 4th version of UUID which is the one we use.
         */
        public static final long VERSION_4_UUID = 0x4000L;

        /**
         * How much we multiply the random number by.
         */
        private static final double RANDOM_MULT = 0x1000;

        /**
         * We use this number to increment the counter.
         */
        private static final long COUNTER_ADDITION = 0x10000L;

        /**
         * | with the nano time.  (not sure what it does)
         */
        private static final long MIN_NANO_TIME = 0x8000000000000000L;

        /**
         * counter will be incremented by 0x10000 for each new SComponent that
         * is created counter is used as the most significant bits of the UUID.
         *
         * initialized to 0x4000 (the version -- 4: randomly generated UUID)
         * along with 3 bytes of randomness: Math.random()*0x1000 (0x0 - 0xFFF)
         *
         * the randomness further reduces the chances of collision between
         * multiple sketches created on multiple computers simultaneously
         *
         * (taken from SCComponent)
         */
        private static long counter = VERSION_4_UUID | (long) (Math.random() * RANDOM_MULT);

        /**
         * Empty constructor.
         */
        private Encoder() {
        }

        /**
         * Returns a {@link Request} that contains the sessionInfo and the time
         * that the message was sent.
         *
         * @param req The message that is being rebuilt.
         *
         * @param sessionInfo The information about the session.
         *
         * @return itself if the sessionInfo is null.
         */
        public static Request requestIDBuilder(final Request req, final String sessionInfo) {
            if (sessionInfo == null) {
                return req;
            }
            final Request.Builder breq = Request.newBuilder();
            breq.mergeFrom(req);
            breq.setSessionInfo(sessionInfo);
            if (!breq.hasMessageTime()) {
                breq.setMessageTime(TimeManager.getSystemTime());
            }
            return breq.build();
        }

        /**
         * @return The next UUID that is generated.
         */
        public static UUID nextID() {
            counter += COUNTER_ADDITION; // Overflow is perfectly fine.
            return new UUID(counter, System.nanoTime() | MIN_NANO_TIME);
        }

        /**
         * @return the counter used in the generation of UUIDs.
         */
        public static long getCounter() {
            return counter;
        }
    }
}
