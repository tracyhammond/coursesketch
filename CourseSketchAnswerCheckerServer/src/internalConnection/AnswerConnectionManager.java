package internalConnection;

import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

public class AnswerConnectionManager extends MultiConnectionManager {

	private boolean connectLocally = CONNECT_LOCALLY;
	
	public AnswerConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}

	public void connectServers(MultiInternalConnectionServer parent) {
		try {
			createAndAddConnection(parent, connectLocally, "srl02.tamu.edu", 8883, SolutionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
