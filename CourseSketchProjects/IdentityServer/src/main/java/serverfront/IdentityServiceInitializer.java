package serverfront;

import connection.IdentityConnectionManager;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import services.IndentityThing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class IdentityServiceInitializer extends ServerWebSocketInitializer {

    /**
     * @param timeoutTime
     *            The time it takes before a connection times out.
     * @param secure
     *            True if the connection is allowing SSL connections.
     * @param connectLocally
     *            True if the server is connecting locally.
     */
    public IdentityServiceInitializer(ServerInfo serverInfo) {
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
     * @param secure
     *            True if the connection is using SSL.
     * @param serverInformation
     * @return {@link IdentityConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInformation) {
        return new IdentityConnectionManager(getServer(), , serverInformation.isLocal());
    }

    @Override
    protected List<CourseSketchRpcService> getRpcServices() {
        List<CourseSketchRpcService> services = new ArrayList<CourseSketchRpcService>();
        services.add(new IndentityThing());
        return services;
    }
}
