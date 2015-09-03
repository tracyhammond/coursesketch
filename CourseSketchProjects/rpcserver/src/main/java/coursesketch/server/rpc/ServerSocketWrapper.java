package coursesketch.server.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.listener.TcpConnectionEventListener;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import io.netty.channel.ChannelHandler;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 10/19/14.
 *
 * This channel should be shareable!
 */
@ChannelHandler.Sharable
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
/* package private! */ class ServerSocketWrapper extends Message.RequestService implements TcpConnectionEventListener {

    /**
     * The path at which you connect to the websocket.
     */
    private static final String WEBSOCKET_PATH = "/websocket";

    /**
     * True if the socket should be secured using SSL.
     */
    private final boolean isSecure;

    /**
     * An actual socket handler that is just wrapped by the.
     */
    private final DefaultRpcHandler socketHandler;

    /**
     * @param handler The handler for the server side of the socket.
     * @param secure True if the socket should use SSL.
     */
    ServerSocketWrapper(final AbstractServerWebSocketHandler handler, final boolean secure) {
        socketHandler = (DefaultRpcHandler) handler;
        isSecure = secure;
    }

    /**
     * <code>rpc sendMessage(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void sendMessage(final RpcController controller, final Message.Request request, final RpcCallback<Message.Request> done) {
        if (controller.failed() || controller.isCanceled()) {
            socketHandler.rpcOnError(controller, new Exception(controller.errorText()));
        } else {
            socketHandler.rpcOnMessage(controller, request);
        }
    }

    @Override public void connectionClosed(final RpcClientChannel rpcClientChannel) {
        socketHandler.rpcOnClose(new RpcSession(rpcClientChannel), -1, "Idk");
    }

    @Override public void connectionOpened(final RpcClientChannel rpcClientChannel) {
        socketHandler.rpcOnConnect(new RpcSession(rpcClientChannel), null);
    }
}
