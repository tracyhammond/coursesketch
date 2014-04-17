package connection;

import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionManager;

@SuppressWarnings("serial")
public class DatabaseServlet extends GeneralConnectionServlet {

	public DatabaseServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final GeneralConnectionServer createServerSocket() {
    	return new DatabaseServer(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new DatabaseConnectionManager(connectionServer, connectLocally, secure);
	}
}
