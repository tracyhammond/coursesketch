package internalConnections;

import java.net.URI;

import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;
import org.java_websocket.drafts.Draft;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DataConnection extends WrapperConnection {
	
	public DataConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent);
	}
}