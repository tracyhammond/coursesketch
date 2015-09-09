package serverfront;

import coursesketch.server.interfaces.ServerInfo;
import coursesketch.server.rpc.GeneralConnectionRunner;
import coursesketch.server.rpc.ServerWebSocketInitializer;

/**
 * A subclass of the runner and sets up some special information for running the
 * environment.
 */
public class IdentityServerRunner extends GeneralConnectionRunner {

    /** 30 minutes * 60 seconds * 1000 milliseconds. */
    private static final long TIMEOUT_TIME = 30 * 60 * 1000;

    /** port of the proxy server. */
    private static final int PROXY_PORT = 8890;

    /**
     * @param args
     *            arguments from the command line.
     */
    public static void main(final String... args) {
        final IdentityServerRunner run = new IdentityServerRunner(args);
        run.start();
    }

    /**
     * sets some SSL information. FUTURE: this should be read from a file
     * instead of listed in code.
     */
    @Override
    public final void executeRemoteEnvironment() {
        setCertificatePath("Challeng3");
        setKeystorePath("srl01_tamu_edu.jks");
    }

    /**
     * Creates a new proxy runner.
     *
     * @param args
     *            arguments from the command line.
     */
    public IdentityServerRunner(final String... args) {
        super(args);
        super.setPort(PROXY_PORT);
        super.setTimeoutTime(TIMEOUT_TIME);
    }

    /**
     * {@inheritDoc}
     * @param serverInformation {@link ServerInfo} Contains all of the information about the server.
     * @return {@link IdentityServiceInitializer}.
     */
    @Override
    public final ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInformation) {
        return new IdentityServiceInitializer(serverInformation);
    }
}
