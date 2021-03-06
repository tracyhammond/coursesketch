package coursesketch.serverfront;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.GeneralConnectionRunner;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.database.util.DatabaseStringConstants;

/**
 * A subclass of the runner which sets up some special information for running the
 * environment.
 */
public final class AuthenticationServerRunner extends GeneralConnectionRunner {

    /** 30 minutes * 60 seconds * 1000 milliseconds. */
    private static final long TIMEOUT_TIME = 30 * 60 * 1000;

    /** port of the proxy server. */
    private static final int PROXY_PORT = 8890;

    /**
     * @param args
     *            Arguments from the command line.
     */
    public static void main(final String... args) {
        final AuthenticationServerRunner run = new AuthenticationServerRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadConfigurations() {
        super.setDatabaseName(DatabaseStringConstants.AUTH_DATABASE);
    }

    /**
     * Sets some SSL information. FUTURE: this should be read from a file instead of listed in code.
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
     *            Arguments from the command line.
     */
    public AuthenticationServerRunner(final String... args) {
        super(args);
        super.setPort(PROXY_PORT);
        super.setTimeoutTime(TIMEOUT_TIME);
    }

    /**
     * {@inheritDoc}
     * @return {@link AuthenticationServiceInitializer}
     */
    @Override
    public ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new AuthenticationServiceInitializer(serverInfo);
    }
}
