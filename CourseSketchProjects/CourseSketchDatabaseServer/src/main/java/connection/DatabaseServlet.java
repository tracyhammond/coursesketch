package connection;

import com.google.common.collect.Lists;
import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.database.auth.AuthenticationUpdater;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.auth.MongoOptionChecker;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.database.institution.mongo.MongoInstitution;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.identity.IdentityWebSocketClient;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import handlers.subhandlers.AnswerCheckerGradingHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * A database specific servlet that creates a new Database server and Database
 * Connection Managers.
 *
 * @author gigemjt
 *
 */
public class DatabaseServlet extends ServerWebSocketInitializer {

    /**
     * Constructor for DatabaseServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public DatabaseServlet(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new DatabaseServerWebSocketHandler(this);
    }

    /**
     * {@inheritDoc}
     * @return {@link DatabaseConnectionManager}.
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new DatabaseConnectionManager(getServer(), serverInfo);
    }

    @Override
    protected List<CourseSketchRpcService> getRpcServices() {
        return Lists.newArrayList(new AnswerCheckerGradingHandler());
    }

    @Override
    public boolean isSharingDatabaseReaders() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MongoInstitution}.
     */
    @Override public final AbstractCourseSketchDatabaseReader createSharedDatabaseReader(final ServerInfo info) {
        final AuthenticationWebSocketClient authChecker = getManager()
                .getBestConnection(AuthenticationWebSocketClient.class);
        final Authenticator auth = new Authenticator(authChecker, new MongoOptionChecker(info));

        final IdentityManagerInterface identityManagerInterface = getManager()
                .getBestConnection(IdentityWebSocketClient.class);
        return new MongoInstitution(info, auth, authChecker, identityManagerInterface);
    }
}
