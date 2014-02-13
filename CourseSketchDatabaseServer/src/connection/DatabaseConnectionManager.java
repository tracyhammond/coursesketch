package connection;

import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DatabaseConnectionManager extends MultiConnectionManager {
	
	public DatabaseConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}

	public void connectServers(MultiInternalConnectionServer serv) {
		System.out.println("Open Database...");
		try {
			createAndAddConnection(serv, false, "Srl02.tamu.edu", 8887, SubmissionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}