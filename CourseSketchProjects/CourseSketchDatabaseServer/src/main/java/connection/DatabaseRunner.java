package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;

/**
 * Starts the database server.
 *
 * @author gigemjt
 */
public class DatabaseRunner extends GeneralConnectionRunner {

    /**
     * The port on which the database server lies.
     */
    private static final int DATABASE_PORT = 8885;

    /**
     * The actual main method that starts the database server.
     *
     * @param args Arguments passed from the command line.
     */
    public static void main(final String[] args) {
        final DatabaseRunner run = new DatabaseRunner(args);
        run.start();
    }

    /**
     * @param args Arguments passed from the command line.
     */
    public DatabaseRunner(final String[] args) {
        super(args);
        super.setPort(DATABASE_PORT);
    }

    /**
     * Initializes a local instance of the database.
     */
    @Override
    public final void executeLocalEnvironment() {
        new MongoInstitution(false, null); // makes the database point locally
        new UserClient(false, null); // makes the database point locally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketInitializer createSocketInitializer(final long time, final boolean secure, final boolean local) {
        return new DatabaseServlet(time, secure, local);
    }
}
