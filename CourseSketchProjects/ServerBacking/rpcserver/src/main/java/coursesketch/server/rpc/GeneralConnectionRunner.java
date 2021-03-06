package coursesketch.server.rpc;

import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;
import coursesketch.server.interfaces.AbstractGeneralConnectionRunner;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.interfaces.ServerInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * The general connection runner for starting a server based on an rpc system.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class GeneralConnectionRunner extends AbstractGeneralConnectionRunner {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GeneralConnectionRunner.class);

    /**
     * 1000ms = 1s.
     */
    private static final int ONE_SECOND = 1000;

    /**
     * Size of the send buffer.
     */
    private static final int SIZE_OF_SEND_BUFFER = 1048576;

    /**
     * Size of the receiving buffer.
     */
    private static final int SIZE_OF_RCV_BUFFER = 1048576;

    /**
     * Max number of threads for the client.
     */
    private static final int MAX_THREAD_POOL_SIZE = 200;

    /**
     * Core number of threads for the client.
     */
    private static final int CORE_THREAD_POOL_SIZE = 3;

    /**
     * Number of workers.
     */
    private static final int NUMBER_EVENT_WORKERS = 16;

    /**
     * The max number of threads for the timeout executor.
     */
    private static final int MAX_TIMEOUT_POOL_SIZE = 5;

    /**
     * Context for SSL to take place.
     */
    private SslContext sslContext;

    /**
     * The Netty server that handles communication.
     */
    private ServerBootstrap server;

    /**
     * A factory used to set details about the connection.
     *
     * Also allows for duplex connections (both ways).
     */
    private DuplexTcpServerPipelineFactory serverFactory;

    /**
     * Contains information about the server, mainly the hostname and the port.
     */
    private PeerInfo peerInfo;

    /**
     * This is the boss event loop group.
     */
    private NioEventLoopGroup boss;

    /**
     * This is the worker event loop group.
     */
    private NioEventLoopGroup workers;

    /**
     * Handles thread management for the Rpc Class.
     */
    private RpcServerCallExecutor executor;

    /**
     * Handles thread management for the timeout of calls.
     */
    private RpcTimeoutExecutor timeoutExecutor;

    /**
     * Checks to see if a call is timing out.
     */
    private RpcTimeoutChecker timeoutChecker;

    /**
     * Logs rpc events locally.
     */
    private RpcConnectionEventNotifier localRpcEventLogger;

    /**
     * Parses the arguments from the server.
     *
     * This only expects a single argument indicating if the server is local.
     *
     * @param arguments
     *         The arguments from the server are then parsed.
     */
    protected GeneralConnectionRunner(final String... arguments) {
        super(arguments);
        super.setCertificatePath("/Users/gigemjt/workspace/coursesketch/config/localssl/server.crt");
        super.setKeystorePath("/Users/gigemjt/workspace/coursesketch/config/localssl/serverpk8.key");
    }

    /**
     * The main method that can be used to run a server.
     *
     * @param args
     *         Input arguments that are running the server.
     */
    public static void main(final String... args) {
        final GeneralConnectionRunner run = new GeneralConnectionRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadConfigurations() {
        // does nothing by default
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The RpcServer Sets up a logger that logs every rpc event for extra debugging.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void executeLocalEnvironment() {
        localRpcEventLogger = LocalRpcEventLoggerFactory.createLocalEventLogger(LOG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void executeRemoteEnvironment() {
        // does nothing by default
    }

    /**
     * Called to create an actual server.
     */
    @Override
    protected final void createServer() {
        peerInfo = new PeerInfo(this.getHostName(), this.getPort());

        executor = new ThreadPoolCallExecutor(CORE_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE);

        serverFactory = new DuplexTcpServerPipelineFactory(peerInfo);
        serverFactory.setRpcServerCallExecutor(executor);
        server = new ServerBootstrap();
    }

    /**
     * Configures the SSL for the server.
     *
     * @param keystorePath
     *         The location of the keystore.
     * @param certificatePath
     *         The password for the keystore.
     */
    @Override
    protected final void configureSSL(final String keystorePath, final String certificatePath) {
        // TO GENERATE NEEDED FILES FOR SSL.
        /*
            openssl genrsa -des3 -out server.key 1024
            openssl req -new -key server.key -out server.csr
            cp server.key server.key.org
            openssl rsa -in server.key.org -out server.key
            openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
            openssl pkcs8 -topk8 -nocrypt -in server.key -out serverpk8.key
        */
        try {
            sslContext = SslContext.newServerContext(new File(certificatePath), new File(keystorePath));
        } catch (SSLException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Called to add connections to the server.
     */
    @Override
    protected final void addConnections() {
        ((ServerWebSocketInitializer) getSocketInitailizerInstance()).setSslContext(sslContext);

        boss = new NioEventLoopGroup(2, new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory()));
        workers = new NioEventLoopGroup(NUMBER_EVENT_WORKERS, new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
        server.group(boss, workers);
        server.channel(NioServerSocketChannel.class);
        server.handler(new LoggingHandler(LogLevel.INFO));

        // TCP/IP settings
        server.option(ChannelOption.SO_SNDBUF, SIZE_OF_SEND_BUFFER);
        server.option(ChannelOption.SO_RCVBUF, SIZE_OF_RCV_BUFFER);
        server.childOption(ChannelOption.SO_SNDBUF, SIZE_OF_SEND_BUFFER);
        server.childOption(ChannelOption.SO_RCVBUF, SIZE_OF_RCV_BUFFER);
        server.option(ChannelOption.TCP_NODELAY, true);

        final ServerWebSocketInitializer socketInitializer = (ServerWebSocketInitializer) this.getSocketInitailizerInstance();
        socketInitializer.initChannel(serverFactory);

        timeoutExecutor = new TimeoutExecutor(1, MAX_TIMEOUT_POOL_SIZE);
        timeoutChecker = new TimeoutChecker();
        timeoutChecker.setTimeoutExecutor(timeoutExecutor);
        timeoutChecker.startChecking(serverFactory.getRpcClientRegistry());

        if (this.isLocal()) {
            serverFactory.registerConnectionEventListener(localRpcEventLogger);
        }

        server.childHandler(serverFactory);
        server.localAddress(peerInfo.getPort());
    }

    /**
     * Starts the server in a separate thread.
     * A server can only be run once.
     */
    @Override
    protected final void startServer() {
        final Thread serverThread = new Thread() {
            @Override
            @SuppressWarnings({ "PMD.CommentRequired", "PMD.AvoidCatchingGenericException" })
            public void run() {
                final CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
                shutdownHandler.addResource(boss);
                shutdownHandler.addResource(workers);
                shutdownHandler.addResource(executor);
                shutdownHandler.addResource(timeoutChecker);
                shutdownHandler.addResource(timeoutExecutor);

                server.bind();
                LOG.info("Server started at http://" + getHostName() + ":" + getPort());
                LOG.info("Server is named {} ", peerInfo.getName());
            }
        };
        serverThread.start();
        try {
            Thread.sleep(ONE_SECOND);
            LOG.info("Server is running hopefully");
            getSocketInitailizerInstance().reconnect();
            getSocketInitailizerInstance().onServerStart();
        } catch (InterruptedException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Parses extra commands that are taken in through the input line.
     *
     * @param command
     *         The command that is being processed.
     * @param systemIn
     *         Used for additional input.
     * @return True if the message command is processed.
     * @throws IOException
     *         Thrown if there is a problem reading input.
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    protected boolean parseUtilityCommand(final String command, final BufferedReader systemIn) throws IOException {
        // processes no messages so false is returned.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stop() {
        // does nothing by default
    }

    /**
     * {@inheritDoc}
     *
     * @return A new instance of a {@link ISocketInitializer}.
     * */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    protected ISocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new ServerWebSocketInitializer(serverInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @return false.
     */
    @Override
    protected final boolean serverStarted() {
        // returns false by default.
        return true;
    }
}
