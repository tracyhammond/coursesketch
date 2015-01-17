package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import utilities.ConnectionException;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 10/22/14.
 */
public class ClientWebSocket extends AbstractClientWebSocket {

    /**
     * The code that is used by the Html aggregator.
     */
    private static final int OBJECT_AGGREGATOR_CODE = 8192;

    /**
     * An eventloop?
     * Something needs to be done to ensure that it closes gracefully.
     */
    private EventLoopGroup group;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()} or call
     * {@link coursesketch.server.interfaces.AbstractClientWebSocket#send(java.nio.ByteBuffer)}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer
     *         The server that is using this connection wrapper.
     */
    protected ClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws ConnectionException
     *         Throws an exception if an error occurs during the connection attempt.
     */
    @Override
    protected final void connect() throws ConnectionException {
        final SslContext sslCtx = null;
        /*
        if (getParentServer().) {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslCtx = null;
        }
        */
        final InetSocketAddress remoteAddress = new InetSocketAddress(getURI().getHost(), getURI().getPort());
        if (remoteAddress.isUnresolved()) {
            throw new ConnectionException("Remote address does not exist " + remoteAddress.getHostString());
        }
        group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final ClientWebSocketWrapper handler =
                    new ClientWebSocketWrapper(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    getURI(), WebSocketVersion.V13, null, false, new DefaultHttpHeaders()), this);

            final Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) {
                            final ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addFirst(sslCtx.newHandler(ch.alloc(), getURI().getHost(), getURI().getPort()));
                            }
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(OBJECT_AGGREGATOR_CODE),
                                    //new WebSocketClientCompressionHandler(),
                                    handler);
                        }
                    });
            System.out.println(this.getClass().getSimpleName() + " connecting to[" + getURI() + "]");
            final Channel ch = b.connect(getURI().getHost(), getURI().getPort()).sync().channel();
            handler.handshakeFuture().sync();
            System.err.println("Something happened?" + ch.metadata());
        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer
     *         The message that is received by this object.
     */
    @Override protected void onMessage(final ByteBuffer buffer) {

    }

    /**
     * @param ctx
     *         the context for the channel.
     */
    final void nettyOnOpen(final ChannelHandlerContext ctx) {
        onOpen(new NettySession(ctx));
    }

    /**
     * @param ctx
     *         The context of the socket
     * @param cause
     *         The error that was thrown
     */
    final void nettyOnError(final ChannelHandlerContext ctx, final Throwable cause) {
        onError(new NettySession(ctx), cause);
    }

    /**
     * @param ctx
     *         The context of the socket.
     * @param wrap
     *         The binary data of the message.
     */
    final void nettyOnMessage(final ChannelHandlerContext ctx, final ByteBuffer wrap) {
        onMessage(wrap);
    }
}
