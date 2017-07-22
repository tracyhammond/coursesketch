package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.user.UserClient;

/**
 * Starts the coursesketch.util.util server.
 *
 * @author gigemjt
 */
public final class DatabaseRunner extends GeneralConnectionRunner {

    /**
     * The port on which the coursesketch.util.util server lies.
     */
    private static final int DATABASE_PORT = 8885;

    /**
     * The actual main method that starts the coursesketch.util.util server.
     *
     * @param args Arguments passed from the command line.
     */
    public static void main(final String... args) {
        final DatabaseRunner run = new DatabaseRunner(args);
        run.start();
    }

    /**
     * @param args Arguments passed from the command line.
     */
    public DatabaseRunner(final String... args) {
        super(args);
        super.setPort(DATABASE_PORT);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected void loadConfigurations() {
        super.setDatabaseName(DatabaseStringConstants.DATABASE);
    }

    /**
     * Initializes a local instance of the coursesketch.util.util.
     */
    @Override
    public void executeLocalEnvironment() {
        new UserClient(false, null); // makes the coursesketch.util.util point locally
    }

    /**
     * {@inheritDoc}
     *
     * @return new instance of {@link DatabaseServlet}.
     */
    @Override
    public ServerWebSocketInitializer createSocketInitializer(final ServerInfo serverInfo) {
        return new DatabaseServlet(serverInfo);
    }
}
