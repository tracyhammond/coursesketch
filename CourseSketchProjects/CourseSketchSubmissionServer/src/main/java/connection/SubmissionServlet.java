package connection;

import coursesketch.server.base.ServerWebSocketHandler;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;

@SuppressWarnings("serial")
public class SubmissionServlet extends ServerWebSocketInitializer {

	public SubmissionServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final ServerWebSocketHandler createServerSocket() {
    	return new SubmissionServerWebSocketHandler(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	public final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new SubmissionConnectionManager(this.getServer(), connectLocally, secure);
	}
}
