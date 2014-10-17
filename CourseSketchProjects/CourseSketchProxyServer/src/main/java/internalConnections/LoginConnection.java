package internalconnections;

import com.google.protobuf.InvalidProtocolBufferException;
import connection.ConnectionException;
import connection.TimeManager;
import multiconnection.ConnectionWrapper;
import multiconnection.GeneralConnectionServer;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.school.School.SrlUser;

import java.net.URI;
import java.nio.ByteBuffer;


/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
@WebSocket()
public final class LoginConnection extends ConnectionWrapper {

    /**
     * Creates a new connection for the Answer checker server.
     * @param destination The location of the login server.
     * @param parent The proxy server instance.
     */
	public LoginConnection(final URI destination, final GeneralConnectionServer parent) {
		super(destination, parent);
	}

    /**
     * Accepts messages and sends the request to the correct server and holds minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
     *
     * If the user was logging in for the first time (registering) then a message is sent to create the user.
     *
     * @param buffer The message that is received by this object.
     */
	@Override
	public void onMessage(final ByteBuffer buffer) {
		final Request r = GeneralConnectionServer.Decoder.parseRequest(buffer);
		if (r.getRequestType() == Request.MessageType.TIME) {
			final Request rsp = TimeManager.decodeRequest(r);
			if (rsp != null) {
				try {
					this.getParentManager().send(rsp, r.getSessionInfo(), LoginConnection.class);
				} catch (ConnectionException e) {
					e.printStackTrace();
				}
			}
			return;
		}

        if (r.getRequestType() == Request.MessageType.LOGIN) {
            LoginInformation login = null;
            try {
                login = LoginInformation.parseFrom(r.getOtherData());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            final LoginConnectionState state = (LoginConnectionState) getStateFromId(r.getSessionInfo());
            state.addTry();
            if (login == null) {
                // FUTURE: make this send an error message to flag.
                System.out.println("Login failed to get to the client");
                return;
            }
            if (login.getIsLoggedIn()) {
                state.logIn(login.getIsInstructor(), r.getServersideId());
            }

            final Request result = ProxyConnectionManager.createClientRequest(r); // strips away identification
            GeneralConnectionServer.send(getConnectionFromState(state), result);

            if (login.getIsRegistering() && login.getIsLoggedIn()) {
                // extra steps that we need to do
                final Request.Builder createUser = Request.newBuilder();
                createUser.setServersideId(r.getServersideId());
                createUser.setRequestType(MessageType.DATA_INSERT);
                final DataSend.Builder dataSend = DataSend.newBuilder();
                final ItemSend.Builder itemSend = ItemSend.newBuilder();
                itemSend.setQuery(ItemQuery.USER_INFO);
                final SrlUser.Builder user = SrlUser.newBuilder();
                user.setEmail(login.getEmail());
                user.setUsername(login.getUsername());
                itemSend.setData(user.build().toByteString());
                dataSend.addItems(itemSend);
                createUser.setOtherData(dataSend.build().toByteString());
                try {
                    this.getParentManager().send(createUser.build(), r.getSessionInfo(), DataConnection.class);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
	}
}
