package coursesketch.serverfront;

import com.google.common.collect.Lists;
import coursesketch.database.auth.Authenticator;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.services.IdentityService;

import java.util.List;

import static coursesketch.utilities.AuthUtilities.createThrowingAuthenticationOptionChecker;

/**
 *
 */
@SuppressWarnings("serial")
public final class IdentityServiceInitializer extends ServerWebSocketInitializer {

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    IdentityServiceInitializer(final ServerInfo serverInfo) {
        super(serverInfo);
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
        return Lists.newArrayList(new IdentityService(
                new Authenticator(super.getRpcAuthChecker(), createThrowingAuthenticationOptionChecker())));
    }

    /**
     * Sets the authentication websocket as an authenticator.
     */
    @Override
    protected void onReconnect() {
        // Does nothing by default
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
