package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.MultiConnectionManager;
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
     * True if the server is allowing secure connections.
     */
    private final boolean secure;

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
     * @param iTimeoutTime
     *         The time it takes before a connection times out.
     * @param iSecure
     *         True if the connection is allowing SSL connections.
     * @param connectLocally
     *         True if the server is connecting locally.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ServerWebSocketInitializer(final long iTimeoutTime, final boolean iSecure, final boolean connectLocally) {
        LOG.info("Currently time out time is not used " + iTimeoutTime);
        this.secure = iSecure;
        connectionServer = createServerSocket();
        manager = createConnectionManager(connectLocally, secure);
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
     * @param connectLocally
     *         True if the connection is acting as if it is on a local computer (used for testing)
     * @param iSecure
     *         True if the connection is using SSL.
     * @return An instance of the {@link coursesketch.server.interfaces.MultiConnectionManager}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public MultiConnectionManager createConnectionManager(final boolean connectLocally, final boolean iSecure) {
        return new MultiConnectionManager(connectionServer, connectLocally, iSecure);
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer.
     *
     * @return An instance of the {@link coursesketch.server.interfaces.AbstractServerWebSocketHandler}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public AbstractServerWebSocketHandler createServerSocket() {
        return new ServerWebSocketHandler(this);
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
            singleWrapper = new ServerSocketWrapper(createServerSocket(), this.secure);
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
