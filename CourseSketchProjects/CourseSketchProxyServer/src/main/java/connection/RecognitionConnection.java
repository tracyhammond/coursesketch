package connection;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.framework.exceptions.TemplateException;
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

import java.net.URI;
import java.util.List;

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
        RecognitionServer.RecognitionResponse.Builder response = RecognitionServer.RecognitionResponse.newBuilder();
        switch (generalRecognitionRequest.getRequestType()) {
            case ADD_UPDATE:
                response.setChanges(addUpdate(generalRecognitionRequest));
                break;
            case SET_NEW_LIST:
                response.setChanges(setNewList(generalRecognitionRequest));
                break;
            case ADD_TEMPLATE:
                response = null;
                addTemplate(generalRecognitionRequest);
                requestResponse.setOtherData(Message.DefaultResponse.getDefaultInstance().toByteString());
                break;
            case RECOGNIZE:
                response.setChanges(recognize(generalRecognitionRequest));
                break;
            case GENERATE_SHAPES:
                response = null;
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

    /**
     * Creates an update list from the {@link RecognitionServer.GeneralRecognitionRequest}.
     *
     * @param generalRecognitionRequest A request that has come in.
     * @return a parsed request of an added update.
     * @throws RecognitionException Thrown if something goes wrong.
     */
    private Commands.SrlUpdateList addUpdate(final RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest)
            throws RecognitionException {
        LOG.debug(generalRecognitionRequest.toString());
        return super.addUpdate(generalRecognitionRequest.getAddUpdate().getRecognitionId(),
                generalRecognitionRequest.getAddUpdate().getUpdate());
    }

    /**
     * Creates an update list from the {@link RecognitionServer.GeneralRecognitionRequest}.
     *
     * @param generalRecognitionRequest A request that has come in.
     * @return a parsed request of a new update list.
     * @throws RecognitionException Thrown if something goes wrong.
     */
    private Commands.SrlUpdateList setNewList(final RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest)
            throws RecognitionException {
        return super.setUpdateList(generalRecognitionRequest.getSetUpdateList().getRecognitionId(),
                generalRecognitionRequest.getSetUpdateList().getUpdateList());
    }

    /**
     * Creates an update list from the {@link RecognitionServer.GeneralRecognitionRequest}.
     *
     * @param generalRecognitionRequest A request that has come in.
     * @return a parsed request from a recognized sketch.
     * @throws RecognitionException Thrown if something goes wrong.
     */
    private Commands.SrlUpdateList recognize(final RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest)
            throws RecognitionException {
        return super.recognize(generalRecognitionRequest.getSetUpdateList().getRecognitionId(),
                generalRecognitionRequest.getSetUpdateList().getUpdateList());
    }

    /**
     * Adds a template that has been recognized.
     *
     * @param generalRecognitionRequest A request that has come in.
     * @throws TemplateException Thrown if something goes wrong.
     */
    private void addTemplate(final RecognitionServer.GeneralRecognitionRequest generalRecognitionRequest) throws TemplateException {
        final String templateId = generalRecognitionRequest.getTemplate().getTemplateId();
        final Sketch.SrlInterpretation interpretation = generalRecognitionRequest.getTemplate().getInterpretation();
        if (generalRecognitionRequest.getTemplate().hasShape()) {
            super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getShape());
        } else if (generalRecognitionRequest.getTemplate().hasSketch()) {
            super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getSketch());
        } else {
            super.addTemplate(templateId, interpretation, generalRecognitionRequest.getTemplate().getStroke());
        }
    }
}
