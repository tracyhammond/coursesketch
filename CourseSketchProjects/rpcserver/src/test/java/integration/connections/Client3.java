package integration.connections;

import coursesketch.server.base.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.request.Message.LoginInformation;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import utilities.ConnectionException;
import utilities.LoggingConstants;
import utilities.TimeManager;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public final class Client3 extends ClientWebSocket {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client3.class);

    /**
     * Creates a new connection for the Answer checker server.
     *
     * @param destination
     *            The location of the login server.
     * @param parent
     *            The proxy server instance.
     */
    public Client3(final URI destination, final AbstractServerWebSocketHandler parent) {
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
        if (request.getRequestType() == MessageType.TIME) {
            LOG.info("RECEIVED TIME DATA {}", request.toString());
            final Request rsp = TimeManager.decodeRequest(request);
            if (rsp != null) {
                try {
                    this.getParentManager().send(rsp, request.getSessionInfo(), Client1.class);
                } catch (ConnectionException e) {
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            }
            return;
        }

        if (request.getRequestType() == MessageType.LOGIN) {
            LoginInformation login = null;
            LOG.info("RECEIVED LOGIN DATA {}", request.toString());
        }
    }
}
