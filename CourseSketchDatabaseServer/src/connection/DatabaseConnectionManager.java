package connection;

import protobuf.srl.request.Message.Request;
import multiConnection.ConnectionException;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DatabaseConnectionManager extends MultiConnectionManager {
	
	public DatabaseConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}

	@Override
	public void send(final Request req, final String sessionID, final Class<? extends WrapperConnection> connectionType) {
		System.out.println("TRYING TO RESEND THE THREAD! THROUGH A SEPCIAL METHOD!");
		try {
			super.send(req, sessionID, connectionType);
		} catch (java.nio.channels.NotYetConnectedException e) {
			parent.reconnect();
			new Thread() {
				@Override
				public void run() {
					System.out.println("TRYING TO RESEND THE THREAD!");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("ATTEMPTING TO SEND AGAIN!");
					send(req, sessionID, connectionType);
				}
			}.start();
		}
	}
}