package coursesketch.server.interfaces;

import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.database.interfaces.DatabaseReaderHolder;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by gigemjt on 10/19/14.
 */
public interface ISocketInitializer {

    /**
     * Stops the socket, and the server and drops all connections.
     */
    void stop();

    /**
     * This is called when the reconnect command is executed.
     *
     * By default this drops all connections and then calls
     *
     * @see MultiConnectionManager#connectServers(AbstractServerWebSocketHandler)
     */
    void reconnect();

    /**
     * @return The current number of current connections.
     */
    int getCurrentConnectionNumber();

    /**
     * Creates and returns a manager that can be used to connect to other servers.
     *
     * This server will act as a client when connecting to those other server.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     * @return An instance of the {@link MultiConnectionManager}
     */
    MultiConnectionManager createConnectionManager(final ServerInfo serverInfo);

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link AbstractServerWebSocketHandler}
     */
    AbstractServerWebSocketHandler createServerSocket();

    /**
     * @return {@link ServerInfo}. Contains all of the data about the server.
     */
    ServerInfo getServerInfo();

    /**
     * Called to initialize The {@link AbstractServerWebSocketHandler}.
     */
    void onServerStart();

    default void loadSharedDatabase(ServerInfo info, List<DatabaseReaderHolder> databaseHolders) {
        if (isSharingDatabaseReaders()) {
            AbstractCourseSketchDatabaseReader shared = createSharedDatabaseReader(info);
            try {
                shared.startDatabase();
            } catch (DatabaseAccessException e) {
                LoggerFactory.getLogger(ISocketInitializer.class)
                        .error("An error was created starting the database for the server", e);
            }
            for (DatabaseReaderHolder databaseHolder : databaseHolders) {
                databaseHolder.setDatabaseReader(shared);
                databaseHolder.onInitializeDatabases();
            }
        } else {
            for (DatabaseReaderHolder databaseHolder : databaseHolders) {
                databaseHolder.initializeDatabase(info);
            }
        }
    }

    boolean isSharingDatabaseReaders();

    AbstractCourseSketchDatabaseReader createSharedDatabaseReader(ServerInfo serverInfo);

    // METHODS BELOW NEED TO BE IN ALL CLASSES OF THIS INTERFACE (but they can't be in interface because of scope.

    /**
     * Creates a GeneralConnectionServlet.
     * @param iTimeoutTime The time it takes before a connection times out.
     * @param iSecure True if the connection is allowing SSL connections.
     * @param connectLocally True if the server is connecting locally.
     */
    //public ISocketInitializer(final long iTimeoutTime, final boolean iSecure, final boolean connectLocally);

    /**
     * Called after reconnecting the connections.
     */
    //protected void onReconnect();

    /**
     * @return the multiConnectionManager.  This is only used within this package.
     */
    /* package-private */ //final MultiConnectionManager getManager();

    /**
     * @return the GeneralConnectionServer.
     */
    //protected final IServerWebSocket getServer();
}
