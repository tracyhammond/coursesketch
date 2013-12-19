package internalConnections;

import protobuf.srl.request.Message.Request;
import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ProxyConnectionManager extends MultiConnectionManager {

	private boolean connectLocally = CONNECT_LOCALLY;

	public ProxyConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}

	public void connectServers(MultiInternalConnectionServer serv) {
		//System.out.println("Open Recognition...");
		//createAndAddConnection(serv, connectLocally, "srl03.tamu.edu", 8887, RecognitionConnection.class);
		System.out.println("Open Login...");
		try {
			createAndAddConnection(serv, connectLocally, "srl02.tamu.edu", 8886, LoginConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Open Data...");
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, DataConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Open Answer Checker Server...");
		//createAndAddConnection(serv, true, 8884, AnswerConnection.class);
	}

	public static final Request createClientRequest(Request r) {
		Request.Builder build = Request.newBuilder(r);
		build.clearServersideId();
		build.clearSessionInfo();
		return build.build();
	}

	public void send(Request req, String sessionId, Class<? extends WrapperConnection> connectionType, String userId) {
		Request.Builder builder = Request.newBuilder(req);
		builder.setServersideId(userId);
		super.send(builder.build(), sessionId, connectionType);
	}
}