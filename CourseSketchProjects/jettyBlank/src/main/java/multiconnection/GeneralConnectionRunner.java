package multiconnection;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded-examples.html#d0e18352
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

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

public class GeneralConnectionRunner {

    public static void main(final String[] args) throws Exception {
        GeneralConnectionRunner runner = new GeneralConnectionRunner(args);
        try {
            runner.runAll();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected GeneralConnectionRunner(final String[] arguments) {
        this.args = arguments;
        if (arguments.length >= 1 && arguments[0].equals("local")) {
            local = true;
        } else {
            local = false;
        }
    }

    private static final int DEFAULT_PORT = 8888;

    private final GeneralConnectionRunner localInstance = this;
    private Server server;
    private GeneralConnectionServlet servletInstance;

    // these should be changed based on the properties
    private final String[] args;
    private int port = DEFAULT_PORT;
    private long timeoutTime;
    private boolean acceptInput = true;
    private boolean production = false;
    private boolean local = true;
    private boolean logging = false;
    private boolean secure = false;
    private String keystorePassword = "";
    private String keystorePath = "";

    /**
     * Runs the entire startup process including input.
     *
     * @throws Exception when instatntating the server fails.
     */
    protected final void runAll() throws Exception {
        this.runMost();
        this.startInput();
    }

    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 32786;
    private static final int DEFAULT_REQUEST_HEADER_SIZE = 8192;
    private static final int DEFAULT_RESPONSE_HEADER_SIZE = 8192;

    private void configureSSL() {

        SslContextFactory cf = new SslContextFactory();

        // Configure SSL

        // Use the real certificate
        System.out.println("Loaded real keystore");
        cf.setKeyStorePath(keystorePath/* "srl01_tamu_edu.jks" */);
        cf.setTrustStorePath(keystorePath);
        cf.setTrustStorePassword(keystorePassword);
        // cf.setCertAlias("nss324-o");
        // cf.checkKeyStore();
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(
                cf, org.eclipse.jetty.http.HttpVersion.HTTP_1_1.toString());

        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(port);
        config.setOutputBufferSize(DEFAULT_OUTPUT_BUFFER_SIZE);
        config.setRequestHeaderSize(DEFAULT_REQUEST_HEADER_SIZE);
        config.setResponseHeaderSize(DEFAULT_RESPONSE_HEADER_SIZE);
        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(
                sslConfiguration);

        ServerConnector connector = new ServerConnector(server,
                sslConnectionFactory, httpConnectionFactory);
        connector.setPort(port);
        server.addConnector(connector);

        server.setConnectors(new Connector[] {connector});
    }

    /**
     * Runs the majority of the startup process.
     *
     * Does not handle accepting Input
     *
     * @throws Exception if there is an error instantiating the server
     */
    protected final void runMost() throws Exception {
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

    public void loadConfigurations() {

    }

    public void executeLocalEnviroment() {

    }

    public void executeRemoveEnviroment() {

    }

    /**
     * Sets up a Jetty embedded server. Uses HTTPS over port 12102 and a key
     * certificate.
     *
     * @throws Exception if there is an error instantiating the server
     */
    public final void createServer() throws Exception {
        server = new Server(port);
        System.out.println("Server has been created on port: " + port);
    }

    public final void addServletHandlers() {
        StatisticsHandler stats = new StatisticsHandler();
        /*
         * ServletContextHandler servletHandler = new
         * ServletContextHandler(ServletContextHandler.SESSIONS);
         * servletHandler.setContextPath("/coursesketch");
         * servletHandler.addServlet(new ServletHolder(new
         * GeneralConnectionServlet()),"/");
         */
        ServletHandler servletHandler = new ServletHandler();

        System.out.println("Creating a new servlet");

        // TODO change this to true!
        servletInstance = getServlet(timeoutTime, false, local);

        servletHandler.addServletWithMapping(
                new ServletHolder(servletInstance), "/*");
        stats.setHandler(servletHandler);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {stats});

        server.setHandler(handlers);
    }

    public final void startServer() {
        Thread d = new Thread() {
            @Override
            public void run() {
                try {
                    server.start();
                    servletInstance.reconnect();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        d.start();
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
    public final GeneralConnectionServlet getServlet(final long timeOut, final boolean isSecure,
            final boolean isLocal) {
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
     * @throws Exception
     *             if an error with I/O or instantiating the server occurs, or
     *             the thread is interrupted
     */
    public final boolean parseCommand(final String command,
            final BufferedReader sysin) throws Exception {
        final int waitDelay = 1000;
        if (command == null) {
            return true;
        }
        if (command.equals("exit")) {
            System.out.println("Are you sure you want to exit? [y/n]");
            if (sysin.readLine().equalsIgnoreCase("y")) {
                this.stop();
                acceptInput = false;
                System.out.println("Stopped accepting input");
            }
            return true;
        } else if (command.equals("restart")) {
            System.out.println("Are you sure you want to restart? [y/n]");
            if (sysin.readLine().equalsIgnoreCase("y")) {
                this.stop();
                System.out.println("sleeping for 1s");
                Thread.sleep(waitDelay);
                this.runMost();
            }
            return true;
        } else if (command.equals("reconnect")) {
            servletInstance.reconnect();
            return true;
        } else if (command.equals("stop")) {
            System.out.println("Are you sure you want to stop? [y/n]");
            if (sysin.readLine().equalsIgnoreCase("y")) {
                this.stop();
            }
            return true;
        } else if (command.equals("start")) {
            if (this.server == null || !this.server.isRunning()) {
                this.runMost();
            } else {
                System.out
                        .println("you can not start the because it is already running.");
            }
            return true;
        }
        return parseUtilityCommand(command, sysin);
    }

    // TODO add a command manager of some sort.
    public final boolean parseUtilityCommand(final String command, final BufferedReader sysin)
            throws Exception {
        if (command.equals("toggle logging")) {
            if (logging) {
                System.out
                        .println("Are you sure you want to turn loggin off? [y/n]");
                if (!sysin.readLine().equalsIgnoreCase("y")) {
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
        if (command.equals("connectionNumber")) {
            System.out.println(servletInstance.getCurrentConnectionNumber());
            return true;
        }
        return false;
    }

    public final void startInput() {
        Thread d = new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader sysin = new BufferedReader(
                            new InputStreamReader(System.in));
                    while (acceptInput) {
                        String in = sysin.readLine();
                        try {
                            localInstance.parseCommand(in, sysin);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        d.start();
    }

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
     * @param pass password to set for the keystore
     */
    protected final void setKeystorePassword(final String pass) {
        this.keystorePassword = pass;
    }

    protected final void setKeystorePath(final String path) {
        this.keystorePath = path;
    }

    public final String[] getArgs() {
        return args;
    }

    public final Server getServer() {
        return server;
    }

    public final void setServer(final Server serverToSet) {
        this.server = serverToSet;
    }

    public final GeneralConnectionServlet getServletInstance() {
        return servletInstance;
    }

    public final void setServletInstance(final GeneralConnectionServlet servletInstanceToSet) {
        this.servletInstance = servletInstanceToSet;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(final int portToSet) {
        this.port = portToSet;
    }

    public final long getTimeoutTime() {
        return timeoutTime;
    }

    public final void setTimeoutTime(final long timeoutTimeToSet) {
        this.timeoutTime = timeoutTimeToSet;
    }

    public final boolean isAcceptInput() {
        return acceptInput;
    }

    public final void setAcceptInput(final boolean acceptInputToSet) {
        this.acceptInput = acceptInputToSet;
    }

    public final boolean isProduction() {
        return production;
    }

    public final void setProduction(final boolean isProduction) {
        this.production = isProduction;
    }

    public final boolean isLocal() {
        return local;
    }

    public final void setLocal(final boolean isLocal) {
        this.local = isLocal;
    }

    public final boolean isLogging() {
        return logging;
    }

    public final void setLogging(final boolean isLogging) {
        this.logging = isLogging;
    }

    public final boolean isSecure() {
        return secure;
    }

    public final void setSecure(final boolean isSecure) {
        this.secure = isSecure;
    }

    public final GeneralConnectionRunner getLocalInstance() {
        return localInstance;
    }

    public final String getKeystorePassword() {
        return keystorePassword;
    }

    public final String getKeystorePath() {
        return keystorePath;
    }
}
