package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import protobuf.srl.request.Message;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketHandler extends AbstractServerWebSocketHandler {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent
     *         The parent servlet of this server.
     */
    protected ServerWebSocketHandler(final ISocketInitializer parent) {
        super(parent);
    }

    /**
     * Called when this server connects to a client.
     * @param ctx The context of the socket itself.
     * @param req The request that contains data about the upgrade request.
     */
    final void nettyOnConnect(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        onOpen(new NettySession(ctx));
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn
     *         the connection that is being opened.
     */
    @Override
    protected void openSession(final SocketSession conn) {

    }

    /**
     * Called if an error occurs.
     * @param session The socket context of the error.
     * @param cause The cause of the error.
     */
    final void nettyOnError(final ChannelHandlerContext session, final Throwable cause) {
        onError(new NettySession(session), cause);
    }

    /**
     * Called when an error occurs with the connection.
     *
     * @param session
     *         The session that has an error.
     * @param cause
     *         The actual error.
     */
    @Override
    protected void onError(final SocketSession session, final Throwable cause) {

    }

    /**
     * Called when the server receives a message.
     * @param session The socket context.
     * @param buf The binary message data.
     */
    final void nettyOnMessage(final ChannelHandlerContext session, final ByteBuffer buf) {
        onMessage(new NettySession(session), buf);
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * @param session
     *         the session object that created the message
     * @param req The protobuf request object that represents what was sent to the server
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void onMessage(final SocketSession session, final Message.Request req) {
        LOG.info("Request: {}", req);
    }

    /**
     * Called when the server or the client closes the connection.
     * @param session The socket session.
     * @param statusCode The code number that represents the reason for closing.
     * @param reason The human readable message that defines why the socket closed.
     */
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
