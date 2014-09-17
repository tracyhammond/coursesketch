package connection;

import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;

public class SubmissionConnectionManager extends MultiConnectionManager {

	public SubmissionConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, secure, DataConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
