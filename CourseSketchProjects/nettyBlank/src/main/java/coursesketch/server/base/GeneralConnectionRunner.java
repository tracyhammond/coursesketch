package coursesketch.server.base;

import coursesketch.server.interfaces.AbstractGeneralConnectionRunner;
import coursesketch.server.interfaces.ISocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

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

    /**
     * The group in charge of managing other groups.
     */
    private EventLoopGroup bossGroup;

    /**
     * Performs actual work on the server.
     */
    private EventLoopGroup workerGroup;

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

    }

    /**
     * Called to setup the system for if it is being run to connect to remote compters.
     */
    @Override
    protected void executeRemoveEnvironment() {

    }

    /**
     * Called to create an actual server.
     */
    @Override
    protected final void createServer() {
        server = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
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
            LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Called to add connections to the server.
     */
    @Override
    protected final void addConnections() {
        ((ServerWebSocketInitializer) getSocketInitailizerInstance()).setSslContext(sslCtx);
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler((ServerWebSocketInitializer) getSocketInitailizerInstance());
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
                try {
                    final ChannelFuture strap = server.bind(getPort());
                    final Channel channel = strap.sync().channel();
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        };
        serverThread.start();
        try {
            Thread.sleep(ONE_SECOND);
            final boolean assumedRunning = !workerGroup.isShutdown() && !workerGroup.isTerminated() && !workerGroup.isShuttingDown();
            LOG.info("Server is running hopefully = {}", assumedRunning);
            getSocketInitailizerInstance().reconnect();
        } catch (InterruptedException e) {
            LOG.info(LoggingConstants.EXCEPTION_MESSAGE, e);
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
     * @throws java.io.IOException
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
     *
     * @param timeOut
     *         length of specified timeout, in miliseconds
     * @param isSecure
     *         <code>true</code> if the servlet should be secure,
     *         <code>false</code> otherwise
     * @param isLocal
     *         <code>true</code> if the server is running locally,
     *         <code>false</code> otherwise
     * @return a new connection servlet for this server
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    protected ISocketInitializer createSocketInitializer(final long timeOut, final boolean isSecure, final boolean isLocal) {
        return new ServerWebSocketInitializer(timeOut, isSecure, isLocal);
    }

    /**
     * @return true if the server has not been started (basically run most has not been called yet)
     */
    @Override
    protected final boolean notServerStarted() {
        return false;
    }
}
