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
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by gigemjt on 10/19/14.
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
     * Context for SSL to take place.
     */
    private SslContext sslCtx;

    /**
     * The Netty server that handles communication.
     */
    private ServerBootstrap server;

    private DuplexTcpServerPipelineFactory serverFactory;

    private PeerInfo serverInfo;

    private NioEventLoopGroup boss;

    private NioEventLoopGroup workers;

    private RpcServerCallExecutor executor;

    private RpcTimeoutExecutor timeoutExecutor;

    private RpcTimeoutChecker timeoutChecker;

    private RpcConnectionEventNotifier localRpcEventLogger;

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *         the arguments from the server are then parsed.
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
     * Called to load the configuration data it can be overwritten to load specific data for each server.
     */
    @Override
    protected void loadConfigurations() {

    }

    /**
     * Called to setup the system if it is being run on a local computer with a local host.
     */
    @Override
    protected void executeLocalEnvironment() {
        localRpcEventLogger = LocalRpcEventLoggerFactory.createLocalEventLogger(LOG);
    }

    /**
     * Called to setup the system for if it is being run to connect to remote computers.
     */
    @Override
    protected void executeRemoteEnvironment() {

    }

    /**
     * Called to create an actual server.
     */
    @Override
    protected final void createServer() {
        final InetSocketAddress remoteAddress = new InetSocketAddress(this.getHostName(), this.getPort());
        serverInfo = new PeerInfo("127.0.0.1", this.getPort());

        executor = new ThreadPoolCallExecutor(3, 200);

        serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);
        serverFactory.setRpcServerCallExecutor(executor);
        server = new ServerBootstrap();
    }

    /**
     * Configures the SSL for the server.
     *
     * @param iKeystorePath
     *         the location of the keystore.
     * @param iCertificatePath
     *         the password for the keystore.
     */
    @Override
    protected final void configureSSL(final String iKeystorePath, final String iCertificatePath) {
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
            sslCtx = SslContext.newServerContext(new File(iCertificatePath), new File(iKeystorePath));
        } catch (SSLException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Called to add connections to the server.
     */
    @Override
    protected final void addConnections() {
        ((ServerWebSocketInitializer) getSocketInitailizerInstance()).setSslContext(sslCtx);

        boss = new NioEventLoopGroup(2,new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory()));
        workers = new NioEventLoopGroup(16,new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
        server.group(boss, workers);
        server.channel(NioServerSocketChannel.class);
        server.handler(new LoggingHandler(LogLevel.INFO));

        // TCP/IP settings
        server.option(ChannelOption.SO_SNDBUF, 1048576);
        server.option(ChannelOption.SO_RCVBUF, 1048576);
        server.childOption(ChannelOption.SO_RCVBUF, 1048576);
        server.childOption(ChannelOption.SO_SNDBUF, 1048576);
        server.option(ChannelOption.TCP_NODELAY, true);

        final ServerWebSocketInitializer socketInitializer = (ServerWebSocketInitializer) this.getSocketInitailizerInstance();
        socketInitializer.initChannel(serverFactory);

        timeoutExecutor = new TimeoutExecutor(1,5);
        timeoutChecker = new TimeoutChecker();
        timeoutChecker.setTimeoutExecutor(timeoutExecutor);
        timeoutChecker.startChecking(serverFactory.getRpcClientRegistry());

        if (this.isLocal()) {
            serverFactory.registerConnectionEventListener(localRpcEventLogger);
        }

        server.childHandler(serverFactory);
        server.localAddress(serverInfo.getPort());
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
                CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
                shutdownHandler.addResource(boss);
                shutdownHandler.addResource(workers);
                shutdownHandler.addResource(executor);
                shutdownHandler.addResource(timeoutChecker);
                shutdownHandler.addResource(timeoutExecutor);

                server.bind();
                LOG.info("Server started at http://" + getHostName() + ":" + getPort());
                LOG.info("Server is named {} ", serverInfo.getName());
            }
        };
        serverThread.start();
        try {
            Thread.sleep(ONE_SECOND);
            LOG.info("Server is running hopefully");
            getSocketInitailizerInstance().reconnect();
        } catch (InterruptedException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Parses extra commands that are taken in through the input line.
     *
     * @param command
     *         The command that is being processed.
     * @param sysin
     *         Used for additional input.
     * @return True if the message command is processed.
     * @throws IOException
     *         Thrown if there is a problem reading input.
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    protected boolean parseUtilityCommand(final String command, final BufferedReader sysin) throws IOException {
        return false;
    }

    /**
     * Stops the server.
     * Input is not stopped by the method.
     */
    @Override
    protected void stop() {

    }

    /**
     * Stops the server.
     * Input is not stopped by the method.
     */
    @Override
    protected void reconnect() {

    }

    /**
     * Returns a new instance of a {@link coursesketch.server.interfaces.ISocketInitializer}.
     * <p/>
     * Override this method if you want to return a subclass of
     * GeneralConnectionServlet
     *  @param timeOut
     *         length of specified timeout, in miliseconds
     * @param isSecure
     *         <code>true</code> if the servlet should be secure,
     *         <code>false</code> otherwise
     * @param isLocal
 *         <code>true</code> if the server is running locally,
 *         <code>false</code> otherwise
     * @param time
     * @param local @return a new connection servlet for this server
     * @param serverInfo
     * */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    protected ISocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new ServerWebSocketInitializer(serverInfo);
    }

    /**
     * @return true if the server has not been started (basically run most has not been called yet)
     */
    @Override
    protected final boolean notServerStarted() {
        return false;
    }
}