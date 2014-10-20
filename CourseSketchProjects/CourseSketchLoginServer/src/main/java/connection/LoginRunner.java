package connection;

import coursesketch.jetty.multiconnection.GeneralConnectionRunner;
import coursesketch.jetty.multiconnection.ServerWebSocketInitializer;
import database.DatabaseClient;

/**
 * Starts the login server.
 *
 * @author gigemjt
 */
public class LoginRunner extends GeneralConnectionRunner {

    /**
     * The port on which the login server lies.
     */
    public static final int LOGIN_PORT = 8886;

    /**
     * The actual main method that starts the login server.
     *
     * @param args
     *            Arguments passed from the command line.
     */
    public static void main(final String[] args) {
        final LoginRunner run = new LoginRunner(args);
        run.start();
    }

    /**
     * @param args
     *            Arguments passed from the command line.
     */
    public LoginRunner(final String[] args) {
        super(args);
        super.setPort(LOGIN_PORT);
    }

    /**
     * Makes the login database run locally.
     */
    @Override
    public final void executeLocalEnviroment() {
        System.out.println("Setting the login database to connect locally");
        new DatabaseClient(false, null); // makes the database point locally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ServerWebSocketInitializer getSocketInitializer(final long time, final boolean secure, final boolean local) {
        return new LoginServlet(time, secure, local);
    }
}
