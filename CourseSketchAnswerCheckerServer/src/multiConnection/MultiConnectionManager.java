package multiConnection;


import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
	 * @param isLocal if the connection that is being created is local or remote
	 * @param port the port that this connection is created at.  (Has to be unique to this computer)
	 * @param connectionType the class that will be made (should be a subclass of WrapperConnection)
	 * @return a completed {@link WrapperConnection}
	 * @throws ConnectionException if a connection has failed to be made.
	 */
	public static WrapperConnection createConnection(MultiInternalConnectionServer serv, boolean isLocal, String remoteAdress, int port, Class<? extends WrapperConnection> connectionType) throws ConnectionException {
		WrapperConnection c = null;
		if (remoteAdress == null && !isLocal) {
			throw new ConnectionException("Attempting to connect to null address");
		}
		String location = isLocal ? "ws://localhost:" + port : "ws://" + remoteAdress +":"+ port;
		try {
			Constructor construct = connectionType.getConstructor(URI.class, Draft.class, MultiInternalConnectionServer.class);
			c = (WrapperConnection) construct.newInstance( new URI( location ), new Draft_10() , serv);
		} catch (Exception e) {
			e.printStackTrace();
		} // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		if (c != null) {
			c.connect();
		}
		if (c == null) {
			throw new ConnectionException("failed to create WrapperConnection");
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

	/**
	 * Creates and then adds a connection to the {@link MultiConnectionManager}.
	 *
	 * @see #createConnection(MultiInternalConnectionServer, boolean, String, int, Class)
	 * @see #addConnection(WrapperConnection, Class) 
	 */
	public void createAndAddConnection(MultiInternalConnectionServer serv, boolean isLocal, String remoteAdress, int port, Class<? extends WrapperConnection> connectionType) throws ConnectionException {
		WrapperConnection connection = createConnection(serv, isLocal, remoteAdress, port, connectionType);
		addConnection(connection, connectionType);
	}

	/**
	 * Adds a connection to a list with the given connectionType.
	 *
	 * @param connection the connection to be added
	 * @param connectionType the type to differientiate connections by
	 * @throws {@link NullPointerException} if connection is null or connectionType is null
	 */
	public void addConnection(WrapperConnection connection, Class<? extends WrapperConnection> connectionType) {
		if (connection == null) {
			throw new NullPointerException("can not add null connection");
		}

		if (connectionType == null) {
			throw new NullPointerException("can not add connection to null type");
		}

		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			cons = new ArrayList<WrapperConnection>();
			cons.add(connection);
			connections.put(connectionType, cons);
			System.out.println("creating a new connectionList for: " +connectionType.getName()+ " with list: " + connections.get(connectionType));
		} else {
			cons.add(connection);
		}
	}

	/**
	 * Returns a connection that we believe to be the best connection at this time.
	 *
	 * @param connectionType
	 * @return a valid connection.
	 */
	public WrapperConnection getBestConnection(Class<? extends WrapperConnection> connectionType){
		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			throw new NullPointerException("ConnectionType: "+ connectionType.getName() +" does not exist in this manager");
		}
		return cons.get(0); // lame best connection.
	}

	public void dropAllConnection() {
		synchronized(connections) {
			for(Class<? extends WrapperConnection> con:connections.keySet()) {
				
//				ArrayList<WrapperConnection>
			}
		}
	}
}