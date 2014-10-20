package internalconnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import connection.ConnectionException;
import connection.TimeManager;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket()
<<<<<<< HEAD
public class AnswerConnection extends ConnectionWrapper {

=======
public final class AnswerConnection extends ConnectionWrapper {

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the answer checker server.
     * @param parent
     *            The proxy server instance.
     */
>>>>>>> master
    public AnswerConnection(final URI destination, final GeneralConnectionServer parent) {
        super(destination, parent);
    }

    /**
<<<<<<< HEAD
     *  * Accepts messages and sends the request to the correct server and holds minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
    */
=======
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
     *
     * @param buffer
     *            The message that is received by this object.
     */
>>>>>>> master
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final MultiConnectionState state = getStateFromId(GeneralConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());

<<<<<<< HEAD
        final Request r = GeneralConnectionServer.Decoder.parseRequest(buffer);
        if (r.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(r);
            if (rsp != null) {
                try {
                    this.parentManager.send(rsp, r.getSessionInfo(), AnswerConnection.class);
=======
        final Request request = GeneralConnectionServer.Decoder.parseRequest(buffer);
        if (request.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(request);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, request.getSessionInfo(), AnswerConnection.class);
>>>>>>> master
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
<<<<<<< HEAD
        final Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
        GeneralConnectionServer.send(getConnectionFromState(state), result);
    }

}
=======
        // strips away identification.
        final Request result = ProxyConnectionManager.createClientRequest(request);
        GeneralConnectionServer.send(getConnectionFromState(state), result);
    }

}
>>>>>>> master
