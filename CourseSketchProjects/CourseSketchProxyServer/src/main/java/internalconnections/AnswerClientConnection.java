package internalconnections;

import java.net.URI;
import java.nio.ByteBuffer;

import coursesketch.jetty.multiconnection.ClientConnection;
import coursesketch.jetty.multiconnection.ServerWebSocketHandler;
import interfaces.IServerWebSocketHandler;
import interfaces.MultiConnectionState;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import utilities.ConnectionException;
import utilities.TimeManager;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket()
public final class AnswerClientConnection extends ClientConnection {

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the answer checker server.
     * @param parent
     *            The proxy server instance.
     */
    public AnswerClientConnection(final URI destination, final ServerWebSocketHandler parent) {
        super(destination, parent);
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
     *
     * @param buffer
     *            The message that is received by this object.
     */
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final MultiConnectionState state = getStateFromId(IServerWebSocketHandler.Decoder.parseRequest(buffer).getSessionInfo());

        final Request request = IServerWebSocketHandler.Decoder.parseRequest(buffer);
        if (request.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(request);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, request.getSessionInfo(), AnswerClientConnection.class);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        // strips away identification.
        final Request result = ProxyConnectionManager.createClientRequest(request);
        ServerWebSocketHandler.send(getConnectionFromState(state), result);
    }

}
