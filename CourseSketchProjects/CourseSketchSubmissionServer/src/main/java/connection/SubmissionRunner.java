package connection;

import coursesketch.jetty.multiconnection.GeneralConnectionRunner;
import coursesketch.jetty.multiconnection.GeneralConnectionServlet;
import database.DatabaseClient;

public class SubmissionRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		SubmissionRunner run = new SubmissionRunner(args);
		try {
			run.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SubmissionRunner(String args[]) {
		super(args);
		super.port = 8883;
	}

	/**
	 * Creates the local Submissions.
	 */
	@SuppressWarnings("unused")
	@Override
	public void executeLocalEnviroment() {
		new DatabaseClient(true);
	}

	@Override
	public final GeneralConnectionServlet getSocketInitializer(long time, boolean secure, boolean local) {
		return new SubmissionServlet(time, secure, local);
	}
}
