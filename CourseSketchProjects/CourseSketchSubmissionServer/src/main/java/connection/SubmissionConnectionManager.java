package connection;

import interfaces.IServerWebSocket;
import multiconnection.MultiConnectionManager;

public class SubmissionConnectionManager extends MultiConnectionManager {

	public SubmissionConnectionManager(IServerWebSocket parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(IServerWebSocket serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, secure, DataConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
