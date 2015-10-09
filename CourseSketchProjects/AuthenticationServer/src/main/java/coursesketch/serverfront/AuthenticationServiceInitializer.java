package coursesketch.serverfront;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import coursesketch.database.auth.DbAuthChecker;
import coursesketch.database.auth.DbAuthManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.services.AuthenticationService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class AuthenticationServiceInitializer extends ServerWebSocketInitializer {

    private MongoClient mongoClient;

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public AuthenticationServiceInitializer(final ServerInfo serverInfo) {
        super(serverInfo);
        mongoClient = new MongoClient(serverInfo.getDatabaseUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new DefaultWebSocketHandler(this, this.getServerInfo());
    }

    @Override
    protected List<CourseSketchRpcService> getRpcServices() {
        final List<CourseSketchRpcService> services = new ArrayList<CourseSketchRpcService>();
        final DB db = mongoClient.getDB(this.getServerInfo().getDatabaseName());
        services.add(new AuthenticationService(new DbAuthChecker(db), new DbAuthManager(db)));
        return services;
    }
}
