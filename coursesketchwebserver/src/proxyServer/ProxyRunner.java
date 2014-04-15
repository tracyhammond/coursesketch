package proxyServer;

import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class ProxyRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		ProxyRunner run = new ProxyRunner();
		try {
			run.runAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ProxyRunner() {
		super.port = 8888;
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new ProxyServlet(time, secure, local);
	}
}
