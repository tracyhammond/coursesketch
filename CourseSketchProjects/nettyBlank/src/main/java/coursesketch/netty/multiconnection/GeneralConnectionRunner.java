package coursesketch.netty.multiconnection;

import interfaces.IGeneralConnectionRunner;
import interfaces.ISocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

/**
 * Created by gigemjt on 10/19/14.
 */
public class GeneralConnectionRunner extends IGeneralConnectionRunner {

    /**
     * Context for SSL to take place.
     */
    private SslContext sslCtx;

    private ServerBootstrap server;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     */
    public static void main(final String[] args) {
        final GeneralConnectionRunner run = new GeneralConnectionRunner(args);
        run.start();
    }

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *            the arguments from the server are then parsed.
     */
    protected GeneralConnectionRunner(final String[] arguments) {
        super(arguments);
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
    protected void executeLocalEnviroment() {

    }

    /**
     * Called to setup the system for if it is being run to connect to remote compters.
     */
    @Override
    protected void executeRemoveEnviroment() {

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
     * @param iKeystorePath     the location of the keystore.
     * @param iCertificatePath the password for the keystore.
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
            e.printStackTrace();
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
            @SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidCatchingGenericException" })
            public void run() {
                try {
                    final Channel ch = server.bind(getPort()).sync().channel();

                    ch.closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        };
        serverThread.start();
    }

    /**
     * Parses extra commands that are taken in through the input line.
     *
     * @param command The command that is being processed.
     * @param sysin   Used for additional input.
     * @return True if the message command is processed.
     * @throws java.io.IOException Thrown if there is a problem reading input.
     */
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
     * Returns a new instance of a {@link interfaces.ISocketInitializer}.
     * <p/>
     * Override this method if you want to return a subclass of
     * GeneralConnectionServlet
     *
     * @param timeOut  length of specified timeout, in miliseconds
     * @param isSecure <code>true</code> if the servlet should be secure,
     *                 <code>false</code> otherwise
     * @param isLocal  <code>true</code> if the server is running locally,
     *                 <code>false</code> otherwise
     * @return a new connection servlet for this server
     */
    @Override
    protected ISocketInitializer getSocketInitializer(final long timeOut, final boolean isSecure, final boolean isLocal) {
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
