package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
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
    public SubmissionRunner(final String[] arguments) {
        super(arguments);
        super.setPort(PORT);
    }

    /**
     * The main method that can be used to run a server.
     * @param args Input arguments that are running the server.
     */
    public static void main(final String[] args) {
        final SubmissionRunner run = new SubmissionRunner(args);
        try {
            run.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the local Submissions.
     */
    @SuppressWarnings("unused")
    @Override
    public void executeLocalEnvironment() {
        new DatabaseClient(true);
    }

    @Override
    public ServerWebSocketInitializer createSocketInitializer(final long time, final boolean secure, final boolean local) {
        return new SubmissionServlet(time, secure, local);
    }
}
