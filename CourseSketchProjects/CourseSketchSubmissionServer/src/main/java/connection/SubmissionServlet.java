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
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public SubmissionServlet(final ServerInfo serverInfo) {
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
     * @param serverInformation {@link ServerInfo} Contains all of the information about the server.
     * @return {@link SubmissionConnectionManager}.
     */
    @Override
    public final MultiConnectionManager createConnectionManager(final ServerInfo serverInformation) {
        return new SubmissionConnectionManager(this.getServer(), serverInformation);
    }
}
