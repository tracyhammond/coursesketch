package coursesketch.server.rpc;

import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClient;
import com.googlecode.protobuf.pro.duplex.execute.ServerRpcController;
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
    protected PeerInfo getPeerInfo() {
        if (controller != null) {
            RpcClient rpcClient = null;
            if (controller instanceof ClientRpcController) {
                rpcClient = ((ClientRpcController) controller).getRpcClient();
            } else if (controller instanceof ServerRpcController) {
                rpcClient = ((ServerRpcController) controller).getRpcClient();
            }
            if (rpcClient != null) {
                return rpcClient.getPeerInfo();
            }
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
        int hashCode = getPeerInfo().hashCode();
        return hashCode;
    }

    protected RpcController getController() {
        return controller;
    }
}
