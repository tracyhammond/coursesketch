package coursesketch.serverfront;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
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
 * Sets up the services and creates the default socket.
 */
@SuppressWarnings("serial")
public final class AuthenticationServiceInitializer extends ServerWebSocketInitializer {

    /**
     * A client that connects to the mongo database.
     */
    private final MongoClient mongoClient;

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    AuthenticationServiceInitializer(final ServerInfo serverInfo) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CourseSketchRpcService> getRpcServices() {
        final List<CourseSketchRpcService> services = new ArrayList<>();
        final MongoDatabase mongoClientDB = mongoClient.getDatabase(this.getServerInfo().getDatabaseName());
        services.add(new AuthenticationService(new DbAuthChecker(mongoClientDB), new DbAuthManager(mongoClientDB)));
        return services;
    }
}
