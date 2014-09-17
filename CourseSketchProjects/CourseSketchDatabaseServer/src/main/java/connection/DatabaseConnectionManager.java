package connection;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;
import protobuf.srl.request.Message.Request;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DatabaseConnectionManager extends MultiConnectionManager {

	public DatabaseConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl02.tamu.edu", 8883, secure, SubmissionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}