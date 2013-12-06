package multiConnection;


import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;

import protobuf.srl.request.Message.Request;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class MultiConnectionManager {

	private boolean connectionType = CONNECT_LOCALLY;
	public static final boolean CONNECT_LOCALLY = true;
	public static final boolean CONNECT_REMOTE = false;
	HashMap<Class<? extends WrapperConnection>, ArrayList<WrapperConnection>> connections
		= new HashMap<Class<? extends WrapperConnection>, ArrayList<WrapperConnection>> ();

	MultiInternalConnectionServer parent;
	public MultiConnectionManager(MultiInternalConnectionServer parent) {
		this.parent = parent;
	}

	/**
	 * Creates a connection given the different information.
	 *
	 * @param serv the server that is connected to this connection manager
	 * @param local if the connection that is being created is local or remote
	 * @param port the port that this connection is created at.  (Has to be unique to this computer)
	 * @param connectionType the class that will be made (should be a subclass of WrapperConnection)
	 * @return
	 */
	public static WrapperConnection createConnection(MultiInternalConnectionServer serv, boolean local, String remoteAdress, int port, Class<? extends WrapperConnection> connectionType) {
		WrapperConnection c=null;
		if (remoteAdress == null) {
			remoteAdress = "goldberglinux02.tamu.edu";
		}
		String location = local ? "ws://localhost:" + port : "ws://" + remoteAdress + port;
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

	/**
	 * Sends a request with the id and the connection at the given index.
	 * @param req The request to send.
	 * @param userID The session Id of the request.
	 * @param connectionNumber the location of where to find the location.
	 */
	public void send(Request req, String userID, Class<? extends WrapperConnection> connectionType) {
		Request packagedRequest = MultiInternalConnectionServer.Encoder.requestIDBuilder(req, userID);		//Attach the existing request with the UserID
		getBestConnection(connectionType).send(packagedRequest.toByteArray());
	}

	public void createAndAddConnection(MultiInternalConnectionServer serv, boolean local, String remoteAdress, int port, Class<? extends WrapperConnection> connectionType) {
		addConnection(createConnection(serv, local, remoteAdress, port, connectionType), connectionType);
	}

	public void addConnection(WrapperConnection connection, Class<? extends WrapperConnection> connectionType) {
		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			cons = new ArrayList<WrapperConnection>();
			cons.add(connection);
			connections.put(connectionType, cons);
		} else {
			cons.add(connection);
		}
	}

	public WrapperConnection getBestConnection(Class<? extends WrapperConnection> connectionType){
		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			throw new NullPointerException("ConnectionType: "+ connectionType.getName() +" does not exist in this manager");
		}
		return cons.get(0); // lame best connection.
	}

}