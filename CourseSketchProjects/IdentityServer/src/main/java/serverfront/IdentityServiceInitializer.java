package serverfront;

import connection.IdentityConnectionManager;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import services.IdentityThing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class IdentityServiceInitializer extends ServerWebSocketInitializer {

    /**
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public IdentityServiceInitializer(final ServerInfo serverInfo) {
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
     * @param serverInformation {@link ServerInfo} Contains all of the information about the server.
     * @return {@link IdentityConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInformation) {
        return new IdentityConnectionManager(getServer(), serverInformation);
    }

    /**
     * @return The list of rpc services that are run by the server.
     */
    @Override
    protected List<CourseSketchRpcService> getRpcServices() {
        final List<CourseSketchRpcService> services = new ArrayList<CourseSketchRpcService>();
        services.add(new IdentityThing());
        return services;
    }
}
