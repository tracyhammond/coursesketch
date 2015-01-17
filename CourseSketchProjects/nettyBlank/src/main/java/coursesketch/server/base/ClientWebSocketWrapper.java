package coursesketch.server.base;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 10/23/14.
 */
class ClientWebSocketWrapper extends SimpleChannelInboundHandler<Object> {
    /**
     * The object that performs the upgrade handshake.
     */
    private final WebSocketClientHandshaker handshaker;

    /**
     * Handles all messages sent by the socket.
     */
    private final ClientWebSocket socketHandler;

    /**
     * Returned during the handshake to know if the handshake was successful.
     */
    private ChannelPromise handshakeFuture;

    /**
     * Wraps around the {@link ClientWebSocket}.
     *
     * @param webSocketClientHandshaker
     *         The handshake that upgrades to a web-socket.
     * @param clientWebSocket
     *         The object that handles the actual socket communication.
     */
    public ClientWebSocketWrapper(final WebSocketClientHandshaker webSocketClientHandshaker, final ClientWebSocket clientWebSocket) {
        handshaker = webSocketClientHandshaker;
        socketHandler = clientWebSocket;
    }

    /**
     * @return {@link io.netty.channel.ChannelPromise}
     */
    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        socketHandler.onClose(0, null);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        socketHandler.nettyOnError(ctx, cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            socketHandler.nettyOnOpen(ctx);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            final FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status()
                            + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        final WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
            System.out.println("WebSocket Client received binary");
            onMessage(ctx, ((BinaryWebSocketFrame) frame));
            return;
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
            socketHandler.onClose(((CloseWebSocketFrame) frame).statusCode(), ((CloseWebSocketFrame) frame).reasonText());
        } else {
            final RuntimeException exp = new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
            socketHandler.nettyOnError(ctx, exp);
            throw exp;
        }
    }

    /**
     * Gets the message from the actual server and sends it to the web socket.
     *
     * @param ctx
     *         THe context of the socket
     * @param frame
     *         The specific binary data.
     */
    private void onMessage(final ChannelHandlerContext ctx, final BinaryWebSocketFrame frame) {
        // This was the only way we were able to make the bytes able to be read.
        // There may be another way in the future to grab the bytes.
        final byte[] bytes = new byte[frame.content().readableBytes()];
        frame.content().readBytes(bytes);
        socketHandler.nettyOnMessage(ctx, ByteBuffer.wrap(bytes));
    }
}
