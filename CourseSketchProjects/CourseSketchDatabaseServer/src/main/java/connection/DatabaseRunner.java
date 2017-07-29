package connection;

import coursesketch.server.rpc.GeneralConnectionRunner;
import coursesketch.server.rpc.ServerWebSocketInitializer;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.user.UserClient;

/**
 * Starts the database server.
 *
 * @author gigemjt
 */
public final class DatabaseRunner extends GeneralConnectionRunner {

    /**
     * The port on which the database server lies.
     */
    private static final int DATABASE_PORT = 8885;

    /**
     * The actual main method that starts the database server.
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
     * Initializes a local instance of the database.
     */
    @Override
    public void executeLocalEnvironment() {
        new UserClient(false, null); // makes the database point locally
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
