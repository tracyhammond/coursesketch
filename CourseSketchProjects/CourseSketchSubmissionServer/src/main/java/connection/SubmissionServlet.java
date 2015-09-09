package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;

/**
 * The default servlet it creates a single websocket instance that is then used
 * on all messages.
 *
 * To create a custom management of the connections use this version
 *
 * @author gigemjt
 */
@SuppressWarnings("serial")
public class SubmissionServlet extends ServerWebSocketInitializer {

    /**
     * Creates a GeneralConnectionServlet.
     *
     * @param timeoutTime
     *         The time it takes before a connection times out.
     * @param isSecure
     *         True if the connection is allowing SSL connections.
     * @param connectLocally
     *         True if the server is connecting locally.
     */
    public SubmissionServlet(ServerInfo serverInfo) {
        super(serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketHandler createServerSocket() {
        return new SubmissionServerWebSocketHandler(this);
    }

    /**
     * {@inheritDoc}
     *
     * <br>
     * We do not need to manage multiple connections so we might as well just make it return null.
     * @param serverInformation
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInformation) {
        return new SubmissionConnectionManager(this.getServer(), , serverInformation.isLocal());
    }
}
