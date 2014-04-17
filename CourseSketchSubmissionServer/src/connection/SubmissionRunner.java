package connection;

import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class SubmissionRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		SubmissionRunner run = new SubmissionRunner(args);
		try {
			run.runAll();
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
	@Override
	public void executeLocalEnviroment() {
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new SubmissionServlet(time, secure, local);
	}
}
