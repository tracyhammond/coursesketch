package coursesketch.serverfront;

import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.identity.IdentityWebSocketClient;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ConnectionException;
import utilities.LoggingConstants;

/**
 * Created by dtracers on 12/6/2015.
 */
public class LoginConnectionManager extends MultiConnectionManager {

    /**
     *  Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoginConnectionManager.class);

    /**
     * A constructor for the multi connection manager.
     * @param server The parent server
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public LoginConnectionManager(final AbstractServerWebSocketHandler server, final ServerInfo serverInfo) {
        super(server, serverInfo);
    }

    /**
     * Called to connect this server to other servers.
     * @param serv The current server that the connections will be made from.
     */
    @Override
    public final void connectServers(final AbstractServerWebSocketHandler serv) {
        try {
            createAndAddConnection(serv, this.isConnectionLocal(), IdentityWebSocketClient.ADDRESS, IdentityWebSocketClient.PORT,
                    this.isSecure(), IdentityWebSocketClient.class);
        } catch (ConnectionException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
