package coursesketch.server.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.server.interfaces.SocketSession;
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
public final class RpcSession implements SocketSession {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RpcSession.class);

    /**
     * The context of the socket.
     */
    private final RpcClientChannel session;

    /**
     * The controller for the socket.
     */
    private ClientRpcController controller;

    /**
     * Creates a wrapper around the {@link ClientRpcController}.
     * @param controller The context of the session (the socket itself).
     */
    public RpcSession(final ClientRpcController controller) {
        this.controller = controller;
        // This is null because it is required to be set to a value.
        session = null;
    }

    /**
     * Creates a wrapper around the {@link RpcClientChannel}.
     * @param rpcClientChannel {@link RpcClientChannel}.  Used for information about the socket created.
     */
    public RpcSession(final RpcClientChannel rpcClientChannel) {
        this.session = rpcClientChannel;
    }

    /**
     * Get the address of the remote side.
     *
     * @return the remote side address
     */
    @Override
    public String getRemoteAddress() {
        return getPeerInfo().getHostName();
    }

    /**
     * Request a close of the current conversation with a normal status code and no reason phrase.
     * <p/>
     * This will ensure a graceful close to the remote endpoint.
     *
     * @see #close(int, String)
     */
    @Override
    public void close() {
        session.close();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null}.
     */
    @Override
    public Future<Void> send(final Message.Request req) {
        session.callMethod(Message.RequestService.getDescriptor().findMethodByName("sendMessage"),
                session.newRpcController(), req, req, null);
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

    /**
     * Checks if two {@RpcSessions} are equal.
     *
     * @param other
     *         A different RpcSession.
     * @return True if the {@link org.eclipse.jetty.websocket.api.Session} are equal.
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof RpcSession) {
            return getPeerInfo().equals(((RpcSession) other).getPeerInfo());
        }
        return false;
    }

    /**
     * @return Information about the connected peer.  (What's on the other end)
     */
    private PeerInfo getPeerInfo() {
        if (controller != null) {
            return controller.getRpcClient().getPeerInfo();
        }
        if (session != null) {
            return session.getPeerInfo();
        }
        return new PeerInfo();
    }

    /**
     * @return The hash code of the {@link org.eclipse.jetty.websocket.api.Session}.
     */
    @Override
    public int hashCode() {
        return getPeerInfo().hashCode();
    }
}
