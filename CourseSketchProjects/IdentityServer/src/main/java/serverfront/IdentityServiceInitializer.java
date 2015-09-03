package serverfront;

import com.google.protobuf.Service;
import connection.IdentityConnectionManager;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
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
    public IdentityServiceInitializer(final long timeoutTime, final boolean secure, final boolean connectLocally) {
        super(timeoutTime, secure, connectLocally);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerWebSocketHandler createServerSocket() {
        return new DefaultWebSocketHandler(this);
    }

    /**
     * @param secure
     *            True if the connection is using SSL.
     * @param serverInfo
     * @return {@link IdentityConnectionManager}
     */
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new IdentityConnectionManager(getServer(), connectLocally, secure);
    }

    @Override
    protected List<Service> getRpcServices() {
        List<Service> services = new ArrayList<Service>();
        services.add(new IndentityThing());
        return services;
    }
}
