package internalconnections;

import java.net.URI;

import coursesketch.jetty.multiconnection.ClientConnection;
import coursesketch.jetty.multiconnection.ServerWebSocketHandler;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket()
public class RecognitionClientConnection extends ClientConnection {

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.jetty.multiconnection.ClientConnection#connect()} or call
     * {@link coursesketch.jetty.multiconnection.ClientConnection#send(byte[])}.
     *
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parent
     *            The server that is using this connection wrapper.
     */
    public RecognitionClientConnection(final URI destination, final ServerWebSocketHandler parent) {
        super(destination, parent);
    }

}
