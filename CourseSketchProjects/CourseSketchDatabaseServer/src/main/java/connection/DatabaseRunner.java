package connection;

import multiconnection.GeneralConnectionRunner;
import multiconnection.GeneralConnectionServlet;
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
        try {
            run.runAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args Arguemtns passed from the command line.
     */
    public DatabaseRunner(final String[] args) {
        super(args);
        super.setPort(DATABASE_PORT);
    }

    @Override
    public final void executeLocalEnviroment() {
        new MongoInstitution(false, null); // makes the database point locally
        new UserClient(false); // makes the database point locally
    }

    @Override
    public final GeneralConnectionServlet getServlet(final long time, final boolean secure, final boolean local) {
        return new DatabaseServlet(time, secure, local);
    }
}
