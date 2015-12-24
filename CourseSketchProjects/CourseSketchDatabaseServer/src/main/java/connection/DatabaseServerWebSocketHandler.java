package connection;

import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.auth.MongoOptionChecker;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import handlers.DataInsertHandler;
import handlers.DataRequestHandler;
import handlers.DataUpdateHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;
import utilities.TimeManager;

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
     * Used to authenticate users in the server.
     */
    private Authenticator auth;

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
        final Institution instance = MongoInstitution.getInstance(getAuthInstance());
        if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
            DataRequestHandler.handleRequest(req, conn, instance, super.getConnectionToId().get(conn).getSessionId(), getConnectionManager());
        } else if (req.getRequestType() == Request.MessageType.DATA_INSERT) {
            DataInsertHandler.handleData(req, conn, instance);
        } else if (req.getRequestType() == Request.MessageType.DATA_UPDATE) {
            DataUpdateHandler.handleData(req, conn, instance);
        } else if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                send(conn, rsp);
            }
        }
        LOG.info("Finished looking at query {}", req); // Is this what you meant by print out request type??
    }

    /**
     * @return An instance of the authentication client. Creates it if it does not exist.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Double-checked_locking">Double Checked Locking</a>.
     */
    @SuppressWarnings("checkstyle:innerassignment")
    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    private Authenticator getAuthInstance() {
        Authenticator result = auth;
        if (result == null) {
            synchronized (Authenticator.class) {
                if (result == null) {
                    final AuthenticationWebSocketClient authChecker = (AuthenticationWebSocketClient) getConnectionManager()
                            .getBestConnection(AuthenticationWebSocketClient.class);
                    auth = result = new Authenticator(authChecker, new MongoOptionChecker());
                }
            }
        }
        return result;
    }
}
