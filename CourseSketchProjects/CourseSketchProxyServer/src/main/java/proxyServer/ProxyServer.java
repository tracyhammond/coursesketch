package proxyServer;

import internalConnections.AnswerConnection;
import internalConnections.DataConnection;
import internalConnections.LoginConnection;
import internalConnections.LoginConnectionState;
import internalConnections.ProxyConnectionManager;
import internalConnections.RecognitionConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Set;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionState;

import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import multiconnection.GeneralConnectionServlet;
import multiconnection.MultiConnectionState;


import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.joda.time.DateTime;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import connection.ConnectionException;
import connection.TimeManager;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE)
public class ProxyServer extends GeneralConnectionServer {

	@SuppressWarnings("hiding")
	public static final int MAX_CONNECTIONS = 60; // sets see if this works!

	public static final int STATE_INVALID_LOGIN = 4002;
	public static final int MAX_LOGIN_TRIES = 5;
	public static final String INVALID_LOGIN_MESSAGE = "Too many incorrect login attempts.\nClosing connection.";

	static int numberOfConnections = Integer.MIN_VALUE;

	public ProxyServer(GeneralConnectionServlet parent) {
		super(parent);
		ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, LoginConnection.class);
				} catch (ConnectionException e1) {
					e1.printStackTrace();
				}
				try {
					getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, AnswerConnection.class);
				} catch (ConnectionException e1) {
					e1.printStackTrace();
				}
				/*
				try {
					getConnectionManager().send(TimeManager.serverSendTimeToClient(), null, RecognitionConnection.class);
				} catch (ConnectionException e1) {
					e1.printStackTrace();
				}
				// */
				Set<Session> conns=connectionToId.keySet();
				for(Session conn:conns)
				{	
					send(conn, TimeManager.serverSendTimeToClient());
				}
			}
		};
		TimeManager.setTimeEstablishedListener(listener);
	}

	@Override
	public void onOpen(Session conn) {
		super.onOpen(conn);
		send(conn, TimeManager.serverSendTimeToClient());
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
			} catch(Exception e) {
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
				} catch(Exception e) {
					System.err.println("Recognition error!");
					send(conn, createBadConnectionResponse(req, RecognitionConnection.class));
				}
				return;
			}
			if (req.getRequestType() == MessageType.SUBMISSION) {
				System.out.println("REQUEST TYPE = SUBMISSION");
				String sessionID = state.getKey();
				try {
					((ProxyConnectionManager)this.getConnectionManager()).send(req, sessionID, AnswerConnection.class, ((ProxyConnectionState) state).getUserId());
				} catch(Exception e) {
					e.printStackTrace();
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
				} catch(Exception e) {
					e.printStackTrace();
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

	@Override
	public String getName() {
    	return "Proxy";
    }

	/**
	 * Creates the listener that happens when the server fails to communicate to another websocket.
	 *
	 * This is typically the case
	 */
	public void initializeListeners() {
		System.out.println("Creating the socket failed listeners for the server");
		ActionListener listen = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("Looking at the failed messages");
				ArrayList<ByteBuffer> failedMessages = (ArrayList<ByteBuffer>) e.getSource();
				for (ByteBuffer message : failedMessages) {
					try {
						Request req = Request.parseFrom(message.array());
						MultiConnectionState state = getIdToState().get(req.getSessionInfo());
						Class<? extends ConnectionWrapper> classType = (Class<? extends ConnectionWrapper>) Class.forName(e.getActionCommand());
						Request result = createBadConnectionResponse(req, classType);
						send(getIdToConnection().get(state), result);
					} catch (InvalidProtocolBufferException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		this.getConnectionManager().setFailedSocketListener(listen, AnswerConnection.class);
		this.getConnectionManager().setFailedSocketListener(listen, DataConnection.class);
		this.getConnectionManager().setFailedSocketListener(listen, LoginConnection.class);
	}
}
