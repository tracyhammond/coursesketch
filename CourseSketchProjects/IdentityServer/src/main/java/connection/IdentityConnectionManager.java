package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
public final class IdentityConnectionManager extends MultiConnectionManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdentityConnectionManager.class);

    /**
     * Creates a manager for the proxy connections.
     *  @param parent
     *            {@link serverfront.DefaultWebSocketHandler}
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public IdentityConnectionManager(final AbstractServerWebSocketHandler parent, final ServerInfo serverInfo) {
        super(parent, serverInfo);
    }

    /**
     * connects to other servers.
     *
     * @param serv
     *            an instance of the local server (
     *            {@link serverfront.DefaultWebSocketHandler}) in this case.
     */
    @Override
    public void connectServers(final AbstractServerWebSocketHandler serv) {
        // System.out.println("Open Recognition...");
        LOG.info("Open Our Connections!");
        LOG.info("Is Connection Local? {}", isConnectionLocal());
        LOG.info("Is Secure? {}", isSecure());
    }
}
