package coursesketch.server.interfaces;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Sets up the server and manages calling the methods needed to start the server.
 * Right now it also handles keyboard input and loading of the configurations (these will probably be moved to separate classes later on)
 *
 * To start up a server all you need to call is {@link coursesketch.server.interfaces.AbstractGeneralConnectionRunner#start()}.
 *
 * @author gigemjt
 * @since 10/19/14
 * @version 1
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AbstractGeneralConnectionRunner {

    /**
     * Max buffer size for the output in bytes.
     */
    protected static final int DEFAULT_OUTPUT_BUFFER_SIZE = 32786;

    /**
     * Max buffer size for the header in bytes.
     */
    protected static final int DEFAULT_REQUEST_HEADER_SIZE = 8192;

    /**
     * Max buffer size for the response header in bytes.
     */
    protected static final int DEFAULT_RESPONSE_HEADER_SIZE = 8192;

    /**
     * The default port is 8888.
     */
    private static final int DEFAULT_PORT = 8888;

    /**
     * A local instance is stored here.
     */
    private final AbstractGeneralConnectionRunner localInstance = this;

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
     * The url that the server runs on.
     */
    private String host;

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
    private String keyManagerPassword;

    /**
     * The location the keystore is stored in.
     */
    private String keystorePath = null;

    /**
     * The location the truststore is stored in.
     */
    private String truststorePath = "";

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     * The servlet that is connected to the server.  (it is typically binded to a certain URL)
     */
    private ISocketInitializer socketInitializerInstance;

    /**
     * True if the server is currently accepting input.
     */
    private boolean inputRunning;
    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *            the arguments from the server are then parsed.
     */
    protected AbstractGeneralConnectionRunner(final String... arguments) {
        this.args = Arrays.copyOf(arguments, arguments.length);
        if (arguments.length >= 1 && arguments[0].equals("local")) {
            System.out.println("Running local code!");
            host = "localhost";
            keystorePassword = "sketchrec";
            keyManagerPassword = "sketchrec";
            keystorePath = "keystore.jks";
            truststorePath = "truststore.jks";
            secure = true;
            local = true;
        } else {
            local = false;
            secure = false;
        }
        production = false;
    }

    /**
     * Runs the entire startup process including input.
     * <ol>
     *     <li>{@link #loadConfigurations()}</li>
     *     <li>if the server is running locally {@link #executeLocalEnvironment()} is called otherwise {@link #executeRemoveEnvironment()}</li>
     *     <li>{@link #createServer()}</li>
     *     <li>if the server is running securely then {@link #configureSSL(String, String)}</li>
     *     <li>{@link #createSocketInitializer(long, boolean, boolean)}</li>
     *     <li>{@link #addConnections()}</li>
     *     <li>{@link #startServer()}</li>
     *     <li>{@link #startInput()}</li>
     * </ol>
     */
    protected final void start() {
        loadConfigurations();
        if (local) {
            executeLocalEnvironment();
        } else {
            executeRemoveEnvironment();
        }
        createServer();

        if (secure) {
            configureSSL(keystorePath, certificatePath);
        }
        socketInitializerInstance = createSocketInitializer(getTimeoutTime(), secure, isLocal());

        addConnections();

        startServer();

        startInput();
    }

    /**
     * Called to load the configuration data it can be overwritten to load specific data for each server.
     */
    protected abstract void loadConfigurations();

    /**
     * Called to setup the system if it is being run on a local computer with a local host.
     */
    protected abstract void executeLocalEnvironment();

    /**
     * Called to setup the system for if it is being run to connect to remote compters.
     */
    protected abstract void executeRemoveEnvironment();

    /**
     * Called to create an actual server.
     */
    protected abstract void createServer();

    /**
     * Configures the SSL for the server.
     * @param iCertificatePath the password for the keystore.
     * @param iKeystorePath the location of the keystore.
     */
    protected abstract void configureSSL(String iKeystorePath, String iCertificatePath);

    /**
     * Called to add connections to the server.
     */
    protected abstract void addConnections();

    /**
     * Starts the server in a separate thread.
     * A server can only be run once.
     *
     * This not used to actually create the server or add connections to see the method that performs that function
     * @see #start()
     */
    protected abstract void startServer();

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
     * @throws java.io.IOException an I/O error
     * @throws InterruptedException the thread is interrupted.
     */
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
            reconnect();
            return true;
        } else if ("stop".equals(command)) {
            stopCommand(sysin);
            return true;
        } else if ("start".equals(command)) {
            startCommand();
            return true;
        } else if ("toggle logging".equals(command)) {
            toggleLoggingCommand(sysin);
            return true;
        }
        return parseUtilityCommand(command, sysin);
    }

    /**
     * A command for exiting the server.
     * @param sysin Keyboard input.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
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
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void restartCommand(final BufferedReader sysin) throws IOException, InterruptedException {
        final int waitDelay = 1000;
        System.out.println("Are you sure you want to restart? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
            System.out.println("sleeping for 1s");
            Thread.sleep(waitDelay);
            this.start();
        }
    }

    /**
     * A command for stopping the server.
     * @param sysin Keyboard input.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void stopCommand(final BufferedReader sysin) throws IOException {
        System.out.println("Are you sure you want to stop? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
        }
    }

    /**
     * A command for starting the server.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void startCommand() throws IOException {
        if (this.notServerStarted()) {
            this.start();
        } else {
            System.out.println("you can not start the because it is already running.");
        }
    }

    /**
     * Toggles off and on logging.
     * @param sysin keyboard input
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void toggleLoggingCommand(final BufferedReader sysin) throws IOException {
        if (logging) {
            System.out.println("Are you sure you want to turn loggin off? [y/n]");
            if (!StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
                System.out.println("action canceled");
                return;
            }
        }
        String isLoggingStr = "Off";
        if (logging) {
            isLoggingStr = "On";
        }
        System.out.println("Turning loggin " + isLoggingStr);
        logging = !logging;
    }

    /**
     * Starts the system that accepts command line input.
     *
     * The input is started in a separate thread and this method will return immediately regardless of success.
     * If input is already running then this method does not start a new thread for input but instead returns.
     */
    public final void startInput() {
        if (inputRunning) {
            return;
        }
        final Thread inputThread = new Thread() {
            @Override
            @SuppressWarnings("PMD.CommentRequired")
            public void run() {
                inputRunning = true;
                final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
                while (acceptInput) {
                    try {
                        final String command = sysin.readLine();
                        localInstance.parseCommand(command, sysin);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                inputRunning = false;
            }
        };
        inputThread.start();
    }

    // FUTURE: add a command manager of some sort.
    /**
     * Parses extra commands that are taken in through the input line.
     * @param command The command that is being processed.
     * @param sysin Used for additional input.
     * @return True if the message command is processed.
     * @throws java.io.IOException Thrown if there is a problem reading input.
     */
    protected abstract boolean parseUtilityCommand(String command, BufferedReader sysin) throws IOException;

    /**
     * Stops the server.
     * Input is not stopped by the method.
     */
    protected abstract void stop();

    /**
     * Stops the server.
     * Input is not stopped by the method.
     */
    protected abstract void reconnect();

    /**
     * Returns a new instance of a {@link ISocketInitializer}.
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
    protected abstract ISocketInitializer createSocketInitializer(final long timeOut, final boolean isSecure, final boolean isLocal);

    /**
     * Sets the password for the SSL keystore.
     *
     * @param pass
     *            password to set for the keystore.
     */
    protected final void setCertificatePath(final String pass) {
        if (this.keystorePath != null) {
            throw new IllegalStateException("password is already set throwing an error.");
        }
        this.certificatePath = pass;
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
     * @return true if the server has not been started (basically run most has not been called yet)
     */
    protected abstract boolean notServerStarted();

    /**
     * @return The port number that this server is connected to.
     */
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
    public final boolean isAcceptingCommandInput() {
        return acceptInput;
    }

    /**
     * @param acceptInputToSet True if the command line will accept input.  False otherwise.
     */
    public final void setAcceptingCommandInput(final boolean acceptInputToSet) {
        this.acceptInput = acceptInputToSet;
    }

    /**
     * @return True if the server is running as a production environment.
     */
    public final boolean isProduction() {
        return production;
    }

    /**
     * @return True if the server is attempting to run as a local server.  (used for testing)
     */
    public final boolean isLocal() {
        return local;
    }

    /**
     * @return True if the computer is logging.
     */
    public final boolean isLogging() {
        return logging;
    }

    /**
     * @return True if the server is accepting connections via SSL.
     */
    protected final boolean isSecure() {
        return secure;
    }

    /**
     * @return An instance of the servlet created by this runner.
     */
    protected final ISocketInitializer getSocketInitailizerInstance() {
        return socketInitializerInstance;
    }

    /**
     * @return the arguments that were passed from the command line.
     */
    protected final String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}
