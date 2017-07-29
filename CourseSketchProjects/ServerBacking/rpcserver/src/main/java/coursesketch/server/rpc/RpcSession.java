package coursesketch.server.rpc;

import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import coursesketch.server.interfaces.SocketSession;

public abstract class RpcSession implements SocketSession {
    /**
     * The controller for the socket.
     */
    protected RpcController controller;

    RpcSession(RpcController controller) {
        this.controller = controller;
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
     * @return Information about the connected peer.  (What's on the other end)
     */
    private PeerInfo getPeerInfo() {
        if (controller != null && controller instanceof ClientRpcController) {
            return ((ClientRpcController) controller).getRpcClient().getPeerInfo();
        }
        return new PeerInfo();
    }

    /**
     * Checks if two {@link ClientRpcSession} are equal.
     *
     * @param other
     *         A different ClientRpcSession.
     * @return True if the {@link PeerInfo} are equal.
     */
    @Override
    public boolean equals(final Object other) {
        return other instanceof ClientRpcSession && getPeerInfo().equals(((RpcSession) other).getPeerInfo());
    }

    /**
     * @return The hash code of the {@link PeerInfo}.
     */
    @Override
    public int hashCode() {
        return getPeerInfo().hashCode();
    }

    protected RpcController getController() {
        return controller;
    }
}
