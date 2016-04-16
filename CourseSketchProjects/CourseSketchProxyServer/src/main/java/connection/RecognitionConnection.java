package connection;

import java.net.ConnectException;
import java.net.URI;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.server.base.ClientWebSocket;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.serverfront.ProxyServerWebSocketHandler;
import coursesketch.services.recognition.RecognitionWebSocketClient;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;
import protobuf.srl.sketch.Sketch;
import utilities.ConnectionException;

/**
 * This example demonstrates how to create a websocket connection to a server.
 * Only the most important callbacks are overloaded.
 */
@WebSocket(maxBinaryMessageSize = AbstractServerWebSocketHandler.MAX_MESSAGE_SIZE)
public class RecognitionConnection extends RecognitionWebSocketClient {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServerWebSocketHandler.class);

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

    public void parseConnection(final Message.Request request) throws ConnectionException, RecognitionException {
        RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest;
        try {
            generalRecognitionRequest = RecognitionServer.GeneralRecognitionRequest
                    .parseFrom(request.getOtherData());
        } catch (InvalidProtocolBufferException e) {
            throw new ConnectionException("Unable to parse proto request for recognition", e);
        }
        RecognitionServer.RecognitionResponse.Builder response = null;
        Commands.SrlUpdateList updateList;
        switch (generalRecognitionRequest.getRequestType()) {
            case ADD_UPDATE:
                LOG.debug(generalRecognitionRequest.toString());
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.addUpdate(generalRecognitionRequest.getAddUpdate().getRecognitionId(), generalRecognitionRequest.getAddUpdate().getUpdate());
                response.setChanges(updateList);
                break;
            case SET_NEW_LIST:
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.setUpdateList(generalRecognitionRequest.getSetUpdateList().getRecognitionId(), generalRecognitionRequest.getSetUpdateList().getUpdateList());
                response.setChanges(updateList);
                break;
            case ADD_TEMPLATE:
                if (generalRecognitionRequest.getTemplate().hasShape()) {
                     super.addTemplate(generalRecognitionRequest.getTemplate().getShape());
                }
                else if (generalRecognitionRequest.getTemplate().hasSketch()) {
                    super.addTemplate(generalRecognitionRequest.getTemplate().getSketch());
                }
                else {
                    super.addTemplate(generalRecognitionRequest.getTemplate().getStroke());
                }
                break;
            case RECOGNIZE:
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.recognize(generalRecognitionRequest.getSetUpdateList().getRecognitionId(), generalRecognitionRequest.getSetUpdateList().getUpdateList());
                response.setChanges(updateList);
                break;
        }

        Message.Request.Builder requestResponse = Message.Request.newBuilder(request);
        if (response != null) {
            requestResponse.setOtherData(response.build().toByteString());
        } else {
            requestResponse.setOtherData(Message.DefaultResponse.getDefaultInstance().toByteString());
        }
        super.onMessage(requestResponse.build().toByteString().asReadOnlyByteBuffer());
        LOG.debug("REQUEST BUILT AND SENT: {}", requestResponse);
    }
}
