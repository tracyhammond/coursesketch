package connection;

import database.DatabaseClient;
import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class LoginRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		LoginRunner run = new LoginRunner();
		try {
			run.runAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LoginRunner() {
		super.port = 8886;
	}

	/**
	 * Makes the databases run locally
	 */
	@Override
	public void executeLocalEnviroment() {
		System.out.println("Setting the database to connect locally");
		new DatabaseClient(false); // makes the database point locally
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new LoginServlet(time, secure, local);
	}
}
