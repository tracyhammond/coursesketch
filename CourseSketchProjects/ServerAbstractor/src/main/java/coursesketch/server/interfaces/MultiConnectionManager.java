package coursesketch.server.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.Request;
import utilities.ConnectionException;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A manager for holding all of the connections that were created.
 *
 * @author gigemjt
 */
public class MultiConnectionManager {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MultiConnectionManager.class);

    /**
     * The path that routes the server to the WebSocket instead of a different possible connection.
     */
    public static final String SOCKET_PATH = "/websocket/";

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
     * @param parent  The server that is using this object.
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    public MultiConnectionManager(final AbstractServerWebSocketHandler parent, final ServerInfo serverInfo) {
        this.parent = parent;
        this.connectLocally = serverInfo.isLocal();
        this.secure = serverInfo.isSecure();
    }

    /**
     * Creates a connection given the different information.
     *
     * @param server         The server that is connected to this connection manager.
     * @param isLocal        If the connection that is being created is local or remote.
     * @param remoteAddress  The location to connect to if it is connecting remotely.
     * @param port           The port that this connection is created at. (Has to be unique
     *                       to this computer)
     * @param isSecure       True if using SSL false otherwise.
     * @param connectionType The class that will be made (should be a subclass of
     *                       ConnectionWrapper)
     * @return a completed {@link AbstractClientWebSocket}.
     * @throws ConnectionException If a connection has failed to be made.
     */
    public static AbstractClientWebSocket createConnection(final AbstractServerWebSocketHandler server, final boolean isLocal,
            final String remoteAddress, final int port, final boolean isSecure,
            final Class<? extends AbstractClientWebSocket> connectionType) throws ConnectionException {
        if (server == null) {
            throw new ConnectionException("Can't create connection with a null parent server");
        }
        if (remoteAddress == null && !isLocal) {
            throw new ConnectionException("Attempting to connect to null address");
        }

        final String start = isSecure ? "wss://" : "ws://";

        final String location = start + (isLocal ? "localhost:" + port : "" + remoteAddress + ":" + port) + SOCKET_PATH;
        LOG.info("Creating a client connecting to: {}", location);
        return initializeConnection(location, connectionType, server);
    }

    /**
     * Initializes the connection given certain parameters.
     *
     * @param location       The location of the server as a URI
     * @param connectionType A class that represents the connection.
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
            LOG.info("Failed to get a local connection");
            throw new ConnectionException("failed to get a connection of type " + connectionType.getSimpleName());
        }
        connection.send(ByteBuffer.wrap(packagedRequest.toByteArray()));
    }

    /**
     * Creates and then adds a connection to the {@link MultiConnectionManager}.
     *
     * @param serv           The server that is connected to this connection manager.
     * @param isLocal        If the connection that is being created is local or remote.
     * @param remoteAddress   The location to connect to if it is connecting remotely.
     * @param port           The port that this connection is created at. (Has to be unique
     *                       to this computer)
     * @param isSecure       True if using SSL false otherwise.
     * @param connectionType The class that will be made (should be a subclass of
     *                       ConnectionWrapper)
     * @throws ConnectionException If a connection has failed to be made.
     * @see #createConnection(AbstractServerWebSocketHandler, boolean, String, int, boolean, Class)
     * @see #addConnection(AbstractClientWebSocket, Class)
     */
    public final void createAndAddConnection(final AbstractServerWebSocketHandler serv, final boolean isLocal,
            final String remoteAddress, final int port, final boolean isSecure,
            final Class<? extends AbstractClientWebSocket> connectionType) throws ConnectionException {
        final AbstractClientWebSocket connection = createConnection(serv, isLocal, remoteAddress, port, isSecure, connectionType);
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
    public final void reconnect() {
        this.dropAllConnection(true, false);
        this.connectServers(parent);
    }

    /**
     * Does nothing by default.
     *
     * Can be overwritten to handle events when the servers are being connected.
     *
     * @param parentServer ignored by this implementation. Override to change
     *                     functionality.
     */
    public void connectServers(final AbstractServerWebSocketHandler parentServer) {
        // Overwritten by specific implementations.
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
            LOG.info("creating a new connectionList for: {} with list: {}", connectionType, connections.get(connectionType));
        } else {
            cons.add(connection);
        }
    }

    /**
     * Returns a connection that we believe to be the best connection at this
     * time. This can be overridden for a better server specific system.
     *
     * @param connectionType The type of connection being requested.
     * @param <T> A subclass of the {@link AbstractClientWebSocket}.
     * @return A valid connection.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public <T extends AbstractClientWebSocket> T getBestConnection(final Class<T> connectionType) {
        final ArrayList<? extends AbstractClientWebSocket> cons = connections.get(connectionType);
        if (cons == null) {
            throw new IllegalStateException("ConnectionType: " + connectionType.getName() + " does not exist in this manager");
        }
        LOG.info("getting Connection: {}", connectionType.getSimpleName());
        return (T) cons.get(0); // lame best connection.
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
                        LOG.info("Connection URI: {}", connection.getURI());
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
