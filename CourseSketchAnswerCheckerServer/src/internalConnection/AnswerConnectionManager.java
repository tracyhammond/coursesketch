package internalConnection;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.MultiConnectionManager;

public class AnswerConnectionManager extends MultiConnectionManager {

	public AnswerConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer parent) {
		try {
			createAndAddConnection(parent, connectLocally, "srl02.tamu.edu", 8883, secure, SolutionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
