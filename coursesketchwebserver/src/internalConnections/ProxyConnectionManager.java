package internalConnections;

import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.MultiConnectionManager;
import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ProxyConnectionManager extends MultiConnectionManager {

	public ProxyConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		super(parent, connectType, secure);
	}

	@Override
	public void connectServers(GeneralConnectionServer serv) {
		//System.out.println("Open Recognition...");
		System.out.println("Open Login...");
		System.out.println(connectLocally);
		System.out.println(secure);
		try {
			createAndAddConnection(serv, connectLocally, "srl02.tamu.edu", 8886, secure, LoginConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Open Data...");
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8885, secure, DataConnection.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			createAndAddConnection(serv, connectLocally, "srl04.tamu.edu", 8884, secure, AnswerConnection.class);
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

	public void send(Request req, String sessionId, Class<? extends ConnectionWrapper> connectionType, String userId) {
		Request.Builder builder = Request.newBuilder(req);
		builder.clearServersideId();
		builder.setServersideId(userId);
		super.send(builder.build(), sessionId, connectionType);
	}
}