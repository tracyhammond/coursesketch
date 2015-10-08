package coursesketch.serverfront;

import com.mongodb.MongoClient;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.database.auth.DbAuthChecker;
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
        List<CourseSketchRpcService> services = new ArrayList<CourseSketchRpcService>();
        services.add(new AuthenticationService(new DbAuthChecker(mongoClient.getDB(this.getServerInfo().getDatabaseName()))));
        return services;
    }
}
