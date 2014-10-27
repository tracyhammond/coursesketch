package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;

public class AnswerCheckerRunner extends GeneralConnectionRunner {
    private static final int PORT = 8884;
    public static void main(final String[] args) {
        final AnswerCheckerRunner run = new AnswerCheckerRunner(args);
        try {
            run.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AnswerCheckerRunner(final String[] args) {
        super(args);
        super.port = PORT;
    }

    /**
     * Creates the local AnswerCheckers.
     */
    @Override
    public void executeLocalEnvironment() {
    }

    @Override
    public final ServerWebSocketInitializer createSocketInitializer(final long time, final boolean secure,
            final boolean local) {
        return new AnswerCheckerServlet(time, secure, local);
    }
}
