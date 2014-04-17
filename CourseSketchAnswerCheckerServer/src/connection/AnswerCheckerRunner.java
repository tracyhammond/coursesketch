package connection;

import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class AnswerCheckerRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		AnswerCheckerRunner run = new AnswerCheckerRunner(args);
		try {
			run.runAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AnswerCheckerRunner(String args[]) {
		super(args);
		super.port = 8884;
	}

	/**
	 * Creates the local AnswerCheckers.
	 */
	@Override
	public void executeLocalEnviroment() {
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new AnswerCheckerServlet(time, secure, local);
	}
}
