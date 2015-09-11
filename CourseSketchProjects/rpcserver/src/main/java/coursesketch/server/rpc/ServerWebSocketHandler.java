package coursesketch.server.rpc;

import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;

/**
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketHandler extends AbstractServerWebSocketHandler {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketHandler.class);

    /**
     * A constructor that accepts a servlet.
     *
     * @param parent
     *         The parent servlet of this server.
     * @param info  {@link ServerInfo} Contains all of the information about the server.
     */
    protected ServerWebSocketHandler(final ISocketInitializer parent, final ServerInfo info) {
        super(parent, info);
    }

    /**
     * Called when this server connects to a client.
     * @param controller The context of the socket itself.
     * @param req The request that contains data about the upgrade request.
     */
    final void rpcOnConnect(final RpcSession controller, final Message.Request req) {
        onOpen(controller);
    }

    /**
     * Called after onOpen Finished. Can be over written.
     *
     * @param conn
     *         the connection that is being opened.
     */
    @Override
    protected void openSession(final SocketSession conn) {

    }

    /**
     * Called if an error occurs.
     * @param session The socket context of the error.
     * @param cause The cause of the error.
     */
    final void rpcOnError(final RpcController session, final Throwable cause) {
        onError(new RpcSession((ClientRpcController) session), cause);
    }

    /**
     * Called when an error occurs with the connection.
     *
     * @param session
     *         The session that has an error.
     * @param cause
     *         The actual error.
     */
    @Override
    protected void onError(final SocketSession session, final Throwable cause) {

    }

    /**
     * Called when the server receives a message.
     * @param session The socket context.
     * @param req The protobuf request object that represents what was sent to the server.
     */
    final void rpcOnMessage(final RpcController session, final Message.Request req) {
        onMessage(new RpcSession((ClientRpcController) session), req);
    }

    /**
     * Takes a request and allows overriding so that subclass servers can handle
     * messages.
     *
     * @param session
     *         the session object that created the message
     * @param req The protobuf request object that represents what was sent to the server
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void onMessage(final SocketSession session, final Message.Request req) {
        LOG.info("Request: {}", req);
    }

    /**
     * Called when the server or the client closes the connection.
     * @param session The socket session.
     * @param statusCode The code number that represents the reason for closing.
     * @param reason The human readable message that defines why the socket closed.
     */
    final void rpcOnClose(final RpcSession session, final int statusCode, final String reason) {
        super.onClose(session, statusCode, reason);
    }

    /**
     * Available for override.  Called after the server is stopped.
     */
    @Override
    protected void onStop() {

    }

    /**
     * @return The {@link coursesketch.server.interfaces.MultiConnectionManager} or subclass so it can be used
     * in this instance.
     */
    protected final MultiConnectionManager getConnectionManager() {
        return ((ServerWebSocketInitializer) getParentServer()).getManager();
    }
}
