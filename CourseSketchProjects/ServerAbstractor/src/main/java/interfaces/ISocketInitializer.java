package interfaces;

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
     * @see IMultiConnectionManager#connectServers(interfaces.IServerWebSocket)
     */
    void reconnect();

    /**
     * @return The current number of current connections.
     */
    int getCurrentConnectionNumber();

    /**
     * Override this method to create a subclass of the MultiConnectionManager.
     *
     * @param connectLocally True if the connection is acting as if it is on a local computer (used for testing)
     * @param iSecure True if the connection is using SSL.
     * @return An instance of the {@link IMultiConnectionManager}
     */
    IMultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean iSecure);

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link IServerWebSocket}
     */
    IServerWebSocket createServerSocket();

    // METHODS BELOW NEED TO BE IN ALL CLASSES OF THIS INTERFACE (but they can't be in interface because of scope.

    /**
     * Called after reconnecting the connections.
     */
    //protected void onReconnect();

    /**
     * @return the multiConnectionManager.  This is only used within this package.
     */
    /* package-private */ //final IMultiConnectionManager getManager();

    /**
     * @return the GeneralConnectionServer.
     */
    //protected final IServerWebSocket getServer();
}
