package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import utilities.ConnectionException;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 10/22/14.
 */
public class ClientWebSocket extends AbstractClientWebSocket {
    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()} or call
     * {@link coursesketch.server.interfaces.AbstractClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param iDestination  The location the server is going as a URI. ex:
     *                      http://example.com:1234
     * @param iParentServer
     *                      The server that is using this connection wrapper.
     */
    protected ClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws ConnectionException Throws an exception if an error occurs during the connection attempt.
     */
    @Override protected void connect() throws ConnectionException {

    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer The message that is received by this object.
     */
    @Override protected void onMessage(final ByteBuffer buffer) {

    }
}
