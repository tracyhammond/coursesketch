package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;

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
	 * @param timeoutTime The time it takes before a connection times out.
	 * @param isSecure True if the connection is allowing SSL connections.
	 * @param connectLocally True if the server is connecting locally.
	 */
	public SubmissionServlet(final long timeoutTime, final boolean isSecure, final boolean connectLocally) {
		super(timeoutTime, isSecure, connectLocally);
	}

	@Override
	public final ServerWebSocketHandler createServerSocket() {
    	return new SubmissionServerWebSocketHandler(this);
    }

	/**
	 * {@inheritDoc}
	 *
	 * <br>
	 * We do not need to manage multiple connections so we might as well just make it return null.
	 */
	@Override
	public final MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean secure) {
		return new SubmissionConnectionManager(this.getServer(), connectLocally, secure);
	}
}
