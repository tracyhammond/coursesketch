package coursesketch.server.compat;

import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import coursesketch.server.interfaces.AbstractClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.rpc.LocalRpcEventLoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ConnectionException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Created by gigemjt on 10/22/14.
 */
public class ClientWebSocket extends AbstractClientWebSocket {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClientWebSocket.class);

    /**
     * The code that is used by the Html aggregator.
     */
    private static final int TIME_OUT_MILLIS = 10000;

    /**
     * Size of the send buffer
     */
    private static final int SIZE_OF_SEND_BUFFER = 1048576;

    /**
     * Size of the recieving buffer
     */
    private static final int SIZE_OF_RCV_BUFFER = 1048576;

    /**
     * Max number of threads for the client
     */
    private static final int MAX_THREAD_POOL_SIZE = 100;

    /**
     * core number of threads for the client
     */
    private static final int CORE_THREAD_POOL_SIZE = 3;

    /**
     * MThe offset from the host port for the client port.
     */
    private static final int CLIENT_PORT_OFFSET = 100;

    /**
     * An Rpc Client channel
     */
    private RpcClientChannel channel = null;

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()} or call
     * {@link coursesketch.server.interfaces.AbstractClientWebSocket#send(ByteBuffer)}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer
     *         The server that is using this connection wrapper.
     */
    protected ClientWebSocket(final URI iDestination, final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    /**
     * Attempts to connect to the server at URI with a webSocket Client.
     *
     * @throws ConnectionException
     *         Throws an exception if an error occurs during the connection attempt.
     */
    @Override
    protected final void connect() throws ConnectionException {
        final SslContext sslCtx = null;
        /*
        if (getParentServer().) {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslCtx = null;
        }
        */
        final InetSocketAddress remoteAddress = new InetSocketAddress(getURI().getHost(), getURI().getPort());
        if (remoteAddress.isUnresolved()) {
            throw new ConnectionException("Remote address does not exist " + remoteAddress.getHostString());
        }
        final PeerInfo client = new PeerInfo(this.getParentServer().getHostName());
        LOG.info("Resolved address {}", remoteAddress.getAddress());
        final PeerInfo server = new PeerInfo(remoteAddress.getHostName(), remoteAddress.getPort());

        final DuplexTcpClientPipelineFactory clientFactory = new DuplexTcpClientPipelineFactory();
        clientFactory.setClientInfo(client);

        final RpcServerCallExecutor executor = new ThreadPoolCallExecutor(CORE_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE);
        clientFactory.setRpcServerCallExecutor(executor);

        clientFactory.setConnectResponseTimeoutMillis(TIME_OUT_MILLIS);
        clientFactory.setCompression(false);

        clientFactory.setRpcLogger(new CategoryPerServiceLogger());

        RpcTimeoutExecutor timeoutExecutor = new TimeoutExecutor(1,5);
        RpcTimeoutChecker checker = new TimeoutChecker();
        checker.setTimeoutExecutor(timeoutExecutor);
        checker.startChecking(clientFactory.getRpcClientRegistry());

        CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
        shutdownHandler.addResource(executor);
        shutdownHandler.addResource(checker);
        shutdownHandler.addResource(timeoutExecutor);

        clientFactory.registerConnectionEventListener(LocalRpcEventLoggerFactory.createLocalEventLogger(LOG));

        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.handler(clientFactory);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, false);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIME_OUT_MILLIS);
        bootstrap.option(ChannelOption.SO_SNDBUF, SIZE_OF_SEND_BUFFER);
        bootstrap.option(ChannelOption.SO_RCVBUF, SIZE_OF_RCV_BUFFER);

        shutdownHandler.addResource(bootstrap.group());

        channel = null;
        try {
            channel = clientFactory.peerWith(server, bootstrap);
        } catch (IOException e) {
            LOG.error("Unable to connect to server", e);
            throw new ConnectionException("Unable to connect to server " + server.getName());
        }

        final ClientWebSocketWrapper wrapper = new ClientWebSocketWrapper(channel, this);
        clientFactory.getRpcServiceRegistry().registerService(wrapper);
        clientFactory.registerConnectionEventListener(wrapper);
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * @param buffer
     *         The message that is received by this object.
     */
    @Override protected void onMessage(final ByteBuffer buffer) {

    }

    /**
     * @return A {@link RpcClientChannel} so that protobuf can send messages.
     */
    public RpcClientChannel getRpcChannel() {
        return channel;
    }

    /**
     * @return A new instance of{@link RpcController}.
     */
    public RpcController getnewRpcController() {
        return channel.newRpcController();
    }
}
