package internalConnections;

import java.net.URI;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public class RecognitionConnection extends ConnectionWrapper {

	public RecognitionConnection(URI destination, GeneralConnectionServer parent) {
		super(destination, parent);
	}

}