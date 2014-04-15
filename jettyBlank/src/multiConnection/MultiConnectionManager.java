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

	protected boolean connectLocally = CONNECT_LOCALLY;
	public static final boolean CONNECT_LOCALLY = true;
	public static final boolean CONNECT_REMOTE = false;
	HashMap<Class<?>, ArrayList<WrapperConnection>> connections
		= new HashMap<Class<?>, ArrayList<WrapperConnection>> ();

	protected MultiInternalConnectionServer parent;
	public MultiConnectionManager(MultiInternalConnectionServer parent) {
		this.parent = parent;
	}

	/**
	 * Creates a connection given the different information.
	 *
	 * @param serv the server that is connected to this connection manager
	 * @param man this is the manager that will then hold the connection
	 * @param isLocal if the connection that is being created is local or remote
	 * @param port the port that this connection is created at.  (Has to be unique to this computer)
	 * @param connectLocally the class that will be made (should be a subclass of WrapperConnection)
	 * @return a completed {@link WrapperConnection}
	 * @throws ConnectionException if a connection has failed to be made.
	 */
	public static WrapperConnection createConnection(MultiInternalConnectionServer serv, boolean isLocal, String remoteAdress, int port, Class<? extends WrapperConnection> connectionType) throws ConnectionException {
		WrapperConnection c = null;
		if (serv == null) {
			throw new ConnectionException("Can't create connection with a null parent server");
		}
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
		// In case of error do this!
		//c.setParent(serv);
		if (c == null) {
			throw new ConnectionException("failed to create WrapperConnection");
		}
		return c;
	}

	/**
	 * Sends a request with the id and the connection at the given index.
	 * @param req The request to send.
	 * @param sessionID The session Id of the request.
	 * @param connectionNumber the location of where to find the location.
	 */
	public void send(Request req, String sessionID, Class<? extends WrapperConnection> connectionType) {
		Request packagedRequest = MultiInternalConnectionServer.Encoder.requestIDBuilder(req, sessionID);		//Attach the existing request with the UserID
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
	 * Does nothing by default.  Can be overwritten to make life easier.
	 * @param parent
	 */
	public void connectServers(MultiInternalConnectionServer parent) {}
	
	/**
	 * Adds a connection to a list with the given connectLocally.
	 *
	 * @param connection the connection to be added
	 * @param connectLocally the type to differentiate connections by
	 * @throws {@link NullPointerException} if connection is null or connectLocally is null
	 */
	public void addConnection(WrapperConnection connection, Class<? extends WrapperConnection> connectionType) {
		if (connection == null) {
			throw new NullPointerException("can not add null connection");
		}

		if (connectionType == null) {
			throw new NullPointerException("can not add connection to null type");
		}

		connection.parentManager = this;
		
		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			cons = new ArrayList<WrapperConnection>();
			cons.add(connection);
			connections.put(connectionType, cons);
			System.out.println("creating a new connectionList for: " + connectionType + " with list: " + connections.get(connectionType));
		} else {
			cons.add(connection);
		}
	}

	/**
	 * Returns a connection that we believe to be the best connection at this time.
	 *
	 * @param connectLocally
	 * @return a valid connection.
	 */
	public WrapperConnection getBestConnection(Class<? extends WrapperConnection> connectionType){
		System.out.println("getting Connection from type: " + connectionType);
		ArrayList<WrapperConnection> cons = connections.get(connectionType);
		if (cons == null) {
			throw new NullPointerException("ConnectionType: "+ connectionType.getName() +" does not exist in this manager");
		}
		return cons.get(0); // lame best connection.
	}

	/**
	 * Closes all connections and removes them from storage.
	 *
	 * @param clearTypes if true then the mapping will be completely cleared.
	 */
	public void dropAllConnection(boolean clearTypes, boolean debugPrint) {
		synchronized(connections) {
			//<?  extends WrapperConnection> // for safe keeping
			for(Class<?> conKey:connections.keySet()) {
				for(WrapperConnection connection: connections.get(conKey)) {
					if (debugPrint) {
						System.out.println(connection.getURI());
					}
					connection.close();
				}
				connections.get(conKey).clear();
			}
			if (clearTypes)
				connections.clear();
		}
	}
}