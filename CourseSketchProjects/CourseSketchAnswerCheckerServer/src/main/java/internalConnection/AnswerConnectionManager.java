package internalConnection;

import connection.ConnectionException;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;

public class AnswerConnectionManager extends MultiConnectionManager {

	public AnswerConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer parent) {
		try {
			createAndAddConnection(parent, connectLocally, "srl02.tamu.edu", 8883, secure, SubmissionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}
