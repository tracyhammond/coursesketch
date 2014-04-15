package jettyMultiConnection;

import java.io.BufferedReader;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
@WebServlet(name = "Course Sketch WebSocket Servlet", urlPatterns = { "/socket" })
public class MainServlet extends WebSocketServlet {

	protected MultiInternalConnectionServer sock = createServerSocket();

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
    
    public void stop() {
    	sock.stop();
    }
    
    public MultiInternalConnectionServer createServerSocket() {
    	return new MultiInternalConnectionServer(this);
    }
    
    /**
	 * This is called when the reconnect command is executed.
	 *
	 * By default this command does nothing.
	 */
	public void reconnect() {}

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
				// TODO: prompt for confirmation!
				System.exit(0);
			}
			return true;
		} else if (command.equals("restart")) {
			throw new Exception("This command is not yet supported");
		} else if (command.equals("reconnect")) {
			this.reconnect();
			return true;
		}
		return false;
	}
}