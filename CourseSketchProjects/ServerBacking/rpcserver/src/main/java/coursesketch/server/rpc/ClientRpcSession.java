package coursesketch.server.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClient;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * A subclass of socket session that handles Rpc data.
 *
 * Created by gigemjt on 10/19/14.
 */
public final class ClientRpcSession extends RpcSession {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientRpcSession.class);

    /**
     * The context of the socket.
     */
    private final RpcClientChannel session;

    private RpcCallback<Message.Request> callback;

    private Message.RequestService service;

    /**
     * Creates a wrapper around the {@link ClientRpcController}.
     * @param controller The context of the session (the socket itself).
     */
    ClientRpcSession(final RpcController controller) {
        super(controller);
        // This is null because it is required to be set to a value.
        session = null;
    }

    /**
     * Creates a wrapper around the {@link RpcClientChannel}.
     * @param rpcClientChannel {@link RpcClientChannel}.  Used for information about the socket created.
     */
    public ClientRpcSession(final RpcClientChannel rpcClientChannel) {
        super(null);
        this.session = rpcClientChannel;
    }

    @Override
    public void close() {
        getSession().close();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null}.
     */
    @Override
    public Future<Void> send(final Message.Request req) {
        if (service == null) {
            synchronized (this) {
                if (service == null) {
                    service = Message.RequestService.newStub(getSession());
                }
            }
        }

        service.sendMessage(getController(), req, callback);
        return null;
    }

    /**
     * Initiates the asynchronous transmission of a binary message. This method returns before the message is transmitted.
     * Developers may use the returned Future object to track progress of the transmission.
     *
     * @param buffer
     *         The data being sent.
     * @return the Future object representing the send operation.
     */
    @Override
    public Future<Void> send(final ByteBuffer buffer) {
        try {
            return send(Message.Request.parseFrom(buffer.array()));
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Unable to send request", e);
        }
        return null;
    }

    /**
     * Send a websocket Close frame, with status code.
     * <p/>
     * This will ensure a graceful close to the remote endpoint.
     *
     * @param statusCode
     *         The status code.
     * @param reason
     *         The (optional) reason. (can be null for no reason)
     * @see #close()
     */
    @Override
    public void close(final int statusCode, final String reason) {
        // rpc do not have status codes or reasons so we can call close.
        close();
    }

    public void addCallback(RpcCallback<Message.Request> callback) {
        this.callback = callback;
    }

    public RpcClientChannel getSession() {
        return session;
    }

    public RpcController getController() {
        if (getSession() == null) {
            return super.getController();
        } else {
            return session.newRpcController();
        }
    }

    /**
     * @return Information about the connected peer.  (What's on the other end)
     */
    protected PeerInfo getPeerInfo() {
        if (session != null) {
            return session.getPeerInfo();
        } else {
            return super.getPeerInfo();
        }
    }
}
