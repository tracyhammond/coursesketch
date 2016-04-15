package connection;

import java.net.ConnectException;
import java.net.URI;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.base.ClientWebSocket;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.services.recognition.RecognitionWebSocketClient;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import protobuf.srl.services.recognition.RecognitionServer;
import utilities.ConnectionException;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public class RecognitionConnection extends RecognitionWebSocketClient {

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.base.ClientWebSocket#connect()} or call
     * {@link coursesketch.server.base.ClientWebSocket#send(byte[])}.
     *
     * @param destination
     *            The location the server is going as a URI. ex:
     *            http://example.com:1234
     * @param parent
     *            The server that is using this connection wrapper.
     */
    public RecognitionConnection(final URI destination, final AbstractServerWebSocketHandler parent) {
        super(destination, parent);
    }

    public void parseConnection(final ByteString otherData) throws ConnectionException {
        RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest;
        try {
            generalRecognitionRequest = RecognitionServer.GeneralRecognitionRequest
                    .parseFrom(otherData);
        } catch (InvalidProtocolBufferException e) {
            throw new ConnectionException("Unable to parse proto request for recognition", e);
        }
        switch (generalRecognitionRequest.getRequestType()) {
            case ADD_UPDATE:
                super.addUpdate(generalRecognitionRequest.getAddUpdate().getRecognitionId(), generalRecognitionRequest.getAddUpdate().)
                break;
            case SET_NEW_LIST:
                break;
            case ADD_TEMPLATE:
                break;
            case RECOGNIZE:
                break;
        }
    }
}
