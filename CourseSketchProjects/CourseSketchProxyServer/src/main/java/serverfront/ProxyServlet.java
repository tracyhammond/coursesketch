package serverfront;

import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;
import internalconnections.ProxyConnectionManager;

@SuppressWarnings("serial")
public final class ProxyServlet extends GeneralConnectionServlet {

	public ProxyServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final GeneralConnectionServer createServerSocket() {
    	return new ProxyServer(this);
    }

	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new ProxyConnectionManager(getServer(), connectLocally, secure);
	}

	@Override
	protected final void onReconnect() {
		((ProxyServer) getServer()).initializeListeners();
	}
}
