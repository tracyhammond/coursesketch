package internalConnections;

import java.net.URI;

import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class RecognitionConnection extends ConnectionWrapper {

	public RecognitionConnection(URI destination, GeneralConnectionServer parent) {
		super(destination, parent);
	}

}