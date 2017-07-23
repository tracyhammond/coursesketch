package connection;

import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.SubmissionDatabaseClient;
import handlers.SubmissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.utils.Util;
import services.SubmissionService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class SubmissionServiceInitializer extends ServerWebSocketInitializer {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmissionServiceInitializer.class);

    /**
     * Submission manager.
     */
    private final SubmissionManagerInterface manager;

    /**
     * Identity manager.
     */
    private final SubmissionDatabaseClient databaseClient;

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public SubmissionServiceInitializer(final ServerInfo serverInfo) {
        super(serverInfo);
        databaseClient = new SubmissionDatabaseClient(serverInfo);
        manager = new SubmissionManager(databaseClient);
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
        services.add(new SubmissionService(new Authenticator(super.getRpcAuthChecker(), createAuthenticationChecker()), manager));
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
     * Creates an empty authentication checker that only throws exceptions when the methods are called.
     * @return Creates an instance of the {@link AuthenticationOptionChecker} that throws an exception when any of its methods are called.
     */
    private AuthenticationOptionChecker createAuthenticationChecker() {
        return new AuthenticationOptionChecker() {

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime)
                    throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }

            /**
             * {@inheritDoc}
             *
             * This instance throws an exception.
             */
            @Override public AuthenticationDataCreator createDataGrabber(final Util.ItemType collectionType, final String itemId)
                    throws DatabaseAccessException {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return {@link SubmissionConnectionManager}.
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new SubmissionConnectionManager(getServer(), serverInfo);
    }
}
