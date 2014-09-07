package proxyServer;

import jettyMultiConnection.GeneralConnectionRunner;
import jettyMultiConnection.GeneralConnectionServlet;

public class ProxyRunner extends GeneralConnectionRunner {
	public static void main(String args[]) {
		ProxyRunner run = new ProxyRunner(args);
		try {
			run.runAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public final void executeRemoveEnviroment() {
		secure = false;
		setKeystorePassword("Challeng3");
		setKeystorePath("srl01_tamu_edu.jks");
	}

	public ProxyRunner(String args[]) {
		super(args);
		super.port = 8888;
		super.timeoutTime = 30 * 60 * 1000; // 30 minutes * 60 seconds * 1000 milliseconds
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new ProxyServlet(time, secure, local);
	}
}
