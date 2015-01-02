package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import netty.WebSocketServerIndexPage;

import java.nio.ByteBuffer;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by gigemjt on 10/19/14.
 *
 * This channel should be shareable!
 */
@ChannelHandler.Sharable
/* package private! */ class ServerSocketWrapper extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private final boolean isSecure;
    /**
     * An actual socket handler that is just wrapped by the
     */
    private final ServerWebSocketHandler socketHandler;
    private WebSocketServerHandshaker handshaker;

    /**
     * @param handler
     * @param secure
     */
    ServerSocketWrapper(final AbstractServerWebSocketHandler handler, final boolean secure) {
        socketHandler = (ServerWebSocketHandler) handler;
        isSecure = secure;
    }

    private static void sendHttpResponse(
            final ChannelHandlerContext ctx, final FullHttpRequest req, final FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status() != OK) {
            final ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.status() != OK) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p/>
     * Is called for each message of type {@link I}.
     *
     * @param ctx
     *         the {@link io.netty.channel.ChannelHandlerContext} which this {@link io.netty.channel.SimpleChannelInboundHandler}
     *         belongs to
     * @param msg
     *         the message to handle
     * @throws Exception
     *         is thrown if an error occurred
     */
    @Override
    protected final void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        // Handle a bad request.

        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/demo".equals(req.uri())) {
            ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }

        if ("/favicon.ico".equals(req.uri())) {
            final FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        handShake(ctx, req);
    }

    /**
     * Called to initiate a handshake to upgrade into a webSocket.
     */
    private void handShake(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        // Handshake
        final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            socketHandler.nettyOnConnect(ctx, req);
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            close(ctx, (CloseWebSocketFrame) frame);
            return;
        }

        // ping pong circule
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof BinaryWebSocketFrame) {
            onMessage(ctx, ((BinaryWebSocketFrame) frame));
            return;
        }

        if (!(frame instanceof BinaryWebSocketFrame)) {
            final RuntimeException exp = new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
            socketHandler.nettyOnError(ctx, exp);
            throw exp;
        }
    }

    private void close(final ChannelHandlerContext ctx, final CloseWebSocketFrame frame) {
        handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        socketHandler.nettyOnClose(ctx, frame.statusCode(), frame.reasonText());
    }

    private void onMessage(final ChannelHandlerContext ctx, final BinaryWebSocketFrame frame) {
        // This was the only way we were able to make the bytes able to be read.
        // There may be another way in the future to grab the bytes.
        final byte[] bytes = new byte[frame.content().readableBytes()];
        frame.content().readBytes(bytes);
        socketHandler.nettyOnMessage(ctx, ByteBuffer.wrap(bytes));
    }

    /**
     * @param req
     *         the http that is requesting the upgrade.
     * @return the location of the socket.
     */
    private String getWebSocketLocation(final FullHttpRequest req) {
        final String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        if (isSecure) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

}
