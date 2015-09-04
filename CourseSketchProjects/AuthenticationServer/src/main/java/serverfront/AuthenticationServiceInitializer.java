package serverfront;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.CourseSketchRpcService;
import coursesketch.server.rpc.ServerWebSocketHandler;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import services.AuthenticationService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("serial")
public final class AuthenticationServiceInitializer extends ServerWebSocketInitializer {

    /**
     * @param timeoutTime
     *            The time it takes before a connection times out.
     * @param secure
     *            True if the connection is allowing SSL connections.
     * @param connectLocally
     *            True if the server is connecting locally.
     */
    public AuthenticationServiceInitializer(ServerInfo serverInfo) {
        super(serverInfo);
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
        services.add(new AuthenticationService(dataGrabber));
        return services;
    }
}
