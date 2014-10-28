package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import utilities.ConnectionException;
import utilities.TimeManager;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
<<<<<<< HEAD:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerConnection.java
public class AnswerConnection extends ConnectionWrapper {

    public AnswerConnection(final URI destination, final GeneralConnectionServer parent) {
=======
public final class AnswerClientWebSocket extends ClientWebSocket {

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the answer checker server.
     * @param parent
     *            The proxy server instance.
     */
    public AnswerClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parent) {
>>>>>>> origin/master:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerClientWebSocket.java
        super(destination, parent);
    }

    /**
     *  * Accepts messages and sends the request to the correct server and holds minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
    */
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final MultiConnectionState state = getStateFromId(AbstractServerWebSocketHandler.Decoder.parseRequest(buffer).getSessionInfo());

<<<<<<< HEAD:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerConnection.java
        final Request r = GeneralConnectionServer.Decoder.parseRequest(buffer);
        if (r.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(r);
            if (rsp != null) {
                try {
                    this.parentManager.send(rsp, r.getSessionInfo(), AnswerConnection.class);
=======
        final Request request = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
        if (request.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(request);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, request.getSessionInfo(), AnswerClientWebSocket.class);
>>>>>>> origin/master:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerClientWebSocket.java
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
<<<<<<< HEAD:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerConnection.java
        final Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
        GeneralConnectionServer.send(getConnectionFromState(state), result);
=======
        // strips away identification.
        final Request result = ProxyConnectionManager.createClientRequest(request);
        this.getParentServer().send(getConnectionFromState(state), result);
>>>>>>> origin/master:CourseSketchProjects/CourseSketchProxyServer/src/main/java/internalConnections/AnswerClientWebSocket.java
    }

}
