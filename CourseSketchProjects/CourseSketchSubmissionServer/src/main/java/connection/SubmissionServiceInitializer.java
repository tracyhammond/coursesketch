package connection;

import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.utils.Util;
import services.SubmissionService;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.utilities.AuthUtilities.createThrowingAuthenticationOptionChecker;

/**
 * Initializes all services.
 */
@SuppressWarnings("serial")
public final class SubmissionServiceInitializer extends ServerWebSocketInitializer {

    /**
     * Constructor for AuthenticationServiceInitializer.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    SubmissionServiceInitializer(final ServerInfo serverInfo) {
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
        final List<CourseSketchRpcService> services = new ArrayList<>();
        services.add(new SubmissionService(new Authenticator(super.getRpcAuthChecker(), createThrowingAuthenticationOptionChecker())));
        return services;
    }

    /**
     * Sets the authentication websocket as an authenticator.
     */
    @Override
    protected void onReconnect() {
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
