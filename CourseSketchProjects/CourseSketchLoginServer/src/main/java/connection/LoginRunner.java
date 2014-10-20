package connection;

import multiconnection.GeneralConnectionRunner;
import multiconnection.GeneralConnectionServlet;
import database.DatabaseClient;

public class LoginRunner extends GeneralConnectionRunner {
    public static final int DEFAULT_PORT = 8886;

    public static void main(final String[] args) {
        LoginRunner run = new LoginRunner(args);
        try {
            run.runAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LoginRunner(final String[] args) {
        super(args);
        super.port = DEFAULT_PORT;
    }

    /**
     * Makes the databases run locally.
     */
    @Override
    public final void executeLocalEnviroment() {
        System.out.println("Setting the database to connect locally");
        new DatabaseClient(false); // makes the database point locally
    }

    @Override
    public final GeneralConnectionServlet getServlet(final long time,
            final boolean secure, final boolean local) {
        return new LoginServlet(time, secure, local);
    }
}
