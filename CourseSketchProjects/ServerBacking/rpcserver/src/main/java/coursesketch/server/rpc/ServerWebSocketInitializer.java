package coursesketch.server.rpc;

import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import coursesketch.auth.AuthenticationWebSocketClient;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the websocket for RPC use.
 *
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketInitializer implements ISocketInitializer {

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
     * The context needed for a SSL connection.
     */
    private SslContext sslContext;

    /**
     * {@link ServerInfo} Contains all of the information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * When the authentication is created the websocket might not be ready so this is used to save the instance.
     */
    private final DelayedAuthenticationChecker authenticationChecker;
    private List<CourseSketchRpcService> services;

    /**
     * Creates a GeneralConnectionServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ServerWebSocketInitializer(final ServerInfo serverInfo) {
        LOG.info("Currently time out time is not used: " + serverInfo.getTimeOut());
        this.serverInfo = serverInfo;
        connectionServer = createServerSocket();
        manager = createConnectionManager(getServerInfo());
        authenticationChecker = new DelayedAuthenticationChecker();
    }

    /**
     * Stops the socket, and the server and drops all connections.
     */
    @Override
    public void stop() {
        // does nothing by default
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
        if (connectionServer != null) {
            connectionServer.initialize();
        }
        for (CourseSketchRpcService service : services) {
            service.initialize(serverInfo);
        }

        final AuthenticationWebSocketClient authSocket = ((ServerWebSocketHandler) getServer()).getAuthenticationWebsocket();
        if (authSocket != null) {
            authenticationChecker.setRealAuthenticationChecker(authSocket);
        } else {
            LOG.warn("Authentication Websocket does not exist for this rpc instance.");
        }
        onReconnect();
    }

    /**
     * Returns The number of current connections.
     *
     * @return The number of current connections.
     */
    @Override
    public final int getCurrentConnectionNumber() {
        return connectionServer.getCurrentConnectionNumber();
    }

    /**
     * {@inheritDoc}
     * @return An instance of the {@link coursesketch.server.interfaces.MultiConnectionManager}.
     */
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:hiddenfield" })
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new MultiConnectionManager(connectionServer, serverInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @return An instance of the {@link ServerWebSocketHandler}.
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public AbstractServerWebSocketHandler createServerSocket() {
        return new ServerWebSocketHandler(this, getServerInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override public final ServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * Sets the context for ssl.
     *
     * @param sslContext
     *         The SSL context
     */
    final void setSslContext(final SslContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * Called after reconnecting the connections.
     */
    protected void onReconnect() {
        // does nothing by default.
    }

    /**
     * Called to initialize The {@link AbstractServerWebSocketHandler}.
     */
    @Override public void onServerStart() {
        // Does nothing by default
    }

    /**
     * Returns the {@link MultiConnectionManager}.
     *
     * This is only used within this package.
     *
     * @return the {@link MultiConnectionManager}.
     */
    /* package-private */ final MultiConnectionManager getManager() {
        return manager;
    }

    /**
     * Returns The {@link ServerWebSocketHandler}.
     *
     * @return The {@link ServerWebSocketHandler}.
     */
    protected final AbstractServerWebSocketHandler getServer() {
        return connectionServer;
    }

    /**
     * Initializes the channel with the server factory adding the services to the factory.
     *
     * @param serverFactory The server that the services are being added to.
     */
    final void initChannel(final DuplexTcpServerPipelineFactory serverFactory) {
        LOG.debug("SslContext {}", sslContext);
        services = getRpcServices();
        if (services == null) {
            throw new IllegalStateException("getRpcServices can not return null");
        }
        final ServerSocketWrapper wrapper = new ServerSocketWrapper(createServerSocket(), getServerInfo().isSecure());
        services.add(wrapper);
        for (CourseSketchRpcService service: services) {
            serverFactory.getRpcServiceRegistry().registerService(service);
        }
        serverFactory.registerConnectionEventListener(wrapper);
    }

    /**
     * Returns The list of rpc services that are run by the server.
     *
     * This should never return null.
     *
     * @return The list of rpc services that are run by the server.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected List<CourseSketchRpcService> getRpcServices() {
        return new ArrayList<>();
    }

    /**
     * @return an auth checker that looks for the authentication data over an rpc socket.
     */
    protected final AuthenticationChecker getRpcAuthChecker() {
        return authenticationChecker;
    }
}
