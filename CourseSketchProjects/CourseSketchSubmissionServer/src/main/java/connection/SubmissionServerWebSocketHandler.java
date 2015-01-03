package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.SocketSession;
import handlers.DataRequestHandler;
import handlers.SubmissionRequestHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.request.Message.Request;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public final class SubmissionServerWebSocketHandler extends ServerWebSocketHandler {

    /**
     * A constructor that accepts a servlet.
     * @param parent The parent servlet of this server.
     */
    public SubmissionServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onMessage(final SocketSession conn, final Request req) {
        /**
         * Attempts to save the submission, which can be either a solution or an experiment.
         * If it is an insertion and not an update then it will send the key to the database
         */
        if (req.getRequestType() == Request.MessageType.SUBMISSION) {
            final Request result = SubmissionRequestHandler.handleRequest(req, getConnectionManager());
            if (result != null) {
                send(conn, result);
            }
        }

        if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
            final Request result = DataRequestHandler.handleRequest(req);
            if (result != null) {
                send(conn, result);
            }
        }

    }
}
