package connection;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;
import utilities.ConnectionException;
import utilities.TimeManager;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded.
 */
@WebSocket()
public final class DataClientWebSocket extends ClientWebSocket {

    /**
     * Declaration and Definition of Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataClientWebSocket.class);

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ClientWebSocket#connect()} or call
     * {@link ClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parentServer
     *            The server that is using this connection wrapper.
     */
    public DataClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parentServer) {
        super(destination, parentServer);
    }

    /**
     * {@inheritDoc}<br>
     *
     * Only handles request for setting time.
     */
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final Request req = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
        if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, req.getSessionInfo(), DataClientWebSocket.class);
                } catch (ConnectionException e) {
                    LOG.info("Exception: {}", e);
                }
            }
        }
    }
}
