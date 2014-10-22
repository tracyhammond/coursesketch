package coursesketch.server.interfaces;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utilities.ConnectionException;
import protobuf.srl.request.Message.Request;

/**
 * A manager for holding all of the connections that were created.
 *
 * @author gigemjt
 */
public class MultiConnectionManager {

    /**
     * Determines whether the server is being connected locally.
     * <p/>
     * Can be overridden.
     */
    private final boolean connectLocally;

    /**
     * Determines whether the server will be connecting securely.
     * <p/>
     * Can be overridden.
     */
    private final boolean secure;

    /**
     * A map that contains a list of connections that are differentiated by a
     * specific class.
     */
    private final Map<Class<?>, ArrayList<AbstractClientWebSocket>> connections = new HashMap<Class<?>, ArrayList<AbstractClientWebSocket>>();

    /**
     * The server that using this {@link MultiConnectionManager}.
     */
    private final AbstractServerWebSocketHandler parent;

    /**
     * Creates a default {@link MultiConnectionManager}.
     *
     * @param iParent  The server that is using this object.
     * @param iIsLocal True if the connection should be for a local server instead of
     *                 a remote server.
     * @param iSecure  True if the connections should be secure.
     */
    public MultiConnectionManager(final AbstractServerWebSocketHandler iParent, final boolean iIsLocal, final boolean iSecure) {
        this.parent = iParent;
        this.connectLocally = iIsLocal;
        this.secure = iSecure;
    }

    /**
     * Creates a connection given the different information.
     *
     * @param serv           The server that is connected to this connection manager.
     * @param isLocal        If the connection that is being created is local or remote.
     * @param remoteAdress   The location to connect to if it is connecting remotely.
     * @param port           The port that this connection is created at. (Has to be unique
     *                       to this computer)
     * @param isSecure       True if using SSL false otherwise.
     * @param connectionType The class that will be made (should be a subclass of
     *                       ConnectionWrapper)
     * @return a completed {@link AbstractClientWebSocket}.
     * @throws ConnectionException If a connection has failed to be made.
     */
    public static AbstractClientWebSocket createConnection(final AbstractServerWebSocketHandler serv, final boolean isLocal,
            final String remoteAdress,
            final int port, final boolean isSecure, final Class<? extends AbstractClientWebSocket> connectionType) throws ConnectionException {
        if (serv == null) {
            throw new ConnectionException("Can't create connection with a null parent server");
        }
        if (remoteAdress == null && !isLocal) {
            throw new ConnectionException("Attempting to connect to null address");
        }

        final String start = isSecure ? "wss://" : "ws://";

        final String location = start + (isLocal ? "localhost:" + port : "" + remoteAdress + ":" + port);
        System.out.println("Creating a client connecting to: " + location);
        return initializeConnection(location, connectionType, serv);
    }

    /**
     * Initializes the connection given certain parameters.
     *
     * @param location       The location of the server as a URI
     * @param connectionType a class that represents the connection.
     * @param serv           The server that is managing the connection.
     * @return A connection wrapper.
     * @throws ConnectionException Thrown if there are problems initializing the connection.
     */
    private static AbstractClientWebSocket initializeConnection(final String location,
            final Class<? extends AbstractClientWebSocket> connectionType,
            final AbstractServerWebSocketHandler serv) throws ConnectionException {
        AbstractClientWebSocket conWrapper = null;
        @SuppressWarnings("rawtypes")
        Constructor construct;
        try {
            construct = connectionType.getConstructor(URI.class, AbstractServerWebSocketHandler.class);
            conWrapper = (AbstractClientWebSocket) construct.newInstance(new URI(location), serv);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ConnectionException("Failed to get constructor for connection wrapper: " + connectionType.getSimpleName(), e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ConnectionException("Failed to invoke constructor for the connection wrapper: " + connectionType.getSimpleName(), e);
        } catch (URISyntaxException e) {
            throw new ConnectionException("The URI for localtion: [" + location + "] is not valid syntax", e);
        }

        if (conWrapper != null) {
            conWrapper.connect();
        }

        return conWrapper;
    }

    /**
     * Sends a request with the id and the connection at the given index.
     *
     * @param req            The request to send.
     * @param sessionID      The session Id of the request.
     * @param connectionType The type of connection being given
     * @throws ConnectionException thrown if a connection failed to be found.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void send(final Request req, final String sessionID, final Class<? extends AbstractClientWebSocket> connectionType)
            throws ConnectionException {
        // Attach the existing request with the UserID
        final Request packagedRequest = AbstractServerWebSocketHandler.Encoder.requestIDBuilder(req, sessionID);
        final AbstractClientWebSocket connection = getBestConnection(connectionType);
        if (connection == null) {
            System.out.println("Failed to get a local connection");
            throw new ConnectionException("failed to get a connection of type " + connectionType.getSimpleName());
        }
        connection.send(ByteBuffer.wrap(packagedRequest.toByteArray()));
    }

    /**
     * Creates and then adds a connection to the {@link MultiConnectionManager}.
     *
     * @param serv           The server that is connected to this connection manager.
     * @param isLocal        If the connection that is being created is local or remote.
     * @param remoteAdress   The location to connect to if it is connecting remotely.
     * @param port           The port that this connection is created at. (Has to be unique
     *                       to this computer)
     * @param isSecure       True if using SSL false otherwise.
     * @param connectionType The class that will be made (should be a subclass of
     *                       ConnectionWrapper)
     * @throws ConnectionException If a connection has failed to be made.
     * @see #createConnection(AbstractServerWebSocketHandler, boolean, String, int,
     * Class)
     * @see #addConnection(AbstractClientWebSocket, Class)
     */
    public final void createAndAddConnection(final AbstractServerWebSocketHandler serv, final boolean isLocal,
            final String remoteAdress, final int port, final boolean isSecure,
            final Class<? extends AbstractClientWebSocket> connectionType) throws ConnectionException {
        final AbstractClientWebSocket connection = createConnection(serv, isLocal, remoteAdress, port, isSecure, connectionType);
        addConnection(connection, connectionType);
    }

    /**
     * Allows a server to set an action to occur when a socket is no longer able
     * to send messages.
     *
     * @param listen         the source object will be a list of request and will also
     *                       contain a string specifying the type of connection.
     * @param connectionType The type to bind the action to.
     */
    public final void setFailedSocketListener(final ActionListener listen, final Class<? extends AbstractClientWebSocket> connectionType) {
        final ArrayList<AbstractClientWebSocket> cons = connections.get(connectionType);
        if (cons == null) {
            throw new IllegalStateException("ConnectionType: " + connectionType.getName() + " does not exist in this manager");
        }
        for (AbstractClientWebSocket con : cons) {
            con.setFailedSocketListener(listen);
        }
    }

    /**
     * Drops all of the connections then adds them all back.
     */
    protected final void reconnect() {
        this.dropAllConnection(true, false);
        this.connectServers(parent);
    }

    /**
     * Does nothing by default. Can be overwritten to make life easier.
     *
     * @param parentServer ignored by this implementation. Override to change
     *                     functionality.
     */
    public void connectServers(final AbstractServerWebSocketHandler parentServer) {
    }

    /**
     * Adds a connection to a list with the given connection Type.
     * <p/>
     * Throws a {@link NullPointerException} If connection is null or
     * connectLocally is null.
     *
     * @param connection     The connection to be added.
     * @param connectionType The type to differentiate connections by.
     */
    public final void addConnection(final AbstractClientWebSocket connection, final Class<? extends AbstractClientWebSocket> connectionType) {
        if (connection == null) {
            throw new IllegalArgumentException("can not add null connection");
        }

        if (connectionType == null) {
            throw new IllegalArgumentException("can not add connection to null type");
        }

        connection.setParentManager(this);

        ArrayList<AbstractClientWebSocket> cons = connections.get(connectionType);
        if (cons == null) {
            cons = new ArrayList<AbstractClientWebSocket>();
            cons.add(connection);
            connections.put(connectionType, cons);
            System.out.println("creating a new connectionList for: " + connectionType + " with list: " + connections.get(connectionType));
        } else {
            cons.add(connection);
        }
    }

    /**
     * Returns a connection that we believe to be the best connection at this
     * time. This can be overridden for a better server specific system.
     *
     * @param connectionType The type of connection being requested.
     * @return A valid connection.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public AbstractClientWebSocket getBestConnection(final Class<? extends AbstractClientWebSocket> connectionType) {
        final ArrayList<AbstractClientWebSocket> cons = connections.get(connectionType);
        if (cons == null) {
            throw new IllegalStateException("ConnectionType: " + connectionType.getName() + " does not exist in this manager");
        }
        System.out.println("getting Connection: " + connectionType.getSimpleName());
        return cons.get(0); // lame best connection.
    }

    /**
     * Closes all connections and removes them from storage.
     *
     * @param clearTypes if true then the mapping will be completely cleared.
     * @param debugPrint If true then the uri is printed as the connection is closed.
     */
    public final void dropAllConnection(final boolean clearTypes, final boolean debugPrint) {
        synchronized (connections) {
            // <? extends ConnectionWrapper> // for safe keeping
            for (Class<?> conKey : connections.keySet()) {
                for (AbstractClientWebSocket connection : connections.get(conKey)) {
                    if (debugPrint) {
                        System.out.println(connection.getURI());
                    }
                    connection.close();
                }
                connections.get(conKey).clear();
            }
            if (clearTypes) {
                connections.clear();
            }
        }
    }

    /**
     * @return The parent server for this MultiConnectionManager.
     */
    protected final AbstractServerWebSocketHandler getParentServer() {
        return parent;
    }

    /**
     * @return True if the connection is local (used for testing).
     */
    protected final boolean isConnectionLocal() {
        return connectLocally;
    }

    /**
     * @return True if the connection is using SSL.
     */
    protected final boolean isSecure() {
        return secure;
    }
}
