package connection;

import coursesketch.jetty.multiconnection.ServerWebSocketHandler;
import coursesketch.jetty.multiconnection.ServerWebSocketInitializer;

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
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new SubmissionConnectionManager(connectionServer, connectLocally, secure);
	}
}
