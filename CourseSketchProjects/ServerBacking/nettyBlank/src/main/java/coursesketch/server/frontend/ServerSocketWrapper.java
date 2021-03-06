package coursesketch.server.frontend;

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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import utilities.ConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
/* package private! */ class ServerSocketWrapper extends SimpleChannelInboundHandler<Object> {

    /**
     * The path at which you connect to the websocket.
     */
    private static final String WEBSOCKET_PATH = "/websocket";

    /**
     * The number of bytes in the value for the length of the server.
     */
    private static final int NUM_BYTES_IN_LENGTH = 8;

    /**
     * True if the socket should be secured using SSL.
     */
    private final boolean isSecure;

    /**
     * An actual socket handler that is just wrapped by the.
     */
    private final ServerWebSocketHandler socketHandler;

    /**
     * Handles the handshake upgrade request.
     */
    private WebSocketServerHandshaker handshaker;

    /**
     * List of messages that are current fragmented.
     */
    private List<ByteBuffer> fragmentedList;

    /**
     * Set to true if the list should be flushed the next time the server receives a message.
     */
    private boolean flushFragmentedList;

    /**
     * @param handler The handler for the server side of the socket.
     * @param secure True if the socket should use SSL.
     */
    ServerSocketWrapper(final AbstractServerWebSocketHandler handler, final boolean secure) {
        socketHandler = (ServerWebSocketHandler) handler;
        isSecure = secure;
    }

    /**
     * Sends a response via Http if needed.
     * @param ctx The socket object.
     * @param req The request that came in as Http.
     * @param res The response in Http Format.
     */
    private static void sendHttpResponse(
            final ChannelHandlerContext ctx, final FullHttpRequest req, final FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status() != OK) {
            final ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
            ctx.write(res);
        }

        // Send the response and close the connection if necessary.
        final ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status() != OK) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p/>
     *
     * @param ctx
     *         the {@link io.netty.channel.ChannelHandlerContext} which this {@link io.netty.channel.SimpleChannelInboundHandler}
     *         belongs to
     * @param msg
     *         the message to handle
     */
    @Override
    protected final void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * Handles http request.
     * @param ctx The client socket context.
     * @param req The request itself.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
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

        if ("/favicon.ico".equals(req.uri())) {
            final FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        handShake(ctx, req);
    }

    /**
     * Called to initiate a handshake to upgrade into a webSocket.
     * @param ctx The context of the socket.
     * @param req The Http Request that contains the information about the upgrade.
     */
    private void handShake(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        // Handshake
        final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false, ServerWebSocketInitializer.MAX_FRAME_SIZE);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            socketHandler.nettyOnConnect(ctx, req);
        }
    }

    /**
     * Handles the communication of a single frame.
     * @param ctx The socket context.
     * @param frame The message.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {

        if (flushFragmentedList) {
            fragmentedList = null;
            flushFragmentedList = false;
        }

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
            if (isFirstOfMany(frame.content())) {
                addFrameToFragmentedList(frame);
                return;
            }
            onMessage(ctx, ((BinaryWebSocketFrame) frame));
            return;
        }

        if (frame instanceof ContinuationWebSocketFrame) {
            addFrameToFragmentedList(frame);
            if (frame.isFinalFragment()) {
                flushFragmentedList = true;
                socketHandler.nettyOnMessage(ctx, mergeList(fragmentedList));
                fragmentedList = null;
            }
            return;
        }

        final RuntimeException exp = new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                .getName()));
        socketHandler.nettyOnError(ctx, exp);
        throw exp;
    }

    /**
     * @param content An array of data from the client.
     * @return {@code true} If it is decided that this message is the first of multiple messages.
     */
    private boolean isFirstOfMany(final ByteBuf content) {
        if (content.readableBytes() < NUM_BYTES_IN_LENGTH) {
            return false;
        }
        final byte[] array = new byte[NUM_BYTES_IN_LENGTH];
        content.readBytes(array);

        final long messageLength = ByteBuffer.wrap(array).getLong();
        return messageLength > content.readableBytes();
    }

    /**
     * Adds a frame to a list of multiple frames to be merged together.
     * @param frame The frame that is part of a fragmented message.
     */
    private void addFrameToFragmentedList(final WebSocketFrame frame) {
        final byte[] buffer = new byte[frame.content().readableBytes()];
        frame.content().readBytes(buffer);

        if (fragmentedList == null) {
            fragmentedList = new ArrayList<>();
        }

        fragmentedList.add(ByteBuffer.wrap(buffer));
    }

    /**
     * Takes multiple frames and merge them into a single byte buffer.
     *
     * @param partialFrameData The list of fragmented frames converted into bytes.
     * @return A merged byte buffer.
     */
    private ByteBuffer mergeList(final List<ByteBuffer> partialFrameData) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (ByteBuffer socketFrame : partialFrameData) {
            try {
                stream.write(socketFrame.array());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ByteBuffer.wrap(stream.toByteArray());
    }

    /**
     * Closes the socket.
     * @param ctx The socket.
     * @param frame The message that represents the closing of the socket.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void close(final ChannelHandlerContext ctx, final CloseWebSocketFrame frame) {
        handshaker.close(ctx.channel(), frame.retain());
        socketHandler.nettyOnClose(ctx, frame.statusCode(), frame.reasonText());
    }

    /**
     * Called on message for binary data.
     *
     * @param ctx The socket.
     * @param frame The binary message.
     */
    @SuppressWarnings({ "PMD.UnusedPrivateMethod", "PMD.AvoidThrowingRawExceptionTypes" })
    private void onMessage(final ChannelHandlerContext ctx, final BinaryWebSocketFrame frame) {
        // This was the only way we were able to make the bytes able to be read.
        // There may be another way in the future to grab the bytes.
        final byte[] bytes = new byte[frame.content().readableBytes()];
        if (bytes.length > NUM_BYTES_IN_LENGTH) {
            frame.content().readBytes(bytes);
            socketHandler.nettyOnMessage(ctx, ByteBuffer.wrap(bytes, NUM_BYTES_IN_LENGTH, bytes.length - NUM_BYTES_IN_LENGTH));
        } else {
            final RuntimeException exp =
                    new RuntimeException(new ConnectionException("Invalid message contains less than NUM_BYTES_IN_LENGTH bytes"));
            socketHandler.nettyOnError(ctx, exp);
            throw exp;
        }
    }

    /**
     * @param req
     *         The http that is requesting the upgrade.
     * @return The location of the socket.
     */
    private String getWebSocketLocation(final FullHttpRequest req) {
        final String location = req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        if (isSecure) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
