package multiconnection;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;

import com.google.protobuf.InvalidProtocolBufferException;

import connection.TimeManager;

/**
 * A connection server.
 * @author gigemjt
 */
@WebSocket()
public class GeneralConnectionServer {

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
     * The message for when the server is full.
     */
    static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";

    /**
     * The state representing that the client has closed the connection.
     */
    public static final int STATE_CLIENT_CLOSE = 4003;

    /**
     * The message representing that the client closed the connection.
     */
    public static final String CLIENT_CLOSE_MESSAGE = "The client closed the connection";

    /**
     * Maps a Session to its MultiConnectionState.
     */
    protected final HashMap<Session, MultiConnectionState> connectionToId = new HashMap<Session, MultiConnectionState>();

    /**
     * Maps a MultiConnectionState to a Session.
     */
    private final HashMap<MultiConnectionState, Session> idToConnection = new HashMap<MultiConnectionState, Session>();

    /**
     * Maps a String representing the connections ID to its MultiConnectionState.
     */
    private final HashMap<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();

    /**
     * The parent servlet for this server.
     */
    private final GeneralConnectionServlet parentServer;

    /**
     * A constructor that accepts a servlet.
     * @param parent The parent servlet of this server.
     */
    public GeneralConnectionServer(final GeneralConnectionServlet parent) {
        parentServer = parent;
    }

    /**
     * Called when the connection is closed.
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    @OnWebSocketClose
    public final void onClose(final Session conn, final int statusCode, final String reason) {
        // FUTURE: find out how to see if the connection is closed by us or them.
        System.out.println(conn.getRemoteAddress() + " has disconnected from The Server." + statusCode + "with reason : " + reason);
        final MultiConnectionState id = connectionToId.remove(conn);
        if (id != null) {
            idToConnection.remove(id);
            idToState.remove(id.getKey());
        } else {
            System.err.println("Connection Id can not be found");
        }
    }

    /**
     * Called every time the connection is formed.
     *
     * @param conn The connection that is being opened.
     */
    @OnWebSocketConnect
    public void onOpen(final Session conn) {
        if (connectionToId.size() >= MAX_CONNECTIONS) {
            // Return negatative state.
            System.out.println("FULL SERVER"); // send message to someone?
            conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
        }

        final MultiConnectionState id = getUniqueState();
        connectionToId.put(conn, id);
        getIdToConnection().put(id, conn);
        System.out.println("Session Key " + id.getKey());
        getIdToState().put(id.getKey(), id);
        System.out.println("ID ASSIGNED");

        System.out.println("Recieving connection " + connectionToId.size());
    }

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    @SuppressWarnings("static-method")
    @OnWebSocketError
    public final void onError(final Session session, final Throwable cause) {
        System.err.println("Session: " + session.getRemoteAddress() + "\ncaused:" + cause);
    }

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param data The bytes that sent the message.
     * @param offset The offset at which the message occurs.
     * @param length The length of the message.
     */
    @OnWebSocketMessage
    public final void onMessage(final Session session, final byte[] data, final int offset, final int length) {
        onMessage(session, ByteBuffer.wrap(data, offset, length));
    }

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param buffer The bytes that sent the message.
     */
    protected final void onMessage(final Session session, final ByteBuffer buffer) {
        final Request req = Decoder.parseRequest(buffer);
        if (req == null) {
            send(session, createBadConnectionResponse(null, ConnectionWrapper.class));
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
     * By default it is an echo server, basically it echos what it receives.
     *
     * @param session
     *            the session object that created the message
     * @param req
     *            the message itself
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void onMessage(final Session session, final Request req) {
        send(session, req);
    }

    /**
     * A helper method for sending data given a session.
     * This is a non-blocking way to send messages to a server.
     *
     * @param session The session that the message is being sent with.
     * @param req The actual message that is being sent.
     */
    public static void send(final Session session, final Request req) {
        session.getRemote().sendBytesByFuture(ByteBuffer.wrap(req.toByteArray()));
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
    protected static Request createBadConnectionResponse(final Request req, final Class<? extends ConnectionWrapper> connectionType) {
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
     * Cleans out the server.
     */
    final void stop() {
        for (Session sesh : connectionToId.keySet()) {
            sesh.close();
        }
        connectionToId.clear();
        idToConnection.clear();
        idToState.clear();
        onStop();
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    public void onStop() {

    }

    /**
     * @return The name of the connection should be overwritten to give it a new name.
     */
    @SuppressWarnings("static-method")
    public String getName() {
        return NAME;
    }

    /**
     * Returns a new connection with an id.
     *
     * This can be overwritten to make a more advance connection. This is only
     * called in {@link GeneralConnectionServer#onOpen(Session)}
     *
     * @return an instance of {@link MultiConnectionState}.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public MultiConnectionState getUniqueState() {
        return new MultiConnectionState(Encoder.nextID().toString());
    }

    /**
     * @return A map representing the Id to state. The returned map is read only.
     */
    protected final Map<String, MultiConnectionState> getIdToState() {
        return Collections.unmodifiableMap(idToState);
    }

    /**
     * @return A map representing the Id to Connection. The returned map is read only.
     */
    protected final Map<MultiConnectionState, Session> getIdToConnection() {
        return Collections.unmodifiableMap(idToConnection);
    }

    /**
     * @return The {@link MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected final MultiConnectionManager getConnectionManager() {
        return parentServer.getManager();
    }

    /**
     * @return The current number of connections to the server.
     */
    public final int getCurrentConnectionNumber() {
        return connectionToId.size();
    }

    /**
     * @return The servlet that represents this server.
     */
    protected final GeneralConnectionServlet getParentServer() {
        return parentServer;
    }

    /**
     * Parses a request from the given ByteBuffer.
     * @author gigemjt
     *
     */
    public static final class Decoder {
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
