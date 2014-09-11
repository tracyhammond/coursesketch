package connection;

import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;

@SuppressWarnings("serial")
public final class LoginServlet extends GeneralConnectionServlet {

	public LoginServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final GeneralConnectionServer createServerSocket() {
    	return new LoginServer(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return null;
	}
}
