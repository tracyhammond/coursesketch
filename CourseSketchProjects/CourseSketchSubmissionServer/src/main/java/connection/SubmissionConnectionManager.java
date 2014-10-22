package connection;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionManager;
import utilities.ConnectionException;

public class SubmissionConnectionManager extends MultiConnectionManager {

	public SubmissionConnectionManager(AbstractServerWebSocketHandler parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(AbstractServerWebSocketHandler serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, secure, DataClientConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
