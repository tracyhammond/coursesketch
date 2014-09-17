package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import jettyMultiConnection.ConnectionException;
import jettyMultiConnection.ConnectionWrapper;
import jettyMultiConnection.GeneralConnectionServer;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlUser;
import connection.TimeManager;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public class LoginConnection extends ConnectionWrapper {

	public LoginConnection(URI destination, GeneralConnectionServer parent) {
		super(destination, parent);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		Request r = GeneralConnectionServer.Decoder.parseRequest(buffer);
		if (r.getRequestType() == Request.MessageType.TIME) {
			
			Request rsp = TimeManager.decodeRequest(r);
			if (rsp != null) {
				try {
					this.parentManager.send(rsp, r.getSessionInfo(), LoginConnection.class);
				} catch (ConnectionException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		LoginConnectionState state = (LoginConnectionState) getStateFromId(r.getSessionInfo());
		if (r.getLogin().getIsLoggedIn()) {
			state.logIn(r.getLogin().getIsInstructor(), r.getServersideId());
		}

		Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
		GeneralConnectionServer.send(getConnectionFromState(state), result);

		if (r.getLogin().getIsRegistering() && r.getLogin().getIsLoggedIn()) {
			// extra steps that we need to do
			Request.Builder createUser = Request.newBuilder();
			createUser.setServersideId(r.getServersideId());
			createUser.setRequestType(MessageType.DATA_INSERT);
			DataSend.Builder dataSend = DataSend.newBuilder();
			ItemSend.Builder itemSend = ItemSend.newBuilder();
			itemSend.setQuery(ItemQuery.USER_INFO);
			SrlUser.Builder user = SrlUser.newBuilder();
			user.setEmail(r.getLogin().getEmail());
			user.setUsername(r.getLogin().getUsername());
			itemSend.setData(user.build().toByteString());
			dataSend.addItems(itemSend);
			createUser.setOtherData(dataSend.build().toByteString());
			try {
				this.parentManager.send(createUser.build(), r.getSessionInfo(), DataConnection.class);
			} catch (ConnectionException e) {
				e.printStackTrace();
			}
		}
	}
}