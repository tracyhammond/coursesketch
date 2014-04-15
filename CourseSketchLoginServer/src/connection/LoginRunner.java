package connection;

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

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new LoginServlet(time, secure, local);
	}
}
