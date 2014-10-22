package coursesketch.server.base;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded
 * -examples.html#d0e18352
 */

import java.io.BufferedReader;
import java.io.IOException;

import coursesketch.server.interfaces.AbstractGeneralConnectionRunner;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Runs and sets up the server.
 * @author gigemjt
 *
 */
@SuppressWarnings("PMD.TooManyMethods")
public class GeneralConnectionRunner extends AbstractGeneralConnectionRunner {

    /**
     * A jetty server that is called upon by all of the other data.
     */
    private Server server;

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     */
    public static void main(final String[] args) {
        final GeneralConnectionRunner runner = new GeneralConnectionRunner(args);
        runner.start();
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
     * Attempts to load the SSL for the jetty server.
     * @param keystorePath The path to the private key.
     * @param iCertificatePath The path to the certificate.
     */
    @Override
    protected final void configureSSL(final String keystorePath, final String iCertificatePath) {

        final SslContextFactory contextfactor = new SslContextFactory();

        // Configure SSL

        // Use the real certificate
        System.out.println("Loaded real keystore");
        contextfactor.setKeyStorePath(keystorePath/* "srl01_tamu_edu.jks" */);
        contextfactor.setTrustStorePath(keystorePath);
        contextfactor.setTrustStorePassword(iCertificatePath);
        // cf.setCertAlias("nss324-o");
        // cf.checkKeyStore();
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(contextfactor,
                org.eclipse.jetty.http.HttpVersion.HTTP_1_1.toString());

        final HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(getPort());
        config.setOutputBufferSize(DEFAULT_OUTPUT_BUFFER_SIZE);
        config.setRequestHeaderSize(DEFAULT_REQUEST_HEADER_SIZE);
        config.setResponseHeaderSize(DEFAULT_RESPONSE_HEADER_SIZE);
        final HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);

        final ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        connector.setPort(getPort());
        server.addConnector(connector);

        server.setConnectors(new Connector[] {connector});
    }

    /**
     * Called to load the configuration data it can be overwritten to load specific data for each server.
     */
    @Override
    public final void loadConfigurations() {
        // loading configuration code goes here.
    }

    /**
     * Called to setup the system if it is being run on a local computer with a local host.
     */
    @Override
    public void executeLocalEnvironment() {
        // does nothing by default.
    }

    /**
     * Called to setup the system for if it is being run to connect to remote compters.
     */
    @Override
    public void executeRemoveEnvironment() {
        // does nothing by default.
    }

    /**
     * Sets up a Jetty embedded server. Uses The given port
     */
    @Override
    public final void createServer() {
        server = new Server(getPort());
        System.out.println("Server has been created on port: " + getPort());
    }

    /**
     * Adds the servlets to the server.  And sets up the port for it.
     */
    public final void addConnections() {
        final StatisticsHandler stats = new StatisticsHandler();
        /*
         * ServletContextHandler servletHandler = new
         * ServletContextHandler(ServletContextHandler.SESSIONS);
         * servletHandler.setContextPath("/coursesketch");
         * servletHandler.addServlet(new ServletHolder(new
         * GeneralConnectionServlet()),"/");
         */
        final ServletHandler servletHandler = new ServletHandler();

        System.out.println("Creating a new servlet");

        servletHandler.addServletWithMapping(new ServletHolder((ServerWebSocketInitializer) getSocketInitailizerInstance()), "/*");
        stats.setHandler(servletHandler);

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {stats});

        server.setHandler(handlers);
    }

    /**
     * Starts the server in a separate thread.
     * A server can only be run once.
     */
    @Override
    public final void startServer() {
        final Thread serverThread = new Thread() {
            @Override
            @SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidCatchingGenericException" })
            public void run() {
                try {
                    server.start();
                    getSocketInitailizerInstance().reconnect();
                    server.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
    }

    /**
     * Returns a new instance of a {@link ServerWebSocketInitializer}.
     *
     * Override this method if you want to return a subclass of
     * GeneralConnectionServlet
     *
     * @param timeOut
     *            length of specified timeout, in miliseconds
     * @param isSecure
     *            <code>true</code> if the servlet should be secure,
     *            <code>false</code> otherwise
     * @param isLocal
     *            <code>true</code> if the server is running locally,
     *            <code>false</code> otherwise
     *
     * @return a new connection servlet for this server
     */
    @SuppressWarnings("checkstyle:designforextension")
    public ServerWebSocketInitializer createSocketInitializer(final long timeOut, final boolean isSecure, final boolean isLocal) {
        if (!isSecure && isProduction()) {
            System.err.println("Running an insecure server");
        }
        return new ServerWebSocketInitializer(timeOut, isSecure, isLocal);
    }

    /**
     * @return true if the server has not started accepting connections yet.
     */
    @Override
    protected final boolean notServerStarted() {
        return this.server == null || !this.server.isRunning();
    }

    // FUTURE: add a command manager of some sort.
    /**
     * Parses extra commands that are taken in through the input line.
     * @param command The command that is being processed.
     * @param sysin Used for additional input.
     * @return True if the message command is processed.
     * @throws IOException Thrown if there is a problem reading input.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public boolean parseUtilityCommand(final String command, final BufferedReader sysin) throws IOException {
        if ("connectionNumber".equals(command)) {
            System.out.println(getSocketInitailizerInstance().getCurrentConnectionNumber());
            return true;
        }
        return false;
    }

    /**
     * stops the server and resets the state.
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public final void stop() {
        try {
            server.stop();
            getSocketInitailizerInstance().stop();
            server = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempts to reconnect all clients.
     */
    @Override
    protected final void reconnect() {
        getSocketInitailizerInstance().reconnect();
    }

    /**
     * @return The server that has been created by this runner.
     */
    public final Server getServer() {
        return server;
    }
}
