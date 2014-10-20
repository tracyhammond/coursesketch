package connection;

import interfaces.IMultiConnectionManager;
import coursesketch.jetty.multiconnection.ServerWebSocket;
import coursesketch.jetty.multiconnection.GeneralConnectionServlet;

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
	protected final IMultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new SubmissionConnectionManager(connectionServer, connectLocally, secure);
	}
}
