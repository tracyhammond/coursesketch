package internalConnections;

import java.net.URI;
import java.nio.ByteBuffer;

import multiConnection.MultiInternalConnectionServer;
import multiConnection.WrapperConnection;

import org.java_websocket.drafts.Draft;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlUser;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class LoginConnection extends WrapperConnection {

	public LoginConnection( URI serverUri , Draft draft , MultiInternalConnectionServer parent) {
		super( serverUri, draft, parent);
	}

	public void onMessage(ByteBuffer buffer) {
		Request r = MultiInternalConnectionServer.Decoder.parseRequest(buffer);
		LoginConnectionState state = (LoginConnectionState) getStateFromId(r.getSessionInfo());
		if (r.getLogin().getIsLoggedIn()) {
			state.logIn(r.getLogin().getIsInstructor(), r.getServersideId());
		}

		Request  result = ProxyConnectionManager.createClientRequest(r); // strips away identification
		getConnectionFromState(state).send(result.toByteArray());

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
			this.parentManager.send(createUser.build(), r.getSessionInfo(), DataConnection.class);
		}
	}
}