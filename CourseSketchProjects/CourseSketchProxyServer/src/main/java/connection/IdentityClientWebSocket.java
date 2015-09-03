package connection;

import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 9/3/15.
 */
public class IdentityClientWebSocket extends ClientWebSocket {
    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link ClientWebSocket#connect()} or call
     * {@link ClientWebSocket#send(ByteBuffer)}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer
     */
    public IdentityClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }
}
