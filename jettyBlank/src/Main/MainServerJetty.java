package Main;

/*
 * Jetty server information
 * https://www.eclipse.org/jetty/documentation/current/embedded-examples.html#d0e18352
 * 
 */

import java.net.UnknownHostException;

import jettyMultiConnection.CourseSketchServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletHandler;



public class MainServerJetty {
	
	public static void main(String[] args) throws Exception{
		try {
			startServer();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Sets up a Jetty embedded server. Uses HTTPS over port 12102 and a key certificate.
	 * @throws Exception 
	 */
	public static void startServer() throws Exception{
		
		Server server = new Server(8888);
		StatisticsHandler stats = new StatisticsHandler();
		/*
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/coursesketch");
		servletHandler.addServlet(new ServletHolder(new CourseSketchServlet()),"/");
		*/
		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(CourseSketchServlet.class, "/*");
		stats.setHandler(servletHandler);
		
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[]{stats});
		
		server.setHandler(handlers);
		
		
		
		
		server.start();
		server.join();
		
	}


}
