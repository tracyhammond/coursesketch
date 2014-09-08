package connection;

import database.institution.Institution;
import database.user.UserClient;
import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class DatabaseRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		DatabaseRunner run = new DatabaseRunner(args);
		try {
			run.runAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DatabaseRunner(String args[]) {
		super(args);
		super.port = 8885;
	}

	/**
	 * Creates the local databases.
	 */
	@Override
	public void executeLocalEnviroment() {
		new Institution(false); // makes the database point locally
		new UserClient(false); // makes the database point locally
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new DatabaseServlet(time, secure, local);
	}
}
