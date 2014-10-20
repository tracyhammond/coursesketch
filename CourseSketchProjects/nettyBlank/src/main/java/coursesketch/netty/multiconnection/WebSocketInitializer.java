package coursesketch.netty.multiconnection;

import interfaces.IMultiConnectionManager;
import interfaces.IServerWebSocket;
import interfaces.ISocketInitializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * Created by gigemjt on 10/19/14.
 */
public class WebSocketInitializer extends ChannelInitializer<SocketChannel> implements ISocketInitializer {
    private SslContext sslContext;

    public WebSocketInitializer(final long timeOut, final boolean isSecure, final boolean isLocal) {

    }

    /**
     * Stops the socket, and the server and drops all connections.
     */
    @Override
    public void stop() {

    }

    /**
     * This is called when the reconnect command is executed.
     * <p/>
     * By default this drops all connections and then calls
     *
     * @see IMultiConnectionManager#connectServers(interfaces.IServerWebSocket)
     */
    @Override
    public void reconnect() {

    }

    /**
     * @return The current number of current connections.
     */
    @Override
    public int getCurrentConnectionNumber() {
        return 0;
    }

    /**
     * Override this method to create a subclass of the MultiConnectionManager.
     *
     * @param connectLocally True if the connection is acting as if it is on a local computer (used for testing)
     * @param iSecure        True if the connection is using SSL.
     * @return An instance of the {@link interfaces.IMultiConnectionManager}
     */
    @Override
    public IMultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean iSecure) {
        return null;
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link interfaces.IServerWebSocket}
     */
    @Override
    public IServerWebSocket createServerSocket() {
        return null;
    }

    public void setSslContext(final SslContext iSslContext) {
        this.sslContext = iSslContext;
    }

    /**
     * This method will be called once the {@link io.netty.channel.Channel} was registered. After the method returns this instance
     * will be removed from the {@link ChannelPipeline} of the {@link io.netty.channel.Channel}.
     *
     * @param ch the {@link io.netty.channel.Channel} which was registered.
     * @throws Exception is thrown if an error occurs. In that case the {@link io.netty.channel.Channel} will be closed.
     */
    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            pipeline.addFirst("ssl", sslContext.newHandler(ch.alloc()));
            //pipeline.addLast("ssl", new SslHandler(sslCtx)); //sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new Socketwrapper(createServerSocket()));
    }
}
