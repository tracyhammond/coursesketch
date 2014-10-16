package serverfront;

import multiconnection.GeneralConnectionRunner;
import multiconnection.GeneralConnectionServlet;

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
		setKeystorePassword("Challeng3");
		setKeystorePath("srl01_tamu_edu.jks");
	}

	public ProxyRunner(String args[]) {
		super(args);
		super.setPort(8888);
		super.setTimeoutTime(30 * 60 * 1000); // 30 minutes * 60 seconds * 1000 milliseconds
	}

	@Override
	public final GeneralConnectionServlet getServlet(long time, boolean secure, boolean local) {
		return new ProxyServlet(time, secure, local);
	}
}
