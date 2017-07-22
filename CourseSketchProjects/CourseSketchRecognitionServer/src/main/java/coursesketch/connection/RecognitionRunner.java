package coursesketch.connection;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.GeneralConnectionRunner;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.database.util.DatabaseStringConstants;

/**
 * A subclass of the runner and sets up some special information for running the
 * environment.
 */
public final class RecognitionRunner extends GeneralConnectionRunner {

    /** 30 minutes * 60 seconds * 1000 milliseconds. */
    private static final long TIMEOUT_TIME = 30 * 60 * 1000;

    /** port of the proxy server. */
    private static final int RECOGNITION = 8893;

    /**
     * @param args
     *            arguments from the command line.
     */
    public static void main(final String... args) {
        final RecognitionRunner run = new RecognitionRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadConfigurations() {
        super.setDatabaseName(DatabaseStringConstants.RECOGNITION_DATABASE);
    }

    /**
     * sets some SSL information. FUTURE: this should be read from a file
     * instead of listed in code.
     */
    @Override
    public void executeRemoteEnvironment() {
        setCertificatePath("Challeng3");
        setKeystorePath("srl01_tamu_edu.jks");
    }

    /**
     * Creates a new proxy runner.
     *
     * @param args
     *            arguments from the command line.
     */
    public RecognitionRunner(final String... args) {
        super(args);
        super.setPort(RECOGNITION);
        super.setTimeoutTime(TIMEOUT_TIME);
    }

    /**
     * {@inheritDoc}
     * @return {@link RecognitionServiceInitializer}
     */
    @Override
    public ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new RecognitionServiceInitializer(serverInfo);
    }
}
