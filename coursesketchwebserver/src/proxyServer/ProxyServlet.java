package proxyServer;

import internalConnections.ProxyConnectionManager;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionManager;

@SuppressWarnings("serial")
public final class ProxyServlet extends GeneralConnectionServlet {

	public ProxyServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final GeneralConnectionServer createServerSocket() {
    	return new ProxyServer(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new ProxyConnectionManager(connectionServer, connectLocally, secure);
	}

	@Override
	public void reconnect() {
		super.reconnect();
		((ProxyServer) connectionServer).initializeListeners();
	}
}