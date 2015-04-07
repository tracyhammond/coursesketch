package internalconnections;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message;
import utilities.ConnectionException;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.TimeManager;
import coursesketch.server.base.ClientWebSocket;
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

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public final class LoginClientWebSocket extends ClientWebSocket {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoginClientWebSocket.class);

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the login server.
     * @param parent
     *            The proxy server instance.
     */
    public LoginClientWebSocket(final URI destination, final AbstractServerWebSocketHandler parent) {
        super(destination, parent);
    }

    /**
     * Accepts messages and sends the request to the correct server and holds
     * minimum client state.
     *
     * Also removes all identification that should not be sent to the client.
     *
     * If the user was logging in for the first time (registering) then a
     * message is sent to create the user.
     *
     * @param buffer
     *            The message that is received by this object.
     */
    @Override
    public void onMessage(final ByteBuffer buffer) {
        final Request request = AbstractServerWebSocketHandler.Decoder.parseRequest(buffer);
        if (request.getRequestType() == Request.MessageType.TIME) {
            final Request rsp = TimeManager.decodeRequest(request);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, request.getSessionInfo(), LoginClientWebSocket.class);
                } catch (ConnectionException e) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            }
            return;
        }

        if (request.getRequestType() == Request.MessageType.LOGIN) {
            LoginInformation login = null;
            try {
                login = LoginInformation.parseFrom(request.getOtherData());
            } catch (InvalidProtocolBufferException e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                this.getParentServer().send(getConnectionFromState(getStateFromId(request.getSessionInfo())),
                        ExceptionUtilities.createExceptionRequest(protoEx, request));
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);

            }
            final LoginConnectionState state = (LoginConnectionState) getStateFromId(request.getSessionInfo());
            state.addTry();
            if (login == null) {
                LOG.error("Login failed to get to the client");
                final Request result = ProxyConnectionManager.createClientRequest(request);
                final Request.Builder errorMessage = Request.newBuilder(result);
                errorMessage.setResponseText(errorMessage.getResponseText()
                        + " : The data sent back from the login server was not the correct format");
                this.getParentServer().send(getConnectionFromState(state), result);
                return;
            }

            loginUser(login, request, state);
            createUser(login, request);
        }
    }

    /**
     * Sends a message to create a user if the user is registering.
     * Also logs in the user.
     * @param login The login information sent from the server.
     * @param request The message that was sent from the login server.
     * @param state The state representing the current connection. (user)
     */
    private void loginUser(final LoginInformation login, final Request request, final LoginConnectionState state) {
        if (login.getIsLoggedIn()) {
            state.logIn(login.getIsInstructor(), request.getServersideId());
        }

        // strips away identification
        final Request result = ProxyConnectionManager.createClientRequest(request);
        this.getParentServer().send(getConnectionFromState(state), result);


    }

    /**
     * Creates a user in the new server if they were trying to register and they successfully logged in.
     * @param request The message that was sent from the login server.
     * @param login The login information sent from the server.
     */
    private void createUser(final LoginInformation login, final Request request) {
        if (login.getIsRegistering() && login.getIsLoggedIn()) {
            // extra steps that we need to do
            final Request.Builder createUser = Request.newBuilder();
            createUser.setServersideId(request.getServersideId());
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
                this.getParentManager().send(createUser.build(), request.getSessionInfo(), DataClientWebSocket.class);
            } catch (ConnectionException e) {
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
        }
    }
}
