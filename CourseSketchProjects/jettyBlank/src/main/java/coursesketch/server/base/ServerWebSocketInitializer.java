package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The default servlet it creates a single websocket instance that is then used
 * on all messages.
 *
 * To create a custom management of the connections use this version
 *
 * @author gigemjt
 */
@WebServlet(name = "Course Sketch WebSocket Servlet", urlPatterns = { "/websocket" })
public class ServerWebSocketInitializer extends WebSocketServlet implements ISocketInitializer {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketInitializer.class);

    /**
     * The server that the servlet is connected to.
     */
    private final AbstractServerWebSocketHandler connectionServer;

    /**
     * The {@link MultiConnectionManager} that is used by the servlet to recieve
     * connections.
     */
    private final MultiConnectionManager manager;

    /**
     * {@link ServerInfo} Contains all of the information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * Creates a GeneralConnectionServlet.
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ServerWebSocketInitializer(final ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        LOG.info("Creating a new connectionServer");
        connectionServer = createServerSocket();
        LOG.info("Creating a new connectionManager");
        manager = createConnectionManager(getServerInfo());
    }

    /**
     * If you want a different policy then you can override the configure
     * method.
     * @param factory The factory that is being configured.
     */
    @Override
    public final void configure(final WebSocketServletFactory factory) {
        LOG.info("Configuring servlet");
        factory.getPolicy().setMaxBinaryMessageBufferSize(AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE);
        factory.getPolicy().setMaxBinaryMessageSize(AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE);
        if (serverInfo.getTimeOut() > 0) {
            LOG.info("Adding a timeout to the socket: {}", serverInfo.getTimeOut());
            factory.getPolicy().setIdleTimeout(serverInfo.getTimeOut());
        }
        factory.setCreator(new SocketCreator());
    }

    /**
     * {@inheritDoc}
     * Turns off tracing.
     */
    @Override
    public final void doTrace(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException {
        throw new ServletException("Trace is not supported by this server");
    }
    /**
     * A custom web socket creator that checks to make sure that the.
     *
     * @author gigemjt
     */
    private class SocketCreator implements WebSocketCreator {

        /**
         * Creates the new websocket. If the socket needs to be secure and it is
         * not secure then the socket creation fails and Null is returned.
         *
         * @param req The servlet upgrade request.
         * @param resp The servlet upgrade response.
         * @return the {@link ServerWebSocketHandler} that handles the websocket communication.
         */
        @Override
        public final Object createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
            LOG.info("Recieved Upgrade request");
            if (serverInfo.isSecure() && !req.isSecure()) {
                LOG.info("Refusing an insecure connection");
                return null;
            }
            LOG.info("Returning a websocket with name {}", connectionServer.getName());
            return connectionServer;
        }

    }

    /**
     * Stops the socket, and the server and drops all connections.
     */
    @Override
    public final void stop() {
        LOG.info("Stopping socket");
        connectionServer.stop();
        if (manager != null) {
            manager.dropAllConnection(true, false);
        }
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link ServerWebSocketHandler}
     */
    @SuppressWarnings("checkstyle:designforextension")
    public AbstractServerWebSocketHandler createServerSocket() {
        return new ServerWebSocketHandler(this, this.getServerInfo());
    }

    /**
     * @return {@link ServerInfo} contains all of the data about the server.
     */
    @Override public final ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Override this method to create a subclass of the MultiConnectionManager.
     *
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     * @return An instance of the {@link MultiConnectionManager}.
     */
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:hiddenfield" })
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new MultiConnectionManager(connectionServer, serverInfo);
    }

    /**
     * This is called when the reconnect command is executed.
     *
     * By default this drops all connections and then calls
     *
     * @see coursesketch.server.interfaces.MultiConnectionManager#connectServers(coursesketch.server.interfaces.AbstractServerWebSocketHandler)
     */
    @Override
    public final void reconnect() {
        LOG.info("Reconnecting");
        if (manager != null) {
            manager.dropAllConnection(true, false);
            manager.connectServers(connectionServer);
        }
        onReconnect();
    }

    /**
     * @return The current number of current connections.
     */
    @Override
    public final int getCurrentConnectionNumber() {
        return connectionServer.getCurrentConnectionNumber();
    }

    /**
     * Called after reconnecting the connections.
     */
    protected void onReconnect() { }

    /**
     * @return the multiConnectionManager.  This is only used within this package.
     */
    /* package-private */ final MultiConnectionManager getManager() {
        return manager;
    }

    /**
     * @return the GeneralConnectionServer.
     */
    protected final AbstractServerWebSocketHandler getServer() {
        return connectionServer;
    }
}
