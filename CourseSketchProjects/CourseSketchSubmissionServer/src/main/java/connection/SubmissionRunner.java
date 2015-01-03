package connection;

import coursesketch.server.base.GeneralConnectionRunner;
import coursesketch.server.base.ServerWebSocketInitializer;
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
		super.setPort(8883);
	}

	/**
	 * Creates the local Submissions.
	 */
	@SuppressWarnings("unused")
	@Override
	public void executeLocalEnvironment() {
		new DatabaseClient(true);
	}

	@Override
	public final ServerWebSocketInitializer createSocketInitializer(long time, boolean secure, boolean local) {
		return new SubmissionServlet(time, secure, local);
	}
}
