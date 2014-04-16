package proxyServer;

import internalConnections.AnswerConnection;
import internalConnections.DataConnection;
import internalConnections.LoginConnection;
import internalConnections.LoginConnectionState;
import internalConnections.ProxyConnectionManager;
import internalConnections.RecognitionConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionState;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import connection.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket()
public class ProxyServer extends GeneralConnectionServer {

	@SuppressWarnings("hiding")
	public static final int MAX_CONNECTIONS = 60; // sets see if this works!

	public static final int STATE_INVALID_LOGIN = 4002;
	public static final int MAX_LOGIN_TRIES = 5;
	public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";

	private SocketManager socketManager = new SocketManager();

	static int numberOfConnections = Integer.MIN_VALUE;

	public ProxyServer(GeneralConnectionServlet parent) {
		super(parent);
		ActionListener listener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, LoginConnection.class);
				getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, AnswerConnection.class);
				getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, RecognitionConnection.class);
			}
		};
		TimeManager.setTimeEstablishedListener(listener);

		socketManager.setExpiredListiner(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				idToConnection.get(e.getActionCommand()).close();		
			}
		});
	}

	/**
	 * Accepts messages and sends the request to the correct server and holds minimum client state.
	 */
	@Override
	public void onMessage(Session conn, Request req) {
		LoginConnectionState state = (LoginConnectionState) connectionToId.get(conn);

		//DO NOT FORGET ABOUT THIS
		if (state.isPending()) {
			//conn.send(pending);
			return;
		}
		if (!state.isLoggedIn()) {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			String sessionID = state.getKey();
			System.out.println("Request type is " + req.getRequestType().name());
			try {
				this.getConnectionManager().send(req, sessionID, LoginConnection.class);
			} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
				send(conn, createBadConnectionResponse(req, LoginConnection.class));
			}
		} else {
			if (state.getTries() > MAX_LOGIN_TRIES) {
				conn.close(STATE_INVALID_LOGIN, INVALID_LOGIN_MESSAGE);
				return;
			}
			if (req.getRequestType() == MessageType.RECOGNITION) {
				System.out.println("REQUEST TYPE = RECOGNITION");
				String sessionID = state.getKey();
				try {
					((ProxyConnectionManager)this.getConnectionManager()).send(req, sessionID, RecognitionConnection.class); // no userId is sent for security reasons.
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					send(conn, createBadConnectionResponse(req, RecognitionConnection.class));
				}
				return;
			}
			if (req.getRequestType() == MessageType.SUBMISSION) {
				System.out.println("REQUEST TYPE = SUBMISSION");
				String sessionID = state.getKey();
				try {
					((ProxyConnectionManager)this.getConnectionManager()).send(req, sessionID, AnswerConnection.class, ((ProxyConnectionState) state).getUserId());
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					send(conn, createBadConnectionResponse(req, AnswerConnection.class));
				}
				return;
			}
			if (req.getRequestType() == MessageType.DATA_REQUEST || req.getRequestType() == MessageType.DATA_INSERT
					|| req.getRequestType() == MessageType.DATA_UPDATE || req.getRequestType() == MessageType.DATA_REMOVE) {
				System.out.println("REQUEST TYPE = DATA REQUEST");
				String sessionID = state.getKey();
				try {
					((ProxyConnectionManager)this.getConnectionManager()).send(req, sessionID, DataConnection.class, ((ProxyConnectionState) state).getUserId());
				} catch(org.java_websocket.exceptions.WebsocketNotConnectedException e) {
					send(conn, createBadConnectionResponse(req, DataConnection.class));
				}
				return;
			}
			return;
		}
	}

	/**
	 * Returns a number that should be unique.
	 */
	@Override
	public MultiConnectionState getUniqueState() {
		return new ProxyConnectionState(Encoder.nextID().toString());
	}

	public String getName() {
    	return "Proxy Socket";
    }
}
