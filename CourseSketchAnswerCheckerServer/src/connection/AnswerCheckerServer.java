package connection;

import internalConnection.AnswerConnectionState;
import internalConnection.SolutionConnection;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.GeneralConnectionServer;
import jettyMultiConnection.GeneralConnectionServlet;
import jettyMultiConnection.MultiConnectionState;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * A simple WebSocketServer implementation.
 *
 * This is a backend server that is only connected by other servers
 */
@WebSocket()
public class AnswerCheckerServer extends GeneralConnectionServer {

	public AnswerCheckerServer(GeneralConnectionServlet parent) {
		super(parent);
	}

	@Override
	public void onMessage(Session conn, Request req) {

		if (req.getRequestType()==Request.MessageType.TIME) {
			Request rsp = TimeManager.decodeRequest(req);
			if (rsp != null) {
				send(conn, rsp);
			}
			return;
		}
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			// then we submit!
			if (req.getResponseText().equals("student")) {
				MultiConnectionState state = connectionToId.get(conn);
				try {
					SrlUpdate.parseFrom(req.getOtherData());
					System.out.println("Parsing as an update");
					try {
						getConnectionManager().send(req, req.getSessionInfo() + "+" + state.getKey(), SolutionConnection.class);
					} catch (ConnectionException e) {
						e.printStackTrace();
					}
					return;
				} catch (InvalidProtocolBufferException e) {
					System.out.println("Parsing as an experiment");
					SrlExperiment student = null;
					try {
						student = SrlExperiment.parseFrom(req.getOtherData());
					} catch (InvalidProtocolBufferException e1) {
						e1.printStackTrace();
					}
					((AnswerConnectionState) state).addPendingExperiment(req.getSessionInfo(), student);
					System.out.println("Student exp " + student);
					try {
						getConnectionManager().send(req, req.getSessionInfo() + "+" + state.getKey(), SolutionConnection.class);
					} catch (ConnectionException e1) {
						e1.printStackTrace();
					} // pass submission on

					// request the solution for checking  NOSHIP: need to actually retrieve answer.
					Request.Builder builder = Request.newBuilder();
					builder.setRequestType(MessageType.DATA_REQUEST);
					builder.setSessionInfo(req.getSessionInfo() + "+" + state.getKey());
					ItemRequest.Builder itemRequest = ItemRequest.newBuilder();
					itemRequest.setQuery(ItemQuery.SOLUTION);
					itemRequest.addItemId(student.getProblemId());  // FIXME: this needs to change probably to make this work
					//internalConnections.send(builder.setOtherData(itemRequest.build().toByteString()).build(), state.getKey(), SolutionConnection.class);
				}
			} else {
				try {
					getConnectionManager().send(req, req.getSessionInfo(), SolutionConnection.class);
				} catch (ConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
