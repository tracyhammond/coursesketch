package jettyMultiConnection;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded-examples.html#d0e18352
 * 
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import jettyMultiConnection.GeneralConnectionServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class GeneralConnectionRunner {

	public static void main(String[] args) throws Exception {
		GeneralConnectionRunner runner = new GeneralConnectionRunner();
		try {
			runner.runAll();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final private GeneralConnectionRunner localInstance = this;
	private Server server;
	private GeneralConnectionServlet servletInstance;

	// these should be changed based on 
	protected int port = 8888;
	protected long timeoutTime;
	protected boolean acceptInput = true;
	private boolean production = false;
	protected boolean local = true;
	protected boolean isLogging = false;

	/**
	 * Runs the entire startup process including input
	 * @throws Exception
	 */
	protected final void runAll() throws Exception {
		this.runMost();
		this.startInput();
	}

	/**
	 * Runs the majority of the startup proccess.
	 *
	 * Does not handle accepting Input
	 */
	protected final void runMost() throws Exception {
		this.loadConfigurations();
		if (local) {
			this.executeLocalEnviroment();
		} else {
			this.executeRemoveEnviroment();
		}
		this.createServer();
		this.addServletHandlers();
		
		this.startServer();
	}

	public void loadConfigurations() {
		
	}

	public void executeLocalEnviroment() {
		
	}

	public void executeRemoveEnviroment() {
		
	}
	
	/**
	 * Sets up a Jetty embedded server. Uses HTTPS over port 12102 and a key certificate.
	 * @throws Exception 
	 */
	public void createServer() throws Exception {
		server = new Server(port);
		System.out.println("Server has been created on port: " + port);
	}

	public void addServletHandlers() {
		StatisticsHandler stats = new StatisticsHandler();
		/*
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/coursesketch");
		servletHandler.addServlet(new ServletHolder(new GeneralConnectionServlet()),"/");
		*/
		ServletHandler servletHandler = new ServletHandler();

		System.out.println("Creating a new servlet");
		servletInstance = getServlet(timeoutTime, false, local); // TODO: change this to true!

		servletHandler.addServletWithMapping(new ServletHolder(servletInstance),"/*");
		stats.setHandler(servletHandler);

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[]{stats});

		server.setHandler(handlers);
	}

	public void startServer() {
		Thread d = new Thread() {
			@Override
			public void run() {
				try {
				server.start();
				servletInstance.reconnect();
				server.join();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		d.start();
	}

	/**
	 * Returns a new instance of a {@link GeneralConnectionServlet}.
	 *
	 * Override this method if you want to return a subclass of GeneralConnectionServlet
	 */
	public GeneralConnectionServlet getServlet(long timeOut, boolean secure, boolean local) {
		if (!secure && production) {
			System.err.println("Running an insecure server");
		}
		return new GeneralConnectionServlet(timeOut, secure, local);
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
	public final boolean parseCommand(String command, BufferedReader sysin) throws Exception {
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
				this.runMost();
			}
			return true;
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
				this.runMost();
			} else {
				System.out.println("you can not start the server because it is already running.");
			}
			return true;
		}
		return parseUtilityCommand(command, sysin);
	}

	// TODO: add a command manager of some sort.
	public boolean parseUtilityCommand(String command, BufferedReader sysin) throws Exception {
		if (command.equals("toggle logging")) {
			if (isLogging) {
				System.out.println("Are you sure you want to turn loggin off? [y/n]");
				if (!sysin.readLine().equalsIgnoreCase("y")) {
					System.out.println("action canceled");
					return true;
				}
			}
			System.out.println("Turning loggin " + (isLogging ? "Off" : "On"));
			isLogging = ! isLogging;
			return true;
		} if (command.equals("connectionNumber")) {
			System.out.println(servletInstance.getCurrentConnectionNumber());
			return true;
		}
		return false;
	}

	public void startInput() {
		Thread d = new Thread() {
			@Override
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
