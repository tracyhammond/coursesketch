package connection;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DatabaseConnectionManager extends MultiConnectionManager{
	
	public DatabaseConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}
	
	public void connectServers(MultiInternalConnectionServer serv) {
		System.out.println("Open Database...");
		createAndAddConnection(serv, false, null, 8887, SolutionConnection.class);
	}

}