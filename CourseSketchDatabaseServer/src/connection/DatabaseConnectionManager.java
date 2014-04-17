package connection;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.MultiConnectionManager;
import protobuf.srl.request.Message.Request;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class DatabaseConnectionManager extends MultiConnectionManager {

	public DatabaseConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void send(final Request req,final  String sessionID,final Class<? extends ConnectionWrapper> connectionType) {
		System.out.println("TRYING TO RESEND THE THREAD! THROUGH A SEPCIAL METHOD!");
		try {
			super.send(req, sessionID, connectionType);
		} catch (java.nio.channels.NotYetConnectedException e) {
			reconnect();
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

	@Override
	public void connectServers(GeneralConnectionServer serv) {
		try {
			createAndAddConnection(serv, connectLocally, "srl02.tamu.edu", 8883, secure, SubmissionConnection.class);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
}