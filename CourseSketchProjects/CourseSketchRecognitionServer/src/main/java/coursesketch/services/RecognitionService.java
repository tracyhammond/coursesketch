package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.recognition.framework.RecognitionInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlStroke;
import utilities.ExceptionUtilities;

/**
 * Created by David Windows on 4/13/2016.
 */
public class RecognitionService extends RecognitionServer.RecognitionService implements CourseSketchRpcService {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionService.class);

    private final RecognitionInterface recognitionManager;

    public RecognitionService(final RecognitionInterface manager) {
        recognitionManager = manager;
    }

    @Override public void setSocketInitializer(final ISocketInitializer socketInitializer) {

    }

    @Override public void addUpdate(final RpcController controller, final RecognitionServer.AddUpdateRequest request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {
        LOG.debug("REQUEST: {}", request);

        SrlUpdateList.Builder build = SrlUpdateList.newBuilder();
        build.addList(request.getUpdate());
        RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();
        try {
            result.setChanges(recognitionManager.recognize(request.getRecognitionId(), build.build()));
        } catch (RecognitionException e) {
            final Message.ProtoException protoException = ExceptionUtilities.createProtoException(e);
            result.setDefaultResponse(Message.DefaultResponse.newBuilder().setException(protoException).build());
        }

        done.run(result.build());
    }

    @Override public void createUpdateList(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {
        // sets update list
        SrlUpdateList updateList = SrlUpdateList.getDefaultInstance();

        RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();

        result.setChanges(updateList);

        done.run(result.build());
    }

    @Override
    public void addTemplate(final RpcController controller, final Sketch.RecognitionTemplate request,
            final RpcCallback<Message.DefaultResponse> done) {
        // adds template to the database

        String templateId = request.getTemplateId();
        SrlStroke stroke = request.getStroke();
        SrlShape shape = request.getShape();
        SrlSketch sketch = request.getSketch();
        LOG.debug("SKETCH {}", sketch);
        LOG.debug("TYPE {}", request.getInterpretation());


        Message.DefaultResponse.Builder defaultResponse = Message.DefaultResponse.newBuilder();
        defaultResponse.setSuccessful(true);

        done.run(defaultResponse.build());
    }

    @Override
    public void recognize(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {

        RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();
        try {
            result.setChanges(recognitionManager.recognize(request.getRecognitionId(), request.getUpdateList()));
        } catch (RecognitionException e) {
            final Message.ProtoException protoException = ExceptionUtilities.createProtoException(e);
            result.setDefaultResponse(Message.DefaultResponse.newBuilder().setException(protoException).build());
        }

        done.run(result.build());
    }
}
