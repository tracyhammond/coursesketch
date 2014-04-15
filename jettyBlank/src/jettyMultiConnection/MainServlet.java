package jettyMultiConnection;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
@WebServlet(name = "Course Sketch WebSocket Servlet", urlPatterns = { "/socket" })
public class MainServlet extends WebSocketServlet {

	ServerSocket sock = new ServerSocket();

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(10000);
        factory.setCreator(new SocketCreator());
    }

    class SocketCreator implements WebSocketCreator {
    	/**
    	 * Creates the new websocket
    	 */
		@Override
		public final Object createWebSocket(ServletUpgradeRequest arg0, ServletUpgradeResponse arg1) {
			return sock;
		}
    	
    }
}