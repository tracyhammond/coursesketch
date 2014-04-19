package jettyMultiConnection;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import protobuf.srl.request.Message.Request;

public class MultiConnectionManager {

	protected boolean connectLocally = CONNECT_LOCALLY;
	protected boolean secure = false;
	public static final boolean CONNECT_LOCALLY = true;
	public static final boolean CONNECT_REMOTE = false;
	HashMap<Class<?>, ArrayList<ConnectionWrapper>> connections
		= new HashMap<Class<?>, ArrayList<ConnectionWrapper>> ();
	
	protected GeneralConnectionServer parent; // TODO: CHANGE THIS

	public MultiConnectionManager(GeneralConnectionServer parent, boolean connectType, boolean secure) {
		this.parent = parent;
		this.connectLocally = connectType;
		this.secure = secure;
	}

	/**
	 * Creates a connection given the different information.
	 *
	 * @param serv the server that is connected to this connection manager
	 * @param man this is the manager that will then hold the connection
	 * @param isLocal if the connection that is being created is local or remote
	 * @param port the port that this connection is created at.  (Has to be unique to this computer)
	 * @param connectLocally the class that will be made (should be a subclass of ConnectionWrapper)
	 * @return a completed {@link ConnectionWrapper}
	 * @throws ConnectionException if a connection has failed to be made.
	 */
	public static ConnectionWrapper createConnection(GeneralConnectionServer serv, boolean isLocal, String remoteAdress, int port,
			boolean isSecure, Class<? extends ConnectionWrapper> connectionType) throws ConnectionException {
		ConnectionWrapper c = null;
		if (serv == null) {
			throw new ConnectionException("Can't create connection with a null parent server");
		}
		if (remoteAdress == null && !isLocal) {
			throw new ConnectionException("Attempting to connect to null address");
		}
		
		String start = isSecure ? "wss://" : "ws://";
		
		String location = start + (isLocal ? "localhost:" + port : "" + remoteAdress +":"+ port);
		System.out.println("Creating a client connecting to: " + location);
		try {
			@SuppressWarnings("rawtypes")
			Constructor construct = connectionType.getConstructor(URI.class, GeneralConnectionServer.class);
			c = (ConnectionWrapper) construct.newInstance( new URI( location ), serv);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (c != null) {
			try {
				c.connect();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		// In case of error do this!
		//c.setParent(serv);
		if (c == null) {
			throw new ConnectionException("failed to create ConnectionWrapper");
		}
		return c;
	}


	/**
	 * Sends a request with the id and the connection at the given index.
	 * @param req The request to send.
	 * @param sessionID The session Id of the request.
	 * @param connectionNumber the location of where to find the location.
	 * @throws ConnectionException 
	 */
	public void send(Request req, String sessionID, Class<? extends ConnectionWrapper> connectionType) throws ConnectionException {
		Request packagedRequest = GeneralConnectionServer.Encoder.requestIDBuilder(req, sessionID);		//Attach the existing request with the UserID
		try {
			getBestConnection(connectionType).send(packagedRequest.toByteArray());
		} catch(NullPointerException e) {
			System.out.println("Failed to get a local connection");
			throw new ConnectionException(e.getLocalizedMessage());
		}
	}

	/**
	 * Creates and then adds a connection to the {@link MultiConnectionManager}.
	 *
	 * @see #createConnection(GeneralConnectionServer, boolean, String, int, Class)
	 * @see #addConnection(ConnectionWrapper, Class) 
	 */
	public void createAndAddConnection(GeneralConnectionServer serv, boolean isLocal, String remoteAdress, int port, boolean isSecure, Class<? extends ConnectionWrapper> connectionType) throws ConnectionException {
		ConnectionWrapper connection = createConnection(serv, isLocal, remoteAdress, port, isSecure, connectionType);
		addConnection(connection, connectionType);
	}

	/**
	 * Drops all of the connections then adds them all back
	 */
	protected void reconnect() {
		this.dropAllConnection(true, false);
		this.connectServers(parent);
	}

	/**
	 * Does nothing by default.  Can be overwritten to make life easier.
	 * @param parent
	 */
	@SuppressWarnings("unused")
	public void connectServers(GeneralConnectionServer parent) {}
	
	/**
	 * Adds a connection to a list with the given connectLocally.
	 *
	 * @param connection the connection to be added
	 * @param connectLocally the type to differentiate connections by
	 * @throws {@link NullPointerException} if connection is null or connectLocally is null
	 */
	public void addConnection(ConnectionWrapper connection, Class<? extends ConnectionWrapper> connectionType) {
		if (connection == null) {
			throw new NullPointerException("can not add null connection");
		}

		if (connectionType == null) {
			throw new NullPointerException("can not add connection to null type");
		}

		connection.parentManager = this;
		
		ArrayList<ConnectionWrapper> cons = connections.get(connectionType);
		if (cons == null) {
			cons = new ArrayList<ConnectionWrapper>();
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
	public ConnectionWrapper getBestConnection(Class<? extends ConnectionWrapper> connectionType) {
		ArrayList<ConnectionWrapper> cons = connections.get(connectionType);
		if (cons == null) {
			throw new NullPointerException("ConnectionType: "+ connectionType.getName() +" does not exist in this manager");
		}
		System.out.println("getting Connection: " + connectionType.getSimpleName());
		return cons.get(0); // lame best connection.
	}

	/**
	 * Closes all connections and removes them from storage.
	 *
	 * @param clearTypes if true then the mapping will be completely cleared.
	 */
	public void dropAllConnection(boolean clearTypes, boolean debugPrint) {
		synchronized(connections) {
			//<?  extends ConnectionWrapper> // for safe keeping
			for(Class<?> conKey:connections.keySet()) {
				for(ConnectionWrapper connection: connections.get(conKey)) {
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
