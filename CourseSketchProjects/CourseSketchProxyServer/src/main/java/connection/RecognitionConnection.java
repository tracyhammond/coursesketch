package connection;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.recognition.framework.exceptions.RecognitionException;

import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.server.interfaces.MultiConnectionState;
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
public final class RecognitionConnection extends RecognitionWebSocketClient {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServerWebSocketHandler.class);

    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     *
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.base.ClientWebSocket#connect()} or call
     * {@link coursesketch.server.base.ClientWebSocket#send(ByteBuffer)}.
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

    /**
     * Parses a recognition request.
     * @param request The request meant for recognition server.
     * @param sessionId The id of the session.
     * @throws ConnectionException Thrown if there are problems connecting to the remote server.
     * @throws RecognitionException Thrown if there are problems with recognition.
     */
    public void parseConnection(final Message.Request request, final String sessionId) throws ConnectionException, RecognitionException {
        final RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest;
        try {
            generalRecognitionRequest = RecognitionServer.GeneralRecognitionRequest
                    .parseFrom(request.getOtherData());
        } catch (InvalidProtocolBufferException e) {
            throw new ConnectionException("Unable to parse proto request for recognition", e);
        }

        final Message.Request.Builder requestResponse = Message.Request.newBuilder(request);
        RecognitionServer.RecognitionResponse.Builder response = null;
        final Commands.SrlUpdateList updateList;
        switch (generalRecognitionRequest.getRequestType()) {
            case ADD_UPDATE:
                LOG.debug(generalRecognitionRequest.toString());
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.addUpdate(generalRecognitionRequest.getAddUpdate().getRecognitionId(),
                        generalRecognitionRequest.getAddUpdate().getUpdate());
                response.setChanges(updateList);
                break;
            case SET_NEW_LIST:
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.setUpdateList(generalRecognitionRequest.getSetUpdateList().getRecognitionId(),
                        generalRecognitionRequest.getSetUpdateList().getUpdateList());
                response.setChanges(updateList);
                break;
            case ADD_TEMPLATE:
                final String templateId = generalRecognitionRequest.getTemplate().getTemplateId();
                final Sketch.SrlInterpretation interpretation = generalRecognitionRequest.getTemplate().getInterpretation();
                if (generalRecognitionRequest.getTemplate().hasShape()) {
                     super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getShape());
                } else if (generalRecognitionRequest.getTemplate().hasSketch()) {
                    super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getSketch());
                } else {
                    super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getStroke());
                }
                requestResponse.setOtherData(Message.DefaultResponse.getDefaultInstance().toByteString());
                break;
            case RECOGNIZE:
                response = RecognitionServer.RecognitionResponse.newBuilder();
                updateList = super.recognize(generalRecognitionRequest.getSetUpdateList().getRecognitionId(),
                        generalRecognitionRequest.getSetUpdateList().getUpdateList());
                response.setChanges(updateList);
                break;
            case GENERATE_SHAPES:
                final List<Sketch.RecognitionTemplate> recognitionTemplates = super.generateTemplates(generalRecognitionRequest.getTemplate());
                final RecognitionServer.GeneratedTemplates.Builder generatedTemplates = RecognitionServer.GeneratedTemplates.newBuilder();
                generatedTemplates.addAllGeneratedTemplates(recognitionTemplates);
                requestResponse.setOtherData(generatedTemplates.build().toByteString());
                break;
            default:
                LOG.info("Unknown case was created: {}", generalRecognitionRequest.getRequestType().name());
                throw new RecognitionException("Unknown case for recognition was created: " + generalRecognitionRequest.getRequestType().name());
        }

        final MultiConnectionState state = getStateFromId(sessionId);
        if (response != null) {
            requestResponse.setOtherData(response.build().toByteString());
        }
        final Message.Request result = ProxyConnectionManager.createClientRequest(requestResponse.build());
        this.getParentServer().send(getConnectionFromState(state), result);
    }
}
