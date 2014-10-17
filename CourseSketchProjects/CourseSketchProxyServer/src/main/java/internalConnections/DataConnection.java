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
public final class DataConnection extends ConnectionWrapper {

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the database server.
     * @param parent
     *            The proxy server instance.
     */
    public DataConnection(final URI destination, final GeneralConnectionServer parent) {
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
        final Request req = GeneralConnectionServer.Decoder.parseRequest(buffer);

        if (req.getRequestType() == Request.MessageType.TIME) {

            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, req.getSessionInfo(), DataConnection.class);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            final MultiConnectionState state = getStateFromId(GeneralConnectionServer.Decoder.parseRequest(buffer).getSessionInfo());

            final Request request = GeneralConnectionServer.Decoder.parseRequest(buffer);
            // Strips away identification.
            final Request result = ProxyConnectionManager.createClientRequest(request);
            GeneralConnectionServer.send(getConnectionFromState(state), result);
        }
    }
}
