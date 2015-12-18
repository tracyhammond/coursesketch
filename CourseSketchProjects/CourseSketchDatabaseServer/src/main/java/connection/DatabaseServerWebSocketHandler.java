package connection;

import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.database.auth.AuthenticationUpdater;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.auth.MongoOptionChecker;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.services.submission.SubmissionWebSocketClient;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.submission.SubmissionManager;
import handlers.DataInsertHandler;
import handlers.DataRequestHandler;
import handlers.DataUpdateHandler;
import handlers.SubmissionHandler;
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
        final Institution instance = (Institution) super.getDatabaseReader();
        if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
            DataRequestHandler.handleRequest(req, conn, instance, super.getConnectionToId().get(conn).getSessionId(), getConnectionManager());
        } else if (req.getRequestType() == Request.MessageType.DATA_INSERT) {
            DataInsertHandler.handleData(req, conn, instance);
        } else if (req.getRequestType() == Request.MessageType.DATA_UPDATE) {
            DataUpdateHandler.handleData(req, conn, instance);
        } else if (req.getRequestType() == Request.MessageType.SUBMISSION) {
            SubmissionHandler.handleData(req, conn, getConnectionManager().getBestConnection(SubmissionWebSocketClient.class), instance);
        } else if (req.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(req);
            if (rsp != null) {
                send(conn, rsp);
            }
        }
        LOG.debug("Finished looking at query {}", req);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MongoInstitution}.
     */
    @Override protected final AbstractCourseSketchDatabaseReader createDatabaseReader(final ServerInfo info) {
        final AuthenticationWebSocketClient authChecker = (AuthenticationWebSocketClient) getConnectionManager()
                .getBestConnection(AuthenticationWebSocketClient.class);
        final Authenticator auth = new Authenticator(authChecker, new MongoOptionChecker(info));
        final AuthenticationUpdater authUpdater = authChecker;
        return new MongoInstitution(info, auth, authUpdater);
    }
}
