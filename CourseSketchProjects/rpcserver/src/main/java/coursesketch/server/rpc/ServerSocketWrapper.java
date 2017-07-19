package coursesketch.server.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.listener.TcpConnectionEventListener;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import utilities.ConnectionException;
import utilities.TimeManager;

/**
 * A wrapper around a {@link ServerWebSocketHandler}.
 *
 * Created by gigemjt on 10/19/14.
 *
 * This channel should be shareable!
 */
@ChannelHandler.Sharable
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
/* package-private */ class ServerSocketWrapper extends Message.RequestService implements TcpConnectionEventListener, CourseSketchRpcService {


    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerSocketWrapper.class);

    /**
     * The actual socket handler that this object wraps around.
     */
    private final ServerWebSocketHandler socketHandler;

    /**
     * Constructor for {@ServerSocketWrapper}.
     *
     * @param handler The handler for the server side of the socket.
     * @param secure True if the socket should use SSL.
     */
    ServerSocketWrapper(final AbstractServerWebSocketHandler handler, final boolean secure) {
        socketHandler = (ServerWebSocketHandler) handler;
        LOG.debug("Server is running securely? {}", secure);
    }

    /**
     * <code>rpc sendMessage(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * Default part of this service.
     *
     * @param controller The controller that was used to send the message.
     * @param request The request that was sent.  In this case it is a request meant for time management.
     * @param rpcCallback Where you set the result.
     */
    @Override public void sendMessage(final RpcController controller, final Message.Request request, final RpcCallback<Message.Request> rpcCallback) {
        if (controller.failed() || controller.isCanceled()) {
            socketHandler.rpcOnError(controller, new ConnectionException(controller.errorText()));
        } else {
            socketHandler.rpcOnMessage(controller, request);
        }
    }

    /**
     * <code>rpc sendTimeRequest(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * Parses the time request and then sends one right back.
     *
     * @param controller The controller that was used to send the message.
     * @param request The request that was sent.
     * @param rpcCallback Where you set the result.
     */
    @Override public void sendTimeRequest(final RpcController controller, final Message.Request request,
            final RpcCallback<Message.Request> rpcCallback) {
        rpcCallback.run(TimeManager.decodeRequest(request));
    }

    /**
     * Notification that a RpcClientChannel has closed. This
     * happens when a remote peer closes an open RpcClientChannel
     * or when the TCP connection on which the RpcClientChannel
     * is built up on breaks - due to JVM crash / kill or network
     * problem. The underlying reason for RpcClientChannel closure is not
     * discernible.
     *
     * @param rpcClientChannel The client channel that has closed.
     */
    @Override public void connectionClosed(final RpcClientChannel rpcClientChannel) {
        socketHandler.rpcOnClose(new RpcSession(rpcClientChannel), -1, "for an unknown reason");
    }

    /**
     * Notification that a RpcClientChannel has been opened. This
     * happens once a TCP connection is opened and the RPC handshake
     * is successfully completed.
     *
     * @param rpcClientChannel The client channel that has opened.
     */
    @Override public void connectionOpened(final RpcClientChannel rpcClientChannel) {
        socketHandler.rpcOnConnect(new RpcSession(rpcClientChannel), null);
    }

    /**
     * Sets the object that initializes this service.
     *
     * @param socketInitializer The Initializer that created this service. (It is ignored in the wrapper).
     */
    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        // This is left blank because this class is a wrapper. The actual handler contains a socket initializer
    }
}
