package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseClient;

/**
 * The class that contains the method for starting the server.
 */
public final class SubmissionRunner extends GeneralConnectionRunner {
    /**
     * Port number for this server.
     */
    private static final int PORT = 8883;

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *            the arguments from the server are then parsed.
     */
    public SubmissionRunner(final String... arguments) {
        super(arguments);
        super.setPort(PORT);
    }

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     */
    public static void main(final String... args) {
        final SubmissionRunner run = new SubmissionRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     * Creates the local database client.
     */
    @SuppressWarnings("unused")
    @Override
    public void executeLocalEnvironment() {
        new DatabaseClient(false);
    }

    /**
     * {@inheritDoc}
     * @param serverInfo
     */
    @Override
    public ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new SubmissionServlet(serverInfo);
    }
}
