package connection;

import handlers.DataRequestHandler;
import handlers.SubmissionRequestHandler;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class SubmissionServer extends GeneralConnectionServer {

	public SubmissionServer(GeneralConnectionServlet parent) {
		super(parent);
	}

	@Override
	public void onMessage(Session conn, Request req) {
		/**
		 * Attempts to save the submission, which can be either a solution or an experiment.
		 * If it is an insertion and not an update then it will send the key to the database
		 */
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			Request result = SubmissionRequestHandler.handleRequest(req, getConnectionManager());
			if (result != null) {
				send(conn, result);
			}
		}

		if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			Request result = DataRequestHandler.handleRequest(req);
			if (result != null) {
				send(conn, result);
			}
		}

	}
}
