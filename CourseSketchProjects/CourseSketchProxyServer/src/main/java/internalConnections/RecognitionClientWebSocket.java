package internalconnections;

import java.net.URI;

import coursesketch.server.base.ClientWebSocket;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket()
public class RecognitionClientWebSocket extends ClientWebSocket {

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.base.ClientWebSocket#connect()} or call
     * {@link coursesketch.server.base.ClientWebSocket#send(byte[])}.
     *
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parent
     *            The server that is using this connection wrapper.
     */
    public RecognitionClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parent) {
        super(destination, parent);
    }

}
