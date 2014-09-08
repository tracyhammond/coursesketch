package connection;

import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionManager;
import internalConnection.AnswerConnectionManager;

@SuppressWarnings("serial")
public class AnswerCheckerServlet extends GeneralConnectionServlet {

	public AnswerCheckerServlet(long timeoutTime, boolean secure, boolean connectLocally) {
		super(timeoutTime, secure, connectLocally);
	}

	@Override
	public final GeneralConnectionServer createServerSocket() {
    	return new AnswerCheckerServer(this);
    }

	/**
	 * We do not need to manage multiple connections so we might as well just make it return null
	 */
	@Override
	protected final MultiConnectionManager createConnectionManager(boolean connectLocally, boolean secure) {
		return new AnswerConnectionManager(connectionServer, connectLocally, secure);
	}
}
