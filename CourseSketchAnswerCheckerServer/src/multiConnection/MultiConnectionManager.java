package multiConnection;


import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;

import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class MultiConnectionManager {

	private boolean connectionType = CONNECT_LOCALLY;
	public static final boolean CONNECT_LOCALLY = true;
	public static final boolean CONNECT_REMOTE = false;
	ArrayList<WrapperConnection> connections = new ArrayList<WrapperConnection>();

	MultiInternalConnectionServer parent;
	public MultiConnectionManager(MultiInternalConnectionServer parent) {
		this.parent = parent;
	}

	public static WrapperConnection createConnection(MultiInternalConnectionServer serv, boolean local, int port, Class<? extends WrapperConnection> connectionType) {
		WrapperConnection c=null;
		String location = local ? "ws://localhost:" + port : "ws://goldberglinux02.tamu.edu:" + port;
		try {
			Constructor construct = connectionType.getConstructor(URI.class, Draft.class, MultiInternalConnectionServer.class);
			c = (WrapperConnection) construct.newInstance( new URI( location ), new Draft_10() , serv);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		return c;
	}

	public void send(Request req, String userID, int connectionNumber){
		Request packagedRequest = MultiInternalConnectionServer.Encoder.requestIDBuilder(req, userID);		//Attach the existing request with the UserID
		getConnection(connectionNumber).send(packagedRequest.toByteArray());
	}

	public void addConnection(WrapperConnection connection) {
		connections.add(connection);
	}

	public WrapperConnection getConnection(int i){
		return connections.get(i);
	}

}