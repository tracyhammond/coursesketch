package coursesketch.server.interfaces;

import com.google.common.collect.Lists;
import com.mongodb.ServerAddress;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LoggingConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.TooManyFields" })
public abstract class AbstractGeneralConnectionRunner {

    /**
     * Declaration/Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractGeneralConnectionRunner.class);

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

    // these should be changed based on the properties
    /**
     * Arguments that come in from the command line.
     */
    private final String[] args;

    /**
     * The object that parses command line input and argument input.
     */
    private final InputParser inputParser;

    /**
     * The port of the server.
     */
    private int port = DEFAULT_PORT;

    /**
     * The hostname of the server.
     */
    private String hostName;

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
    private String certificatePath = null;

    /**
     * The location the keystore is stored in.
     */
    private String keystorePath = null;

    /**
     * The servlet that is connected to the server.  (it is typically binded to a certain URL)
     */
    private ISocketInitializer socketInitializerInstance;

    /**
     * True if the server is currently accepting input.
     */
    private boolean inputRunning;

    /**
     * Only true during times where setup happens and certain values can be set.
     */
    private boolean setup = false;

    /**
     * A list of addresses where the database can be found at.
     */
    private List<ServerAddress> databaseUrl;

    /**
     * The name of the database.
     */
    private String databaseName;

    /**
     * The object that holds command line input.
     */
    private BufferedReader input;

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *            the arguments from the server are then parsed.
     */
    protected AbstractGeneralConnectionRunner(final String... arguments) {
        inputParser = new InputParser();
        addArgumentsLocal(inputParser);
        this.args = Arrays.copyOf(arguments, arguments.length);

        production = false;
        secure = false;
    }

    /**
     * Runs the entire startup process including input.
     * The order that the subclass methods are called is:
     * <ol>
     *     <li>{@link #loadConfigurations()}</li>
     *     <li>if the server is running locally {@link #executeLocalEnvironment()} is called otherwise {@link #executeRemoteEnvironment()}</li>
     *     <li>{@link #createServer()}</li>
     *     <li>if the server is running securely then {@link #configureSSL(String, String)}</li>
     *     <li>{@link #createSocketInitializer(ServerInfo)}</li>
     *     <li>{@link #addConnections()}</li>
     *     <li>{@link #startServer()}</li>
     *     <li>{@link #startInput()}</li>
     * </ol>
     */
    public final void start() {
        setup = true;
        loadConfigurations();
        if (local) {
            privateLocalEnvironment();
            executeLocalEnvironment();
        } else {
            privateRemoteEnvironment();
            executeRemoteEnvironment();
        }
        setup = false;
        createServer();

        if (secure) {
            configureSSL(keystorePath, certificatePath);
        }

        final ServerInfo serverInfo = new ServerInfo(this.getHostName(), this.getPort(), getTimeoutTime(), secure, isLocal(),
                getDatabaseName(), getDatabaseUrl());

        socketInitializerInstance = createSocketInitializer(serverInfo);

        addConnections();

        startServer();

        startInput();
    }

    /**
     * Called to load the configuration data.
     *
     * It can be overwritten to load specific data for each server.
     */
    protected abstract void loadConfigurations();

    /**
     * Called to setup the system when it is being run on a local computer with a local host.
     */
    protected abstract void executeLocalEnvironment();

    /**
     * Called to setup the system if it is being run on a remote host.
     */
    protected abstract void executeRemoteEnvironment();

    /**
     * Called to create an actual server.
     */
    protected abstract void createServer();

    /**
     * Configures the SSL for the server.
     *
     * @param iKeystorePath
     *         The location of the keystore.
     * @param iCertificatePath
     *         The password for the keystore.
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
     * Sets up some global variables for the local configuration.
     */
    private void privateLocalEnvironment() {
        hostName = "localhost";
    }

    /**
     * Sets up some global variables for the remote configuration.
     */
    private void privateRemoteEnvironment() {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Error grabbing host name for remote environment", e);
        }
    }

    /**
     * A command for exiting the server.
     * @param sysin Keyboard input.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void exitCommand(final BufferedReader sysin) throws IOException {
        LOG.info("Are you sure you want to exit? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
            acceptInput = false;
            LOG.info("Stopped accepting input");
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
        LOG.info("Are you sure you want to restart? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
            LOG.info("sleeping for 1s");
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
        LOG.info("Are you sure you want to stop? [y/n]");
        if (StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
            this.stop();
        }
    }

    /**
     * A command for starting the server.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void startCommand() throws IOException {
        if (!this.serverStarted()) {
            this.start();
        } else {
            LOG.info("you can not start the because it is already running.");
        }
    }

    /**
     * Toggles off and on logging.
     *
     * @param sysin keyboard input.
     * @param equals If the user is wanting to turn on logging.
     * @throws java.io.IOException Thrown if there are problems getting the keyboard input.
     */
    private void toggleLoggingCommand(final BufferedReader sysin, boolean equals) throws IOException {
        if (logging && !equals) {
            LOG.info("Are you sure you want to turn loggin off? [y/n]");
            if (!StringUtils.defaultString(sysin.readLine()).equalsIgnoreCase("y")) {
                LOG.info("action canceled");
                return;
            }
        }
        String isLoggingStr = "Off";
        if (logging) {
            isLoggingStr = "On";
        }
        LOG.info("Turning loggin {}", isLoggingStr);
        logging = !logging;
    }

    /**
     * Starts the system that accepts command line input.
     *
     * The input is started in a separate thread and this method will return immediately regardless of success.
     * If input is already running then this method does not start a new thread for input but instead returns.
     */
    private void startInput() {
        inputParser.clear();
        if (inputRunning || !isAcceptingCommandInput()) {
            return;
        }
        addCommandsLocal(inputParser);
        final Thread inputThread = new Thread(() -> {
            inputRunning = true;
            final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            input = sysin;
            while (acceptInput) {
                try {
                    final String command = sysin.readLine();
                    LOG.debug("Input command is {}", command);
                    inputParser.parse(new String[] {"-" + command});
                } catch (IOException | ParseException e) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                    break;
                }
            }
            inputRunning = false;
        });
        inputThread.start();
    }

    /**
     * Stops the server.
     *
     * Input is not stopped by the method.
     */
    protected abstract void stop();

    /**
     * Reestablishes connections to other servers.
     *
     * Input is not stopped by the method.
     */
    protected final void reconnect() {
        getSocketInitailizerInstance().reconnect();
    }

    /**
     * Creates and returns a new instance of a {@link ISocketInitializer}.
     *
     * Override this method if you want to return a subclass of GeneralConnectionServlet.
     *
     * @param serverInfo {@link ServerInfo} Contains all of the information about the server.
     *
     * @return a new instance of an {@link ISocketInitializer}.
     **/
    protected abstract ISocketInitializer createSocketInitializer(ServerInfo serverInfo);

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
     * False if the server has not been started (basically {@link #startServer()} has not been called yet).
     *
     * @return False if the server has not been started.
     */
    protected abstract boolean serverStarted();

    /**
     * If in a remote server environment, gets host name by DNS resolving.
     *
     * @return The host name of the server.
     */
    public final String getHostName() {
        return hostName;
    }

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
    private long getTimeoutTime() {
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
    private boolean isAcceptingCommandInput() {
        return acceptInput;
    }

    /**
     * @param acceptInputToSet True if the command line will accept input.  False otherwise.
     */
    private void setAcceptingCommandInput(final boolean acceptInputToSet) {
        this.acceptInput = acceptInputToSet;
    }

    /**
     * @return True if the server is running as a production environment.
     */
    protected final boolean isProduction() {
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

    /**
     * Returns a list of valid {@link ServerAddress} the database can connect to.
     *
     * @return An unmodifiable list that represents the addresses the database can connect to.
     */
    private List<ServerAddress> getDatabaseUrl() {
        if (databaseUrl == null) {
            return Collections.unmodifiableList(Lists.newArrayList(new ServerAddress()));
        }
        return Collections.unmodifiableList(databaseUrl);
    }

    /**
     * Sets the list of addresses the database can connect to.
     *
     * @param databaseUrl The list of addresses the database can connect to.
     */
    private void setDatabaseUrl(final List<ServerAddress> databaseUrl) {
        if (!setup) {
            throw new IllegalStateException("Can only set this variable during setup");
        }
        this.databaseUrl = databaseUrl;
    }

    /**
     * Gets the database name.
     *
     * @return The database name.
     */
    private String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the database name.
     *
     * @param databaseName The name of the database.
     */
    protected final void setDatabaseName(final String databaseName) {
        if (!setup) {
            throw new IllegalStateException("Can only set this variable during setup");
        }
        this.databaseName = databaseName;
    }

    /**
     * Adds local arguments.
     *
     * @param argumentInputParser The parser that gets the local arguments
     */
    private void addArgumentsLocal(InputParser argumentInputParser) {
        argumentInputParser.addParsingOption(argumentInputParser
                        .createOption("isLocal", true,
                                "Used for running the server as a local host or if it should connect to given IP addresses\n"
                                + "True if it is local false otherwise"),
                argumentValue -> local = argumentValue.equalsIgnoreCase("true"));

        argumentInputParser.addParsingOption(argumentInputParser
                        .createOption("commandLine", true,
                                "True if the server should accept command line input false if it should not"),
                argumentValue -> {
                    if (argumentValue.equals("true")) {
                        setAcceptingCommandInput(true);
                    } else {
                        setAcceptingCommandInput(false);
                    }
                });

        argumentInputParser.addParsingOption(argumentInputParser
                        .createOption("databaseUrls", true,
                                "The address the database server is at as a list of comma separated Urls"),
                argumentValue -> {
                    final List<ServerAddress> mongoLocation = new ArrayList<>();
                    for (String serverAddress : argumentValue.split(",")) {
                        mongoLocation.add(new ServerAddress(serverAddress));
                    }
                    setDatabaseUrl(mongoLocation);
                });
        addArguments(argumentInputParser);
    }

    /**
     * Adds commands for parsing arguments.
     *
     * @param argumentInputParser The parser for arguments.
     */
    protected abstract void addArguments(InputParser argumentInputParser);

    /**
     * Creates commands that can be used to perform certain functionality.
     *
     * @param commandLineInputParser The parser that the local commands are being added to.
     */
    private void addCommandsLocal(InputParser commandLineInputParser) {
        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("exit", false, "kills the server"),
                argumentValue -> exitCommand(getInput()));

        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("restart", false, "restarts the server"),
                argumentValue -> restartCommand(getInput()));

        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("reconnect", false, "restarts the server connections"),
                argumentValue -> reconnect());

        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("start", false, "starts the server connections"),
                argumentValue -> startCommand());

        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("stop", false, "stops the server connections"),
                argumentValue -> stopCommand(getInput()));

        commandLineInputParser.addParsingOption(commandLineInputParser.createOption("setLogging", true,
                "turns on or off logging use \"on\" or \"off\""),
                argumentValue -> toggleLoggingCommand(getInput(), "on".equals(argumentValue)));
        addCommands(commandLineInputParser);
    }

    /**
     * Parses extra commands that are taken in through the input line.
     *
     * @param commandLineInputParser The parser that the commands are being added to.
     */
    protected abstract void addCommands(InputParser commandLineInputParser);

    /**
     * @return The reader used for command line input.
     */
    public BufferedReader getInput() {
        return input;
    }
}
