package coursesketch.server.compat;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.listener.TcpConnectionEventListener;
import coursesketch.server.rpc.RpcSession;
import protobuf.srl.request.Message;
import utilities.TimeManager;

/**
 * Wraps around the request service to allow the handler to extend the abstract handler.
 *
 * Created by gigemjt on 10/23/14.
 */
class ClientWebSocketWrapper extends Message.RequestService implements TcpConnectionEventListener {

    /**
     * Handles all messages sent by the socket.
     */
    private final ClientWebSocket socketHandler;

    /**
     * Wraps around the {@link ClientWebSocket}.
     *
     * @param clientWebSocket
     *         The object that handles the actual socket communication.
     */
    ClientWebSocketWrapper(final ClientWebSocket clientWebSocket) {
        socketHandler = clientWebSocket;
    }

    /**
     * <code>rpc sendMessage(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * @param controller The controller that was used to send the message.
     * @param request The request that was sent.
     * @param rpcCallback Call {@link RpcCallback#run(Object)} to send back the result.
     */
    @Override public void sendMessage(final RpcController controller, final Message.Request request, final RpcCallback<Message.Request> rpcCallback) {
        socketHandler.onMessage(request.toByteString().asReadOnlyByteBuffer());
    }

    /**
     * <code>rpc sendTimeRequest(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * @param controller The controller that was used to send the message.
     * @param request The request that was sent.  In this case it is a request meant for time management.
     * @param rpcCallback Call {@link RpcCallback#run(Object)} to send back the result.
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
        socketHandler.onClose(-1, "Idk");
    }

    /**
     * Notification that a RpcClientChannel has been opened. This
     * happens once a TCP connection is opened and the RPC handshake
     * is successfully completed.
     *
     * @param rpcClientChannel The client channel that has opened.
     */
    @Override public void connectionOpened(final RpcClientChannel rpcClientChannel) {
        socketHandler.onOpen(new RpcSession(rpcClientChannel));
    }
}
