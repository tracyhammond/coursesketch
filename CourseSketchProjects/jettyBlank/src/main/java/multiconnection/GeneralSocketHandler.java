package multiconnection;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by gigemjt on 10/18/14.
 */
public class GeneralSocketHandler extends WebSocketHandler {

    /**
     * The server that the servlet is connected to.
     */
    private final Class<? extends GeneralConnectionServer> connectionServer;

    /**
     * The {@link MultiConnectionManager} that is used by the servlet to recieve
     * connections.
     */
    private final MultiConnectionManager manager;

    /**
     * The amount of time it takes before a connection times out.
     */
    private final long timeoutTime;

    /**
     * True if the server is allowing secure connections.
     */
    private final boolean secure;

    /**
     * Creates a GeneralConnectionServlet.
     * @param iTimeoutTime The time it takes before a connection times out.
     * @param iSecure True if the connection is allowing SSL connections.
     * @param connectLocally True if the server is connecting locally.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public GeneralSocketHandler(final long iTimeoutTime, final boolean iSecure, final boolean connectLocally) {
        this.timeoutTime = iTimeoutTime;
        this.secure = iSecure;
        System.out.println("Creating a new connectionServer");
        connectionServer = createServerSocket();
        System.out.println("Creating a new connectionManager");
        manager = createConnectionManager(connectLocally, secure);
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link GeneralConnectionServer}
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Class<? extends GeneralConnectionServer> createServerSocket() {
        return GeneralConnectionServer.class;
    }

    /**
     * Stops the socket, and the server and drops all connections.
     */
    public final void stop2() {
        System.out.println("Stopping socket");
        //connectionServer.stop();
        if (manager != null) {
            manager.dropAllConnection(true, false);
        }
    }

    @Override
    public final void configure(final WebSocketServletFactory factory) {
        factory.register(connectionServer);
    }

    /**
     * Override this method to create a subclass of the MultiConnectionManager.
     *
     * @param connectLocally True if the connection is acting as if it is on a local computer (used for testing)
     * @param iSecure True if the connection is using SSL.
     * @return An instance of the {@link MultiConnectionManager}
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean iSecure) {
        return null; //new MultiConnectionManager(connectionServer, connectLocally, iSecure);
    }
}
