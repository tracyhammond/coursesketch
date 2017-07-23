package connection;

import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.ServerInfo;

/**
 * Runs and sets up the server.
 *
 * @author gigemjt
 */
public class AnswerCheckerRunner extends GeneralConnectionRunner {
    /**
     * Port number.
     */
    private static final int PORT = 8884;

    /**
     * Parses the arguments from the server. This only expects a single argument
     * which is if it is local.
     *
     * @param arguments
     *         the arguments from the server are then parsed.
     */
    AnswerCheckerRunner(final String... arguments) {
        super(arguments);
        super.setPort(PORT);
    }

    /**
     * The main method that can be used to run a server.
     *
     * @param args
     *         Input arguments that are running the server.
     */
    public static void main(final String... args) {
        final AnswerCheckerRunner run = new AnswerCheckerRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     * Does nothing.
     */
    @Override
    public void executeLocalEnvironment() {
        // Currently this server does not have any specific changes for executing a local environment.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadConfigurations() {
        super.setDatabaseName(DatabaseStringConstants.ANSWER_CHECKER_DATABASE);
    }

    /**
     * {@inheritDoc}.<br>
     *
     * @return an {@link connection.AnswerCheckerServlet}.
     */
    @Override
    public final ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new AnswerCheckerServlet(serverInfo);
    }
}
