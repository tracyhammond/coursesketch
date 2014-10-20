package connection;

import interfaces.IServerWebSocketHandler;
import interfaces.MultiConnectionManager;
import utilities.ConnectionException;

public class SubmissionConnectionManager extends MultiConnectionManager {

	public SubmissionConnectionManager(IServerWebSocketHandler parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(IServerWebSocketHandler serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, secure, DataClientConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
