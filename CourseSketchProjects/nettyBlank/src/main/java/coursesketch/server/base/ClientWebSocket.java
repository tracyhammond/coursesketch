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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

/**
 * Created by gigemjt on 10/22/14.
 */
public class ClientWebSocket extends AbstractClientWebSocket {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientWebSocket.class);

    /**
     * The code that is used by the Html aggregator.
     */
    private static final int OBJECT_AGGREGATOR_CODE = 8192;

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
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final ClientWebSocketWrapper handler =
                    new ClientWebSocketWrapper(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    getURI(), WebSocketVersion.V13, null, false, new DefaultHttpHeaders()), this);

            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @SuppressWarnings("PMD.CommentRequired")
                        @Override
                        protected void initChannel(final SocketChannel channel) {
                            final ChannelPipeline pipeline = channel.pipeline();
                            if (sslCtx != null) {
                                pipeline.addFirst(sslCtx.newHandler(channel.alloc(), getURI().getHost(), getURI().getPort()));
                            }
                            pipeline.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(OBJECT_AGGREGATOR_CODE),
                                    //new WebSocketClientCompressionHandler(),
                                    handler);
                        }
                    });
            LOG.info("{} connecting to[ {} ]", this.getClass().getSimpleName() , getURI());
            bootstrap.connect(getURI().getHost(), getURI().getPort()).sync().channel();
            handler.handshakeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
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
