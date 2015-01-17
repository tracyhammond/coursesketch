package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;

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
    public AnswerCheckerRunner(final String[] arguments) {
        super(arguments);
        super.setPort(PORT);
    }

    /**
     * The main method that can be used to run a server.
     *
     * @param args
     *         Input arguments that are running the server.
     */
    public static void main(final String[] args) {
        final AnswerCheckerRunner run = new AnswerCheckerRunner(args);
        run.start();
    }

    /**
     * {@inheritDoc}
     * Does nothing.
     */
    @Override
    public void executeLocalEnvironment() {
    }

    /**
     * {@inheritDoc}.<br>
     *
     * @return an {@link connection.AnswerCheckerServlet}.
     */
    @Override
    public final ServerWebSocketInitializer createSocketInitializer(final long time, final boolean secure,
            final boolean local) {
        return new AnswerCheckerServlet(time, secure, local);
    }
}
