package jettyMultiConnection;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded-examples.html#d0e18352
 * 
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import jettyMultiConnection.CourseSketchServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class MultiConnectionRunner {

	public static void main(String[] args) throws Exception {
		MultiConnectionRunner runner = new MultiConnectionRunner();
		try {
			runner.loadConfigurations();
			runner.createServer();
			runner.addServletHandlers();
			runner.startInput();
			runner.startServer();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final private MultiConnectionRunner localInstance = this;
	private Server server;
	private CourseSketchServlet servletInstance;

	// these should be changed based on 
	private int port = 8888;
	private long timeoutTime;
	private boolean acceptInput = true;
	private boolean production;

	public void loadConfigurations() {
		
	}

	/**
	 * Sets up a Jetty embedded server. Uses HTTPS over port 12102 and a key certificate.
	 * @throws Exception 
	 */
	public void createServer() throws Exception {
		server = new Server(port);
	}

	public void addServletHandlers() {
		StatisticsHandler stats = new StatisticsHandler();
		/*
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/coursesketch");
		servletHandler.addServlet(new ServletHolder(new CourseSketchServlet()),"/");
		*/
		ServletHandler servletHandler = new ServletHandler();

		servletInstance = getServlet();

		servletHandler.addServletWithMapping(new ServletHolder(servletInstance),"/*");
		stats.setHandler(servletHandler);

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[]{stats});

		server.setHandler(handlers);
	}

	public void startServer() {
		
		Thread d = new Thread() {
			public void run() {
				try {
				server.start();
				server.join();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		d.start();
	}

	/**
	 * Returns a new instance of a {@link CourseSketchServlet}.
	 *
	 * Override this method if you want to return a subclass of CourseSketchServlet
	 */
	public CourseSketchServlet getServlet() {
		boolean secure = false;
		if (!secure) {
			System.err.println("Running an insecure server");
		}
		return new CourseSketchServlet(timeoutTime, secure);
	}

	/**
	 * Handles commands that can be used to perform certain functionality.
	 *
	 * This method can and in some cases should be overwritten.
	 * We <b>strongly</b> suggest that you call super first then check to see if it is true and then call your overwritten method.
	 * @param command The command that is parsed to provide functionality.
	 * @param sysin Used if additional input is needed for the command.
	 * @return true if the command is an accepted command and is used by the server
	 * @throws Exception 
	 */
	public boolean parseCommand(String command, BufferedReader sysin) throws Exception {
		if (command.equals( "exit" )) {
			System.out.println("Are you sure you want to exit? [y/n]");
			if (sysin.readLine().equalsIgnoreCase("y")) {
				this.stop();
				acceptInput = false;
				System.out.println("Stopped accepting input");
			}
			return true;
		} else if (command.equals("restart")) {
			System.out.println("Are you sure you want to restart? [y/n]");
			if (sysin.readLine().equalsIgnoreCase("y")) {
				this.stop();
				System.out.println("sleeping for 1s");
				Thread.sleep(1000);
				this.loadConfigurations();
				this.createServer();
				this.addServletHandlers();
				this.startServer();
			}
		} else if (command.equals("reconnect")) {
			servletInstance.reconnect();
			return true;
		} else if (command.equals("stop")) {
			System.out.println("Are you sure you want to stop? [y/n]");
			if (sysin.readLine().equalsIgnoreCase("y")) {
				this.stop();
			}
			return true;
		}  else if (command.equals("start")) {
			if (this.server == null || !this.server.isRunning()) {
				this.loadConfigurations();
				this.createServer();
				this.addServletHandlers();
				this.startServer();
			} else {
				System.out.println("you can not start the server because it is already running.");
			}
			return true;
		}
		return false;
	}

	public void startInput() {
		Thread d = new Thread() {
			public void run() {
				try {
					BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
					while ( acceptInput ) {
						String in = sysin.readLine();
						try {
							localInstance.parseCommand(in, sysin);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		d.start();
	}

	public void stop() {
		try {
			server.stop();
			servletInstance.stop();
			server = null;
			servletInstance = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
