package multiconnection;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import interfaces.IConnectionWrapper;
import interfaces.IServerWebSocket;
import interfaces.SocketSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;

/**
 * A connection server.
 * @author gigemjt
 */
@WebSocket()
@SuppressWarnings("PMD.TooManyMethods")
public class ServerWebSocket implements IServerWebSocket {

    /**
     * The message for when the server is full.
     */
    static final String FULL_SERVER_MESSAGE = "Sorry, the BLANK server is full";

    /**
     * Maps a Session to its MultiConnectionState.
     */
    private final Map<SocketSession, MultiConnectionState> connectionToId = new HashMap<SocketSession, MultiConnectionState>();

    /**
     * Maps a MultiConnectionState to a Session.
     */
    private final Map<MultiConnectionState, SocketSession> idToConnection = new HashMap<MultiConnectionState, SocketSession>();

    /**
     * Maps a String representing the connections ID to its MultiConnectionState.
     */
    private final Map<String, MultiConnectionState> idToState = new HashMap<String, MultiConnectionState>();

    /**
     * The parent servlet for this server.
     */
    private final GeneralConnectionServlet parentServer;

    /**
     * A constructor that accepts a servlet.
     * @param parent The parent servlet of this server.
     */
    public ServerWebSocket(final GeneralConnectionServlet parent) {
        parentServer = parent;
    }

    /**
     * Called when the connection is closed.
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    @OnWebSocketClose
    public final void jettyClose(final Session conn, final int statusCode, final String reason) {
        onClose(new JettySession(conn), statusCode, reason);
    }

    /**
     * Called when the connection is closed.
     * @param conn The connection that closed the websocket
     * @param statusCode The reason that the connection was closed.
     * @param reason The human readable reason that the connection was closed.
     */
    @Override
    public final void onClose(final SocketSession conn, final int statusCode, final String reason) {
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
    @OnWebSocketConnect
    public final void jettyOpen(final Session conn) {
        onOpen(new JettySession(conn));
    }

    /**
     * Called every time the connection is formed.
     *
     * @param conn The connection that is being opened.
     */
    @Override
    public final void onOpen(final SocketSession conn) {
        if (getConnectionToId().size() >= MAX_CONNECTIONS) {
            // Return negatative state.
            System.out.println("FULL SERVER"); // send message to someone?
            conn.close(STATE_SERVER_FULL, FULL_SERVER_MESSAGE);
        }

        final MultiConnectionState uniqueState = getUniqueState();
        getConnectionToId().put(conn, uniqueState);
        getIdToConnection().put(uniqueState, conn);
        System.out.println("Session Key " + uniqueState.getKey());
        getIdToState().put(uniqueState.getKey(), uniqueState);
        System.out.println("ID ASSIGNED");

        System.out.println("Recieving connection " + getConnectionToId().size());
        openSession(conn);
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn the connection that is being opened.
     */
    protected void openSession(final SocketSession conn) {
        // Does nothing by default.
    }

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    @OnWebSocketError
    @SuppressWarnings("static-method")
    public final void jettyError(final Session session, final Throwable cause) {
        onError(new JettySession(session), cause);
    }

    /**
     * Called when an error occurs with the connection.
     * @param session The session that has an error.
     * @param cause The actual error.
     */
    @Override
    public final void onError(final SocketSession session, final Throwable cause) {
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
    public final void jettyOnMessage(final Session session, final byte[] data, final int offset, final int length) {
        onMessage(new JettySession(session), ByteBuffer.wrap(data, offset, length));
    }

    /**
     * Called when data is received.
     * @param session The session that sent the message.
     * @param buffer The bytes that sent the message.
     */
    protected final void onMessage(final SocketSession session, final ByteBuffer buffer) {
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
    @Override
    public void onMessage(final SocketSession session, final Request req) {
        send(session, req);
    }

    /**
     * A helper method for sending data given a session.
     * This is a non-blocking way to send messages to a server.
     *
     * @param session The session that the message is being sent with.
     * @param req The actual message that is being sent.
     */
    @Override
    public final void send(final SocketSession session, final Request req) {
        ((JettySession) session).getRemote().sendBytesByFuture(ByteBuffer.wrap(req.toByteArray()));
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
    @Override
    public final Request createBadConnectionResponse(final Request req, final Class<? extends IConnectionWrapper> connectionType) {
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
        for (SocketSession sesh : getConnectionToId().keySet()) {
            sesh.close();
        }
        getConnectionToId().clear();
        idToConnection.clear();
        idToState.clear();
        onStop();
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    public void onStop() {
        // Does nothing by default.
    }

    /**
     * @return The name of the connection should be overwritten to give it a new name.
     */
    @Override
    @SuppressWarnings("static-method")
    public final String getName() {
        return NAME;
    }

    /**
     * Returns a new connection with an id.
     *
     * This can be overwritten to make a more advance connection. This is only
     * called in {@link ServerWebSocket#onOpen(SocketSession)}
     *
     * @return an instance of {@link MultiConnectionState}.
     */
    @Override
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
    protected final Map<MultiConnectionState, SocketSession> getIdToConnection() {
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
    @Override
    public final int getCurrentConnectionNumber() {
        return getConnectionToId().size();
    }

    /**
     * @return The servlet that represents this server.
     */
    protected final GeneralConnectionServlet getParentServer() {
        return parentServer;
    }

    /**
     * @return the connectionToId
     */
    protected final Map<SocketSession, MultiConnectionState> getConnectionToId() {
        return connectionToId;
    }

}
