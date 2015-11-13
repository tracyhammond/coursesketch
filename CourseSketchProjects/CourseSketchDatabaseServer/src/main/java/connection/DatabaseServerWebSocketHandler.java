package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.SocketSession;
import handlers.DataInsertHandler;
import handlers.DataRequestHandler;
import handlers.DataUpdateHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.request.Message.Request;
import utilities.TimeManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public class DatabaseServerWebSocketHandler extends ServerWebSocketHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseServerWebSocketHandler.class);
    /**
     * @param parent Passes it up to super constructor.
     */
    public DatabaseServerWebSocketHandler(final ServerWebSocketInitializer parent) {
        super(parent, parent.getServerInfo());
    }

    /**
     * Sends a time to its client when it is opened. To sync the times.
     * @param conn The connection that is opened.
     */
    @Override
    protected final void openSession(final SocketSession conn) {
        LOG.info("Sending the connection time to the client");
        send(conn, TimeManager.serverSendTimeToClient());
    }

    /**
     * @param conn The connection the sent the message.
     * @param req The message contents bundled as a request.
     */
    @Override
    public final void onMessage(final SocketSession conn, final Request req) {
        if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
            DataRequestHandler.handleRequest(req, conn, super.getConnectionToId().get(conn).getSessionId(), getConnectionManager());
        } else if (req.getRequestType() == Request.MessageType.DATA_INSERT) {
            DataInsertHandler.handleData(req, conn);
        } else if (req.getRequestType() == Request.MessageType.DATA_UPDATE) {
            DataUpdateHandler.handleData(req, conn);
        } else if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                send(conn, rsp);
            }
        }
        LOG.info("Finished looking at query {}", req); // Is this what you meant by print out request type??
    }
}
