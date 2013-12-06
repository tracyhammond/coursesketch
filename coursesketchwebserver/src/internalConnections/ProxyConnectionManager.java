package internalConnections;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ProxyConnectionManager extends MultiConnectionManager {
	
	public ProxyConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}
	
	public void connectServers(MultiInternalConnectionServer serv) {
		//System.out.println("Open Recognition...");
		//createAndAddConnection(serv, true, 8887, RecognitionConnection.class);
		System.out.println("Open Login...");
		try {
			createAndAddConnection(serv, false, "srl02.tamu.edu", 8886, LoginConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		System.out.println("Open Data...");
		try {
			createAndAddConnection(serv, false, "srl04.tamu.edu", 8885, DataConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//System.out.println("Open Answer Checker Server...");
		//createAndAddConnection(serv, true, 8884, AnswerConnection.class);
	}

}