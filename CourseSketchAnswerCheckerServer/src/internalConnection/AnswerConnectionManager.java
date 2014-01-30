package internalConnection;

import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

public class AnswerConnectionManager extends MultiConnectionManager {

	public AnswerConnectionManager(MultiInternalConnectionServer parent, boolean connectLocally) {
		super(parent);
		this.connectLocally = connectLocally;
	}

	public void connectServers(MultiInternalConnectionServer parent) {
		try {
			createAndAddConnection(parent, connectLocally, "srl02.tamu.edu", 8883, SolutionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
