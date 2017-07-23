package coursesketch.connection;

import coursesketch.database.RecognitionDatabaseClient;
import coursesketch.recognition.BasicRecognition;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.services.RecognitionService;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the recognition service.
 */
@SuppressWarnings("serial")
public final class RecognitionServiceInitializer extends ServerWebSocketInitializer {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionServiceInitializer.class);

    /**
     * Identity manager.
     */
    private final RecognitionDatabaseClient databaseClient;

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public RecognitionServiceInitializer(final ServerInfo serverInfo) {
        super(serverInfo);
        databaseClient = new RecognitionDatabaseClient(serverInfo);
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
        final List<CourseSketchRpcService> services = new ArrayList<CourseSketchRpcService>();
        try {
            databaseClient.startDatabase();
        } catch (DatabaseAccessException e) {
            LOG.error("Error starting database", e);
        }
        services.add(new RecognitionService(new BasicRecognition(databaseClient)));
        return services;
    }

    /**
     * Sets the authentication websocket as an authenticator.
     */
    @Override
    protected void onReconnect() {
        try {
            databaseClient.startDatabase();
        } catch (DatabaseAccessException e) {
            LOG.error("Error starting database", e);
        }
        // Does nothing by default
    }

    /**
     * {@inheritDoc}
     * @return {@link null}.
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return null;
    }
}
