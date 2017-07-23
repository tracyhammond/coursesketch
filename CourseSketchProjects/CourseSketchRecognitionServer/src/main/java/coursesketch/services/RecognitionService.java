package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.recognition.framework.RecognitionInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.framework.exceptions.TemplateException;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;
import protobuf.srl.sketch.Sketch;
import coursesketch.utilities.ExceptionUtilities;

/**
 * Created by David Windows on 4/13/2016.
 */
public final class RecognitionService extends RecognitionServer.RecognitionService implements CourseSketchRpcService {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionService.class);

    /**
     * The interface that performs the recognition specific code.
     */
    private final RecognitionInterface recognitionManager;

    /**
     * Creates a recognition service with the given Recognition Backing.
     *
     * @param manager The manager that backs the service and performs the actual recognition.
     */
    public RecognitionService(final RecognitionInterface manager) {
        recognitionManager = manager;
    }


    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {
        throw new UnsupportedOperationException("This method is not supported");
    }

    @Override public void addUpdate(final RpcController controller, final RecognitionServer.AddUpdateRequest request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {
        final SrlUpdateList.Builder build = SrlUpdateList.newBuilder();
        build.addList(request.getUpdate());
        final RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();
        try {
            result.setChanges(recognitionManager.recognize(request.getRecognitionId(), build.build()));
        } catch (RecognitionException e) {
            final Message.ProtoException protoException = ExceptionUtilities.createProtoException(e);
            result.setDefaultResponse(Message.DefaultResponse.newBuilder().setException(protoException).build());
            LOG.error("Exception during add update", e);
        }

        done.run(result.build());
    }

    @Override public void createUpdateList(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {
        // sets update list
        final SrlUpdateList updateList = SrlUpdateList.getDefaultInstance();

        final RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();

        result.setChanges(updateList);

        done.run(result.build());
    }

    @Override
    public void addTemplate(final RpcController controller, final Sketch.RecognitionTemplate request,
            final RpcCallback<Message.DefaultResponse> done) {
        final Message.DefaultResponse.Builder defaultResponse = Message.DefaultResponse.newBuilder();
        try {
            addTemplate(request);
            defaultResponse.setSuccessful(true);
        } catch (TemplateException e) {
            defaultResponse.setException(ExceptionUtilities.createProtoException(e));
        }
        done.run(defaultResponse.build());
    }

    /**
     * Adds a template to the database.
     *
     * @param template The template that is being added to the database.
     * @throws TemplateException Thrown if there is a problem adding the template.
     */
    private void addTemplate(final Sketch.RecognitionTemplate template) throws TemplateException {
        if (template.hasSketch()) {
            recognitionManager.addTemplate(template.getTemplateId(), template.getInterpretation(), template.getSketch());
        } else if (template.hasShape()) {
            recognitionManager.addTemplate(template.getTemplateId(), template.getInterpretation(), template.getShape());
        } else if (template.hasStroke()) {
            recognitionManager.addTemplate(template.getTemplateId(), template.getInterpretation(), template.getStroke());
        } else {
            throw new TemplateException("No template data has been found");
        }
    }

    @Override
    public void recognize(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {

        final RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();
        try {
            result.setChanges(recognitionManager.recognize(request.getRecognitionId(), request.getUpdateList()));
        } catch (RecognitionException e) {
            final Message.ProtoException protoException = ExceptionUtilities.createProtoException(e);
            result.setDefaultResponse(Message.DefaultResponse.newBuilder().setException(protoException).build());
            LOG.error("Exception during recognize", e);
        }

        done.run(result.build());
    }

    /**
     * <code>rpc generateTemplates(.protobuf.srl.sketch.RecognitionTemplate) returns (.protobuf.srl.services.recognition.GeneratedTemplates);</code>
     *
     * <pre>
     * *
     * Creates a list of potential template matches.
     * </pre>
     *
     */
    @Override public void generateTemplates(final RpcController controller, final Sketch.RecognitionTemplate request,
            final RpcCallback<RecognitionServer.GeneratedTemplates> done) {
        final RecognitionServer.GeneratedTemplates.Builder result = RecognitionServer.GeneratedTemplates.newBuilder();
        try {
            result.addAllGeneratedTemplates(recognitionManager.generateTemplates(request));
        } catch (RecognitionException e) {
            final Message.ProtoException protoException = ExceptionUtilities.createProtoException(e);
            result.setDefaultResponse(Message.DefaultResponse.newBuilder().setException(protoException).build());
            LOG.error("Exception during recognize", e);
        }
        done.run(result.build());
    }
}
