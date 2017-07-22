package coursesketch.serverfront;

import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManager;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.services.IdentityService;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class IdentityServiceInitializer extends ServerWebSocketInitializer {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityServiceInitializer.class);

    /**
     * Identity manager.
     */
    private final IdentityManager manager;

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public IdentityServiceInitializer(final ServerInfo serverInfo) {
        super(serverInfo);
        manager = new IdentityManager(this.getServerInfo());
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
        services.add(new IdentityService(new Authenticator(super.getRpcAuthChecker(), createAuthenticationChecker()), manager));
        return services;
    }

    /**
     * Sets the authentication websocket as an authenticator.
     */
    @Override
    protected void onReconnect() {
        try {
            manager.startDatabase();
        } catch (DatabaseAccessException e) {
            LOG.error("Error starting coursesketch.util.util", e);
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
     * @return {@link IdentityConnectionManager}.
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new IdentityConnectionManager(getServer(), serverInfo);
    }
}
