package multiconnection;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded
 * -examples.html#d0e18352
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import interfaces.IGeneralConnectionRunner;
import org.apache.commons.lang3.StringUtils;
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
public class GeneralConnectionRunner implements IGeneralConnectionRunner {

    /**
     * Max buffer size for the output in bytes.
     */
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 32786;

    /**
     * Max buffer size for the header in bytes.
     */
    private static final int DEFAULT_REQUEST_HEADER_SIZE = 8192;

    /**
     * Max buffer size for the response header in bytes.
     */
    private static final int DEFAULT_RESPONSE_HEADER_SIZE = 8192;

    /**
     * The default port is 8888.
     */
    private static final int DEFAULT_PORT = 8888;

    /**
     * A local instance is stored here.
     */
    private final IGeneralConnectionRunner localInstance = this;

    /**
     * A jetty server that is called upon by all of the other data.
     */
    private Server server;

    /**
     * The servlet that is connected to the server.  (it is typically binded to a certain URL)
     */
    private GeneralConnectionServlet servletInstance;

    // these should be changed based on the properties
    /**
     * Arguments that come in from the command line.
     */
    private final String[] args;

    /**
     * The port of the server.
     */
    private int port = DEFAULT_PORT;

    /**
     * The timeoutTime of a connection.
     */
    private long timeoutTime;

    /**
     * If true then the server will accept commandline input.  If false the server will not accept commandline input.
     */
    private boolean acceptInput = true;

    /**
     * If true then the server is treated as a production server.
     */
    @SuppressWarnings("PMD.ImmutableField")
    private boolean production = false;

    /**
     * If true then the server will try and run as if it is running on a local computer (this is used for testing).
     */
    private boolean local = true;

    /**
     * If true then the server will perform logging.
     */
    private boolean logging = false;

    /**
     * True if the server is using SSL and false otherwise.
     */
    @SuppressWarnings("PMD.ImmutableField")
    private boolean secure = false;

    /**
     * The password for the keystore.
     */
    private String keystorePassword = "";

    /**
     * The location the keystore is stored in.
     */
    private String keystorePath = "";

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     */
    public static void main(final String[] args) {
        final GeneralConnectionRunner runner = new GeneralConnectionRunner(args);
        runner.runAll();
    }

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *            the arguments from the server are then parsed.
     */
    protected GeneralConnectionRunner(final String[] arguments) {
        this.args = Arrays.copyOf(arguments, arguments.length);
        if (arguments.length >= 1 && arguments[0].equals("local")) {
            local = true;
        } else {
            local = false;
        }
        production = false;
        secure = false;
    }

    /**
     * Runs the entire startup process including input.
     *
     */
    protected final void runAll() {
        this.runMost();
        this.startInput();
    }

    /**
     * Configures the SSL for the server.
     * Ignoring this method for now.
     */
    private void configureSSL() {

        final SslContextFactory contextfactor = new SslContextFactory();

        // Configure SSL

        // Use the real certificate
        System.out.println("Loaded real keystore");
        contextfactor.setKeyStorePath(keystorePath/* "srl01_tamu_edu.jks" */);
        contextfactor.setTrustStorePath(keystorePath);
        contextfactor.setTrustStorePassword(keystorePassword);
        // cf.setCertAlias("nss324-o");
        // cf.checkKeyStore();
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(contextfactor,
                org.eclipse.jetty.http.HttpVersion.HTTP_1_1.toString());

        final HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(port);
        config.setOutputBufferSize(DEFAULT_OUTPUT_BUFFER_SIZE);
        config.setRequestHeaderSize(DEFAULT_REQUEST_HEADER_SIZE);
        config.setResponseHeaderSize(DEFAULT_RESPONSE_HEADER_SIZE);
        final HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);

        final ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        connector.setPort(port);
        server.addConnector(connector);

        server.setConnectors(new Connector[] {connector});
    }

    /**
     * Runs the majority of the startup process.
     *
     * Does not handle accepting Input.
     */
    protected final void runMost() {
        this.loadConfigurations();
        if (local) {
            this.executeLocalEnviroment();
        } else {
            this.executeRemoveEnviroment();
        }
        this.createServer();

        if (secure) {
            configureSSL();
        }

        this.addServletHandlers();

        this.startServer();
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
    public void executeLocalEnviroment() {
        // does nothing by default.
    }

    /**
     * Called to setup the system for if it is being run to connect to remote compters.
     */
    @Override
    public void executeRemoveEnviroment() {
        // does nothing by default.
    }

    /**
     * Sets up a Jetty embedded server. Uses The given port
     *
     */
    @Override
    public final void createServer() {
        server = new Server(port);
        System.out.println("Server has been created on port: " + port);
    }

    /**
     * Adds the servlets to the server.  And sets up the port for it.
     */
    public final void addServletHandlers() {
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

        // FUTURE: change this to true!
        servletInstance = getServlet(timeoutTime, false, local);

        servletHandler.addServletWithMapping(new ServletHolder(servletInstance), "/*");
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
                    servletInstance.reconnect();
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
     * Returns a new instance of a {@link GeneralConnectionServlet}.
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
    public GeneralConnectionServlet getServlet(final long timeOut, final boolean isSecure, final boolean isLocal) {
        if (!isSecure && production) {
            System.err.println("Running an insecure server");
        }
        return new GeneralConnectionServlet(timeOut, isSecure, isLocal);
    }

    /**
     * Handles commands that can be used to perform certain functionality.
     *
     * This method can and in some cases should be overwritten. We
     * <b>strongly</b> suggest that you call super first then check to see if it
     * is true and then call your overwritten method.
     *
     * @param command
     *            The command that is parsed to provide functionality.
     * @param sysin
     *            Used if additional input is needed for the command.
     * @return true if the command is an accepted command and is used by the
     *         server
     * @throws IOException an I/O error
     * @throws InterruptedException the thread is interrupted.
     */
    @Override
    public final boolean parseCommand(final String command, final BufferedReader sysin) throws IOException, InterruptedException {
        if (command == null) {
            return true;
        }
        if ("exit".equals(command)) {
            exitCommand(sysin);
            return true;
        } else if ("restart".equals(command)) {
            restartCommand(sysin);
            return true;
        } else if ("reconnect".equals(command)) {
            servletInstance.reconnect();
            return true;
        } else if ("stop".equals(command)) {
            stopCommand(sysin);
            return true;
        } else if ("start".equals(command)) {
            startCommand();
            return true;
        }
        return parseUtilityCommand(command, sysin);
    }

    /**
     * A command for exiting the server.
     * @param sysin Keyboard input.
     * @throws IOException Thrown if there are problems getting the keyboard input.
     */
    private void exitCommand(final BufferedReader sysin) throws IOException {
        System.out.println("Are you sure you want to exit? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
            acceptInput = false;
            System.out.println("Stopped accepting input");
        }
    }

    /**
     * A command for restarting the server.
     * @param sysin Keyboard input.
     * @throws InterruptedException Thrown if the thread is interrupted while waiting.
     * @throws IOException Thrown if there are problems getting the keyboard input.
     */
    private void restartCommand(final BufferedReader sysin) throws IOException, InterruptedException {
        final int waitDelay = 1000;
        System.out.println("Are you sure you want to restart? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
            System.out.println("sleeping for 1s");
            Thread.sleep(waitDelay);
            this.runMost();
        }
    }

    /**
     * A command for stopping the server.
     * @param sysin Keyboard input.
     * @throws IOException Thrown if there are problems getting the keyboard input.
     */
    private void stopCommand(final BufferedReader sysin) throws IOException {
        System.out.println("Are you sure you want to stop? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
        }
    }

    /**
     * A command for starting the server.
     * @throws IOException Thrown if there are problems getting the keyboard input.
     */
    private void startCommand() throws IOException {
        if (this.server == null || !this.server.isRunning()) {
            this.runMost();
        } else {
            System.out.println("you can not start the because it is already running.");
        }
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
        if ("toggle logging".equals(command)) {
            if (logging) {
                System.out.println("Are you sure you want to turn loggin off? [y/n]");
                if (!StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
                    System.out.println("action canceled");
                    return true;
                }
            }
            String isLoggingStr = "Off";
            if (logging) {
                isLoggingStr = "On";
            }
            System.out.println("Turning loggin " + isLoggingStr);
            logging = !logging;
            return true;
        }
        if ("connectionNumber".equals(command)) {
            System.out.println(servletInstance.getCurrentConnectionNumber());
            return true;
        }
        return false;
    }

    /**
     * Starts the system that accepts command line input.
     */
    @Override
    public final void startInput() {
        final Thread inputThread = new Thread() {
            @Override
            @SuppressWarnings("PMD.CommentRequired")
            public void run() {
                final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
                while (acceptInput) {
                    try {
                        final String command = sysin.readLine();
                        localInstance.parseCommand(command, sysin);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        inputThread.start();
    }

    /**
     * Stops the server.
     * Input is not stopped by the method.
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public final void stop() {
        try {
            server.stop();
            servletInstance.stop();
            server = null;
            servletInstance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the password for the SSL keystore.
     *
     * @param pass
     *            password to set for the keystore.
     */
    protected final void setKeystorePassword(final String pass) {
        if (this.keystorePath != null) {
            throw new IllegalStateException("password is already set throwing an error.");
        }
        this.keystorePassword = pass;
    }

    /**
     * Sets the path where the keyStore is located.
     * @param path The location of the keyStore.
     */
    protected final void setKeystorePath(final String path) {
        if (this.keystorePath != null) {
            throw new IllegalStateException("Key path is already set throwing an error.");
        }
        this.keystorePath = path;
    }

    /**
     * @return The arguments that were used to start this program.
     */
    @Override
    public final String[] getArgs() {
        return args.clone();
    }

    /**
     * @return The server that has been created by this runner.
     */
    @Override
    public final Server getServer() {
        return server;
    }

    /**
     * @return An instance of the servlet created by this runner.
     */
    public final GeneralConnectionServlet getServletInstance() {
        return servletInstance;
    }

    /**
     * @return The port number that this server is connected to.
     */
    @Override
    public final int getPort() {
        return port;
    }

    /**
     * @param portToSet The port number that this server is connected to.
     */
    protected final void setPort(final int portToSet) {
        this.port = portToSet;
    }

    /**
     * @return The time it takes for a connection to timeout.
     */
    @Override
    public final long getTimeoutTime() {
        return timeoutTime;
    }

    /**
     * Sets the timeout time.
     * @param timeoutTimeToSet The time it takes to time out a connection.
     */
    protected final void setTimeoutTime(final long timeoutTimeToSet) {
        this.timeoutTime = timeoutTimeToSet;
    }

    /**
     * @return true if the command-line is accepting input.
     */
    @Override
    public final boolean isAcceptingCommandInput() {
        return acceptInput;
    }

    /**
     * @param acceptInputToSet True if the command line will accept input.  False otherwise.
     */
    @Override
    public final void setAcceptingCommandInput(final boolean acceptInputToSet) {
        this.acceptInput = acceptInputToSet;
    }

    /**
     * @return True if the server is running as a production environment.
     */
    @Override
    public final boolean isProduction() {
        return production;
    }

    /**
     * @return True if the server is attempting to run as a local server.  (used for testing)
     */
    @Override
    public final boolean isLocal() {
        return local;
    }

    /**
     * @return True if the computer is logging.
     */
    @Override
    public final boolean isLogging() {
        return logging;
    }

    /**
     * @return True if the server is accepting connections via SSL.
     */
    protected final boolean isSecure() {
        return secure;
    }
}
