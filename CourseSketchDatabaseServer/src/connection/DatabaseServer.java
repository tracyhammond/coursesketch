package connection;

import handlers.DataInsertHandler;
import handlers.DataRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServer.Decoder;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionManager;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.joda.time.DateTime;

import protobuf.srl.request.Message.Request;
import database.DatabaseAccessException;
import database.institution.Institution;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
@WebSocket()
public class DatabaseServer extends GeneralConnectionServer {

	public DatabaseServer(GeneralConnectionServlet parent) {
		super(parent);
	}

	@Override
	public void onOpen(Session conn) {
		super.onOpen(conn);
		send(conn, TimeManager.serverSendTimeToClient());
	}

	@Override
	public void onMessage(Session conn, Request req) {
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			System.out.println("Submitting submission id");
			try {
				Institution.mongoInsertSubmission(req);
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
				System.out.println("THIS NEEDS TO BE SENT TO THE CLIENT!");
			}
		} else if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			DataRequestHandler.handleRequest(req, conn, super.connectionToId.get(conn).getKey(), getConnectionManager());
		} else if (req.getRequestType() == Request.MessageType.DATA_INSERT) {
			DataInsertHandler.handleData(req, conn);
		} else if (req.getRequestType() == Request.MessageType.TIME) {
			Request rsp = TimeManager.decodeRequest(req);
			if (rsp != null) {
				send(conn, rsp);
			}
		}
		System.out.println("Finished looking at query");
	}
}
