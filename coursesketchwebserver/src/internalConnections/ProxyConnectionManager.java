package internalConnections;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;
import proxyServer.ProxyServer;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ProxyConnectionManager extends MultiConnectionManager{
	
	public ProxyConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}
	
	public void connectServers(ProxyServer serv) {
		//System.out.println("Open Recognition...");
		//createAndAddConnection(serv, true, 8887, RecognitionConnection.class);
		System.out.println("Open Login...");
		createAndAddConnection(serv, false, "goldberglinux.tamu.edu:", 8886, LoginConnection.class);
		System.out.println("Open Data...");
		createAndAddConnection(serv, false, "srl04.tamu.edu:", 8885, DataConnection.class);
		//System.out.println("Open Answer Checker Server...");
		//createAndAddConnection(serv, true, 8884, AnswerConnection.class);
	}

}