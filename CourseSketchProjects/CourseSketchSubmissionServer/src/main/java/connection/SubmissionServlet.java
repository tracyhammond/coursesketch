package connection;

import multiconnection.ServerWebSocket;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;

@SuppressWarnings("serial")
public class SubmissionServlet extends GeneralConnectionServlet {

	public SubmissionServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final ServerWebSocket createServerSocket() {
    	return new SubmissionServerWebSocket(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new SubmissionConnectionManager(connectionServer, connectLocally, secure);
	}
}
