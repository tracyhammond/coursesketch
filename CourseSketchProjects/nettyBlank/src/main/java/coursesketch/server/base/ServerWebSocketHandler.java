package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import protobuf.srl.request.Message;

import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketHandler extends AbstractServerWebSocketHandler {

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent The parent servlet of this server.
     */
    protected ServerWebSocketHandler(final ISocketInitializer parent) {
        super(parent);
    }

    final void nettyOnConnect(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        onOpen(new NettySession(ctx));
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn the connection that is being opened.
     */
    @Override
    protected void openSession(final SocketSession conn) {

    }

    final void nettyOnError(final ChannelHandlerContext session, final Throwable cause) {
        onError(new NettySession(session), cause);
    }

    /**
     * Called when an error occurs with the connection.
     *
     * @param session The session that has an error.
     * @param cause   The actual error.
     */
    @Override
    protected void onError(final SocketSession session, final Throwable cause) {

    }

    final void nettyOnMessage(final ChannelHandlerContext session, final ByteBuffer buf) {
        onMessage(new NettySession(session), buf);
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * @param session the session object that created the message
     * @param req
     */
    @Override
    protected final void onMessage(final SocketSession session, final Message.Request req) {
        System.out.println(req);
    }

    final void nettyOnClose(final ChannelHandlerContext session, final int statusCode, final String reason) {
        super.onClose(new NettySession(session), statusCode, reason);
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    protected void onStop() {

    }

    /**
     * @return The {@link coursesketch.server.interfaces.MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected final MultiConnectionManager getConnectionManager() {
        return ((ServerWebSocketInitializer) getParentServer()).getManager();
    }
}
