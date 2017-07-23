package coursesketch.server.base;

import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * Overrides the AutoCloseable interface so it can be used in a try with resources block.
 * @author gigemjt
 */
class CloseableWebsocketClient extends WebSocketClient implements AutoCloseable {

    @Override
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.CommentRequired" })
    public final void close() throws Exception {
        this.stop();
    }

}
