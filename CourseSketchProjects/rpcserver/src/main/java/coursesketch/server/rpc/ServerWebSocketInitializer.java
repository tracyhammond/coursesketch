package coursesketch.server.rpc;

import com.google.protobuf.Service;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
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
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketInitializer implements ISocketInitializer {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServerWebSocketInitializer.class);

    /**
     * Max size used in aggregating http request.  which is 2^16.
     */
    private static final int MAX_SIZE = 65536;
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
     * The wrapper for the server socket.
     */
    private ServerSocketWrapper singleWrapper;
    private final ServerInfo serverInfo;

    /**
     * Creates a GeneralConnectionServlet.
     *
     * @param iTimeoutTime
     *         The time it takes before a connection times out.
     * @param iSecure
     *         True if the connection is allowing SSL connections.
     * @param connectLocally
     *         True if the server is connecting locally.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ServerWebSocketInitializer(ServerInfo serverInfo) {
        LOG.info("Currently time out time is not used " + serverInfo.getTimeOut());
        this.serverInfo = serverInfo;
        connectionServer = createServerSocket();
        manager = createConnectionManager(getServerInfo());
    }

    /**
     * Stops the socket, and the server and drops all connections.
     */
    @Override
    public void stop() {

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
     * Override this method to create a subclass of the MultiConnectionManager.
     *
     *
     * @param serverInfo@return An instance of the {@link coursesketch.server.interfaces.MultiConnectionManager}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new MultiConnectionManager(connectionServer, serverInfo.isLocal(), serverInfo.isSecure());
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link coursesketch.server.interfaces.AbstractServerWebSocketHandler}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public AbstractServerWebSocketHandler createServerSocket() {
        return new ServerWebSocketHandler(this, getServerInfo());
    }

    /**
     * @return {@link ServerInfo} contains all of the data about the server.
     */
    @Override public ServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * Sets the context for ssl.
     *
     * @param iSslContext
     *         The Ssl context
     */
    final void setSslContext(final SslContext iSslContext) {
        this.sslContext = iSslContext;
    }

    /**
     * Called after reconnecting the connections.
     */
    protected void onReconnect() {
    }

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

    public final void initChannel(final DuplexTcpServerPipelineFactory serverFactory) {
        List<Service> services = getRpcServices();
        if (services == null) {
            throw new NullPointerException("getRpcServices can not return null");
        }
        ServerSocketWrapper wrapper = new ServerSocketWrapper(createServerSocket(), getServerInfo().isSecure());
        services.add(wrapper);
        for (Service service: services) {
            serverFactory.getRpcServiceRegistry().registerService(service);
        }
        serverFactory.registerConnectionEventListener(wrapper);
    }

    protected List<Service> getRpcServices() {
        return new ArrayList<Service>();
    }
}
