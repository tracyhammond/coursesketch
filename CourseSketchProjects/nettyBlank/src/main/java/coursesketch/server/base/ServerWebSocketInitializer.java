package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.ServerInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gigemjt on 10/19/14.
 */
public class ServerWebSocketInitializer extends ChannelInitializer<SocketChannel> implements ISocketInitializer {

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
     * {@link ServerInfo} Contains all of the information about the server.
     */
    private final ServerInfo serverInfo;

    /**
     * The context needed for a SSL connection.
     */
    private SslContext sslContext;

    /**
     * The wrapper for the server socket.
     */
    private ServerSocketWrapper singleWrapper;

    /**
     * Creates a GeneralConnectionServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ServerWebSocketInitializer(final ServerInfo serverInfo) {
        // Netty does not natively support timeout times.
        LOG.info("Currently time out is ignored by a netty backing " + serverInfo.getTimeOut());
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
     * {@inheritDoc}
     */
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:hiddenfield" })
    @Override
    public MultiConnectionManager createConnectionManager(final ServerInfo serverInfo) {
        return new MultiConnectionManager(connectionServer, serverInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @return An instance of the {@link ServerWebSocketHandler}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public AbstractServerWebSocketHandler createServerSocket() {
        return new ServerWebSocketHandler(this, this.getServerInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override public final ServerInfo getServerInfo() {
        return this.serverInfo;
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
     * This method will be called once the {@link io.netty.channel.Channel} was registered. After the method returns this instance
     * will be removed from the {@link ChannelPipeline} of the {@link io.netty.channel.Channel}.
     *
     * @param channel
     *         the {@link io.netty.channel.Channel} which was registered.
     */
    @Override
    protected final void initChannel(final SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        if (sslContext != null) {
            pipeline.addFirst("ssl", sslContext.newHandler(channel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(MAX_SIZE));
        // TODO change this to the double locking check thingy
        if (singleWrapper == null) {
            singleWrapper = new ServerSocketWrapper(createServerSocket(), this.getServerInfo().isSecure());
        }
        pipeline.addLast(singleWrapper);
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
}
