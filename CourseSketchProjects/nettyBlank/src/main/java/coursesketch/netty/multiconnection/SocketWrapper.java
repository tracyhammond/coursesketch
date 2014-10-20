package coursesketch.netty.multiconnection;

import interfaces.IServerWebSocket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by gigemjt on 10/19/14.
 */
/* package private! */class Socketwrapper extends SimpleChannelInboundHandler<Object> {

    /**
     * An actual socket handler that is just wrapped by the
     */
    final IServerWebSocket socketHandler;

    public Socketwrapper(final IServerWebSocket handler) {
        socketHandler = handler;
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p/>
     * Is called for each message of type {@link I}.
     *
     * @param ctx the {@link io.netty.channel.ChannelHandlerContext} which this {@link io.netty.channel.SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {

    }
}
