package multiconnection;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;

import com.google.protobuf.InvalidProtocolBufferException;

@WebSocket()
public class GeneralConnectionServer {

    public static final int MAX_CONNECTIONS = 80;
    public static final int STATE_SERVER_FULL = 4001;
    static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";

    public static final int STATE_CLIENT_CLOSE = 4003;
    public static final String CLIENT_CLOSE_MESSAGE = "The client closed the connection";

    protected HashMap<Session, MultiConnectionState> connectionToId = new HashMap<Session, MultiConnectionState>();
    protected HashMap<MultiConnectionState, Session> idToConnection = new HashMap<MultiConnectionState, Session>();
    protected HashMap<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();
    protected GeneralConnectionServlet parentServer = null;

    public GeneralConnectionServer(GeneralConnectionServlet parent) {
        parentServer = parent;
    }

    @OnWebSocketClose
    public void onClose(Session conn, int statusCode, String reason) {
        // TODO: find out how to see if the connection is closed by us or them.
        System.out.println(conn.getRemoteAddress() + " has disconnected from The Server." + statusCode + "with reason : " + reason);
        MultiConnectionState id = connectionToId.remove(conn);
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
     * @param conn
     */
    @OnWebSocketConnect
    public void onOpen(Session conn) {
        if (connectionToId.size() >= MAX_CONNECTIONS) {
            // Return negatative state.
            System.out.println("FULL SERVER"); // send message to someone?
            conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
        }

        MultiConnectionState id = getUniqueState();
        connectionToId.put(conn, id);
        getIdToConnection().put(id, conn);
        System.out.println("Session Key " + id.getKey());
        getIdToState().put(id.getKey(), id);
        System.out.println("ID ASSIGNED");

        System.out.println("Recieving connection " + connectionToId.size());
    }

    @SuppressWarnings("static-method")
    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
        System.err.println("Session: " + session.getRemoteAddress() + "\ncaused:" + cause);
    }

    // @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void onMessage(Session session, byte[] data, int offset, int length) {
        onMessage(session, ByteBuffer.wrap(data));
    }

    /**
     * A blank binary onMessage called every time data is sent.
     */
    protected final void onMessage(Session session, ByteBuffer buffer) {
        Request req = Decoder.parseRequest(buffer);
        if (req == null) {
            send(session, createBadConnectionResponse(req, ConnectionWrapper.class));
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
    @SuppressWarnings("static-method")
    protected void onMessage(Session session, Request req) {
        send(session, req);
    }

    /**
     * A helper method for sending data given a session.
     * 
     * @param session
     * @param req
     */
    public static void send(Session session, Request req) {
        session.getRemote().sendBytesByFuture(ByteBuffer.wrap(req.toByteArray()));
    }

    protected static Request createBadConnectionResponse(Request req, Class<? extends ConnectionWrapper> connectionType) {
        Request.Builder response = Request.newBuilder();
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
     * Available for people to call
     */
    public void onStop() {

    }

    @SuppressWarnings("static-method")
    public String getName() {
        return "General Socket";
    }

    /**
     * Returns a new connection with an id.
     * 
     * This can be overwritten to make a more advance connection. This is only
     * called in {@link GeneralConnectionServer#onOpen(Session)}
     */
    @SuppressWarnings("static-method")
    public MultiConnectionState getUniqueState() {
        return new MultiConnectionState(Encoder.nextID().toString());
    }

    protected HashMap<String, MultiConnectionState> getIdToState() {
        return idToState;
    }

    protected HashMap<MultiConnectionState, Session> getIdToConnection() {
        return idToConnection;
    }

    /**
     * Returns the {@link MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected MultiConnectionManager getConnectionManager() {
        return parentServer.manager;
    }

    public static final class Decoder {
        /**
         * Returns a {@link Request} as it is parsed from the ByteBuffer.
         * 
         * Returns null if the ByteBuffer does not exist.
         * 
         * @param buffer
         * @return
         */
        public static Request parseRequest(ByteBuffer buffer) {
            try {
                return Request.parseFrom(buffer.array());
            } catch (InvalidProtocolBufferException e) {
                // e.printStackTrace();
                return null;
            }
        }
    }

    public static final class Encoder {
        /**
         * counter will be incremented by 0x10000 for each new SComponent that
         * is created counter is used as the most significant bits of the UUID
         * 
         * initialized to 0x4000 (the version -- 4: randomly generated UUID)
         * along with 3 bytes of randomness: Math.random()*0x1000 (0x0 - 0xFFF)
         * 
         * the randomness further reduces the chances of collision between
         * multiple sketches created on multiple computers simultaneously
         * 
         * (taken from SCComponent)
         */
        public static long counter = 0x4000L | (long) (Math.random() * 0x1000);

        /**
         * Returns a {@link Request} that contains the sessionInfo and the time
         * that the message was sent.
         * 
         * Returns itself if the sessionInfo is null.
         */
        public static Request requestIDBuilder(Request req, String sessionInfo) {
            if (sessionInfo == null)
                return req;
            Request.Builder breq = Request.newBuilder();
            breq.mergeFrom(req);
            breq.setSessionInfo(sessionInfo);
            if (!breq.hasMessageTime()) {
                breq.setMessageTime(System.currentTimeMillis());
            }
            return breq.build();
        }

        public static UUID nextID() {
            counter += 0x10000L; // Overflow is perfectly fine.
            return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
        }
    }

    public int getCurrentConnectionNumber() {
        return connectionToId.size();
    }
}
