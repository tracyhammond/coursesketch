package internalConnections;

import java.net.URI;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public class RecognitionConnection extends ConnectionWrapper {

	public RecognitionConnection(URI destination, GeneralConnectionServer parent) {
		super(destination, parent);
	}

}