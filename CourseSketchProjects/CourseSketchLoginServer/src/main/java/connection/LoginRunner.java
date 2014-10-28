package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
import database.DatabaseClient;

public class LoginRunner extends GeneralConnectionRunner {
    public static final int DEFAULT_PORT = 8886;

    public static void main(final String[] args) {
<<<<<<< HEAD
        LoginRunner run = new LoginRunner(args);
        try {
            run.runAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
=======
        final LoginRunner run = new LoginRunner(args);
        run.start();
>>>>>>> origin/master
    }

    public LoginRunner(final String[] args) {
        super(args);
        super.port = DEFAULT_PORT;
    }

    /**
     * Makes the databases run locally.
     */
    @Override
<<<<<<< HEAD
    public final void executeLocalEnviroment() {
        System.out.println("Setting the database to connect locally");
        new DatabaseClient(false); // makes the database point locally
=======
    public final void executeLocalEnvironment() {
        System.out.println("Setting the login database to connect locally");
        new DatabaseClient(false, null); // makes the database point locally
>>>>>>> origin/master
    }

    @Override
<<<<<<< HEAD
    public final GeneralConnectionServlet getServlet(final long time,
            final boolean secure, final boolean local) {
=======
    public final ServerWebSocketInitializer createSocketInitializer(final long time, final boolean secure, final boolean local) {
>>>>>>> origin/master
        return new LoginServlet(time, secure, local);
    }
}
