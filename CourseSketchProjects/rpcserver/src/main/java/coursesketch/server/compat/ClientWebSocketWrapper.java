package coursesketch.server.compat;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.listener.TcpConnectionEventListener;
import coursesketch.server.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import utilities.TimeManager;

/**
 * Created by gigemjt on 10/23/14.
 */
class ClientWebSocketWrapper extends Message.RequestService implements TcpConnectionEventListener {

    /**
     * Declaration/Definition of Logger!
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientWebSocketWrapper.class);


    /**
     * Handles all messages sent by the socket.
     */
    private final ClientWebSocket socketHandler;

    /**
     * The client rpc channel.
     *
     * This is the connection to the server and contains methods used to manage that conneciton
     */
    private final RpcClientChannel channel;

    /**
     * Wraps around the {@link ClientWebSocket}.
     *
     * @param channel
     *         The handshake that controls the web-socket.
     * @param clientWebSocket
     *         The object that handles the actual socket communication.
     */
    ClientWebSocketWrapper(final RpcClientChannel channel, final ClientWebSocket clientWebSocket) {
        this.channel = channel;
        socketHandler = clientWebSocket;
    }

    /**
     * <code>rpc sendMessage(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * @param controller The
     * @param request
     * @param done
     */
    @Override public void sendMessage(final RpcController controller, final Message.Request request, final RpcCallback<Message.Request> done) {
        socketHandler.onMessage(request.toByteString().asReadOnlyByteBuffer());
    }

    /**
     * <code>rpc sendTimeRequest(.protobuf.srl.request.Request) returns (.protobuf.srl.request.Request);</code>.
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override public void sendTimeRequest(final RpcController controller, final Message.Request request, final RpcCallback<Message.Request> done) {
        done.run(TimeManager.decodeRequest(request));
    }

    /**
     * Notification that a RpcClientChannel has closed. This
     * happens when a remote peer closes an open RpcClientChannel
     * or when the TCP connection on which the RpcClientChannel
     * is built up on breaks - due to JVM crash / kill or network
     * problem. The underlying reason for RpcClientChannel closure is not
     * discernible.
     *
     * @param rpcClientChannel
     */
    @Override public void connectionClosed(final RpcClientChannel rpcClientChannel) {
        socketHandler.onClose(-1, "Idk");
    }

    /**
     * Notification that a RpcClientChannel has been opened. This
     * happens once a TCP connection is opened and the RPC handshake
     * is successfully completed.
     *
     * @param rpcClientChannel
     */
    @Override public void connectionOpened(final RpcClientChannel rpcClientChannel) {
        socketHandler.onOpen(new RpcSession(rpcClientChannel));
    }
}
