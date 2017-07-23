package coursesketch.services.recognition;

import com.google.protobuf.ServiceException;
import coursesketch.recognition.framework.RecognitionInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.framework.exceptions.TemplateException;
import coursesketch.server.compat.ClientWebSocket;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;
import protobuf.srl.sketch.Sketch;

import java.net.URI;
import java.util.List;

/**
 * A service for recognition.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class RecognitionWebSocketClient extends ClientWebSocket implements RecognitionInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionWebSocketClient.class);

    /**
     * The default address for the Submission server.
     */
    public static final String ADDRESS = "RECOGNITION_IP_PROP";

    /**
     * The default port of the Submission Server.
     */
    public static final int PORT = 8893;

    /**
     * Exception for template exceptions.
     */
    private static final String TEMPLATE_EXCEPTION_MESSAGE = "Exception when adding template";

    /**
     * The blocker service that is used to communicate.
     */
    private RecognitionServer.RecognitionService.BlockingInterface recognitionService;


    /**
     * Creates a ConnectionWrapper to a destination using a given server.
     * <p/>
     * Note that this does not actually try and connect the wrapper you have to
     * either explicitly call {@link coursesketch.server.interfaces.AbstractClientWebSocket#connect()}.
     *
     * @param iDestination
     *         The location the server is going as a URI. ex:
     *         http://example.com:1234
     * @param iParentServer The server that created the websocket.
     */
    public RecognitionWebSocketClient(final URI iDestination,
            final AbstractServerWebSocketHandler iParentServer) {
        super(iDestination, iParentServer);
    }

    @Override
    public final Commands.SrlUpdateList addUpdate(final String recognitionId, final Commands.SrlUpdate srlUpdate)
            throws RecognitionException {
        if (recognitionService == null) {
            recognitionService = RecognitionServer.RecognitionService.newBlockingStub(getRpcChannel());
        }

        final RecognitionServer.AddUpdateRequest.Builder addUpdateRequest = RecognitionServer.AddUpdateRequest.newBuilder();
        addUpdateRequest.setRecognitionId(recognitionId);
        addUpdateRequest.setUpdate(srlUpdate);

        final RecognitionServer.RecognitionResponse recognitionResponse;

        try {
            LOG.debug("Sending srlUpdate addition request {}", addUpdateRequest);
            recognitionResponse = recognitionService.addUpdate(getNewRpcController(), addUpdateRequest.build());
            if (recognitionResponse.hasDefaultResponse() && recognitionResponse.getDefaultResponse().hasException()) {
                final DatabaseAccessException databaseException =
                        new DatabaseAccessException("Exception with submission server");
                databaseException.setProtoException(recognitionResponse.getDefaultResponse().getException());
                throw new RecognitionException("Exception when adding update", databaseException);
            }
        } catch (ServiceException e) {
            throw new RecognitionException("Exception when adding update service error", e);
        }
        LOG.debug("IN RPC SERVER; RECOGNITION SERVER REPONSE:");
        LOG.debug(recognitionResponse.toString());
        return recognitionResponse.getChanges();
    }

    @Override
    public final Commands.SrlUpdateList setUpdateList(final String recognitionId, final Commands.SrlUpdateList srlUpdateList)
            throws RecognitionException {
        if (recognitionService == null) {
            recognitionService = RecognitionServer.RecognitionService.newBlockingStub(getRpcChannel());
        }

        final RecognitionServer.RecognitionUpdateList.Builder recognitionUpdateList = RecognitionServer.RecognitionUpdateList.newBuilder();
        recognitionUpdateList.setRecognitionId(recognitionId);
        recognitionUpdateList.setUpdateList(srlUpdateList);


        final RecognitionServer.RecognitionResponse recognitionResponse;

        try {
            LOG.debug("Sending SrlUpdateList recognition request");
            recognitionResponse = recognitionService.createUpdateList(getNewRpcController(), recognitionUpdateList.build());
            if (recognitionResponse.getDefaultResponse().hasException()) {
                final DatabaseAccessException databaseException =
                        new DatabaseAccessException("Exception with submission server");
                databaseException.setProtoException(recognitionResponse.getDefaultResponse().getException());
                throw new RecognitionException(TEMPLATE_EXCEPTION_MESSAGE, databaseException);
            }
        } catch (ServiceException e) {
            throw new RecognitionException(TEMPLATE_EXCEPTION_MESSAGE, e);
        }
        return recognitionResponse.getChanges();
    }

    @Override
    public final Sketch.SrlSketch setSketch(final String sketchId, final Sketch.SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a template to the recognition server.
     * @param templateId The id of the template being added.
     * @param interpretation The interpretation of the template.
     * @param template The template data.
     * @throws TemplateException Thrown if there are problems creating the template.
     */
    private void addTemplate(final String templateId, final Sketch.SrlInterpretation interpretation,
            final Sketch.RecognitionTemplate.Builder template) throws TemplateException {
        if (recognitionService == null) {
            recognitionService = RecognitionServer.RecognitionService.newBlockingStub(getRpcChannel());
        }
        template.setTemplateId(templateId);
        template.setInterpretation(interpretation);
        try {
            LOG.debug("Sending template addition request");
            final Message.DefaultResponse defaultResponse = recognitionService.addTemplate(getNewRpcController(), template.build());
            if (defaultResponse.hasException()) {
                final DatabaseAccessException databaseException =
                        new DatabaseAccessException("Exception with submission server");
                databaseException.setProtoException(defaultResponse.getException());
                throw new TemplateException(TEMPLATE_EXCEPTION_MESSAGE, databaseException);
            }
        } catch (ServiceException e) {
            throw new TemplateException(TEMPLATE_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public final void addTemplate(final String templateId, final Sketch.SrlInterpretation interpretation,
            final Sketch.SrlSketch sketch) throws TemplateException {
        addTemplate(templateId, interpretation, Sketch.RecognitionTemplate.newBuilder().setSketch(sketch));
    }

    @Override
    public final void addTemplate(final String templateId, final Sketch.SrlInterpretation interpretation,
            final Sketch.SrlShape srlShape) throws TemplateException {
        addTemplate(templateId, interpretation, Sketch.RecognitionTemplate.newBuilder().setShape(srlShape));
    }

    @Override
    public final void addTemplate(final String templateId, final Sketch.SrlInterpretation interpretation,
            final Sketch.SrlStroke srlStroke) throws TemplateException {
        addTemplate(templateId, interpretation, Sketch.RecognitionTemplate.newBuilder().setStroke(srlStroke));
    }

    @Override
    public final void trainTemplate(final Sketch.RecognitionTemplate recognitionTemplate) throws TemplateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void finishTraining() throws RecognitionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Commands.SrlUpdateList recognize(final String recognitionId, final Commands.SrlUpdateList srlUpdateList)
            throws RecognitionException {
        if (recognitionService == null) {
            recognitionService = RecognitionServer.RecognitionService.newBlockingStub(getRpcChannel());
        }

        final RecognitionServer.RecognitionUpdateList.Builder recognitionUpdateList = RecognitionServer.RecognitionUpdateList.newBuilder();
        recognitionUpdateList.setRecognitionId(recognitionId);
        recognitionUpdateList.setUpdateList(srlUpdateList);


        final RecognitionServer.RecognitionResponse recognitionResponse;

        try {
            LOG.debug("Sending SrlUpdateList recognition request");
            recognitionResponse = recognitionService.recognize(getNewRpcController(), recognitionUpdateList.build());
            if (recognitionResponse.getDefaultResponse().hasException()) {
                final DatabaseAccessException databaseException =
                        new DatabaseAccessException("Exception with recognition server");
                databaseException.setProtoException(recognitionResponse.getDefaultResponse().getException());
                throw new RecognitionException("Exception when recognizing update List", databaseException);
            }
        } catch (ServiceException e) {
            throw new RecognitionException("Service Exception when recognizing update List", e);
        }
        return recognitionResponse.getChanges();
    }

    @Override
    public final Sketch.SrlSketch recognize(final String sketchId, final Sketch.SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final List<Sketch.SrlInterpretation> recognize(final String sketchId, final Sketch.RecognitionTemplate recognitionTemplate) throws
            RecognitionException {
        return null;
    }

    @Override
    public final List<Sketch.RecognitionTemplate> generateTemplates(final Sketch.RecognitionTemplate recognitionTemplate)
            throws RecognitionException {
        if (recognitionService == null) {
            recognitionService = RecognitionServer.RecognitionService.newBlockingStub(getRpcChannel());
        }

        RecognitionServer.GeneratedTemplates generatedTemplates = null;
        try {
            LOG.debug("Sending generate additional templates request");
            generatedTemplates = recognitionService
                    .generateTemplates(getNewRpcController(), recognitionTemplate);
            if (generatedTemplates.getDefaultResponse().hasException()) {
                final DatabaseAccessException databaseException =
                        new DatabaseAccessException("Exception with recognition server");
                databaseException.setProtoException(generatedTemplates.getDefaultResponse().getException());
                throw new RecognitionException("Exception when recognizing update List", databaseException);
            }
        } catch (ServiceException e) {
            throw new TemplateException(TEMPLATE_EXCEPTION_MESSAGE, e);
        }
        return generatedTemplates.getGeneratedTemplatesList();
    }

    @Override
    public final void initialize() throws RecognitionException {
        throw new UnsupportedOperationException();
    }
}
