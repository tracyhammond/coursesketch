package jettyMultiConnection;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * The default servlet it creates a single websocket instance that is then used on all messages.
 *
 * To create a custom managment of the connections use this version
 * @author gigemjt
 */

@SuppressWarnings("serial")
@WebServlet(name = "Course Sketch WebSocket Servlet", urlPatterns = { "/" })
public class GeneralConnectionServlet extends WebSocketServlet {

	protected GeneralConnectionServer sock = createServerSocket();
	protected MultiConnectionManager manager = createConnectionManager();
	private long timeoutTime = 0;
	private boolean secure;

    public GeneralConnectionServlet(long timeoutTime, boolean secure) {
    	this.timeoutTime = timeoutTime;
    	this.secure = secure;
	}

	@Override
    public void configure(WebSocketServletFactory factory) {
    	System.out.println("Configuring servlet");
    	if (timeoutTime > 0) {
    		factory.getPolicy().setIdleTimeout(timeoutTime);
    	}
        factory.setCreator(new SocketCreator());
    }

    class SocketCreator implements WebSocketCreator {
    	/**
    	 * Creates the new websocket
    	 */
		@Override
		public final Object createWebSocket(ServletUpgradeRequest arg0, ServletUpgradeResponse arg1) {
			System.out.println("We are updating our servlet (well we are trying)");
			if (secure && !arg0.isSecure()) {
				return null;
			}
			return sock;
		}
    	
    }

    public void stop() {
    	System.out.println("Stopping socket");
    	sock.stop();
    }

    /**
     * Override this method to create a subclass of GeneralConnectionServer
     * @return
     */
    public GeneralConnectionServer createServerSocket() {
    	return new GeneralConnectionServer(this);
    }

    /**
     * Override this method to create a subclass of the GeneralConnectionServer
     * @return
     */
    private MultiConnectionManager createConnectionManager() {
		return new MultiConnectionManager(sock);
	}

    /**
	 * This is called when the reconnect command is executed.
	 *
	 * By default this command does nothing.
	 */
	public void reconnect() {
		manager.dropAllConnection(true, false);
		manager.connectServers(sock);
	}
}