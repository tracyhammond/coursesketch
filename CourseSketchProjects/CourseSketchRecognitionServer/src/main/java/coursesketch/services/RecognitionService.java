package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.recognition.framework.RecognitionInterface;
import coursesketch.server.interfaces.ISocketInitializer;
import coursesketch.server.rpc.CourseSketchRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.request.Message;
import protobuf.srl.services.recognition.RecognitionServer;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlInterpretation;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.utils.SketchUtil.IdChain;
import utilities.ExceptionUtilities;
import utilities.TimeManager;

import java.util.UUID;

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

        RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();

        SrlUpdate.Builder update = request.getUpdate().newBuilderForType();

        SrlInterpretation.Builder interpretationn = SrlInterpretation.newBuilder();
        interpretationn.setLabel("line");
        interpretationn.setConfidence(1);
        interpretationn.setComplexity(1);

        SrlShape.Builder shape = SrlShape.newBuilder();
        shape.setId(UUID.randomUUID().toString());
        shape.setTime(TimeManager.getSystemTime());
        shape.setIsUserCreated(false);
        shape.addInterpretations(interpretationn);

        IdChain.Builder shapeIdChain = IdChain.newBuilder();
        shapeIdChain.addIdChain(shape.getId());

        SrlCommand.Builder addShapeCommand = SrlCommand.newBuilder();
        addShapeCommand.setCommandType(CommandType.ADD_SHAPE);
        addShapeCommand.setIsUserCreated(false);
        addShapeCommand.setCommandData(shape.build().toByteString());
        addShapeCommand.setCommandId(UUID.randomUUID().toString());

        ActionPackageShape.Builder actionPackageShape = Commands.ActionPackageShape.newBuilder();
        LOG.debug("About to create a SrlStroke: " + shape.getId());
        LOG.debug(update.toString());
        try {
            SrlStroke stroke = SrlStroke.parseFrom(request.getUpdate().getCommands(0).getCommandData());
            actionPackageShape.addShapesToBeContained(stroke.getId());
        }
        catch (com.google.protobuf.InvalidProtocolBufferException e) {
            LOG.error("There was no stroke contained in the request.");
            result.setDefaultResponse(ExceptionUtilities.createExceptionResponse(e));
            done.run(result.build());
            return;
        }
        LOG.info("Created a SrlStroke!");
        actionPackageShape.setNewContainerId(shapeIdChain);

        SrlCommand.Builder packageShapeCommand = SrlCommand.newBuilder();
        packageShapeCommand.setCommandType(CommandType.PACKAGE_SHAPE);
        packageShapeCommand.setIsUserCreated(false);
        packageShapeCommand.setCommandData(actionPackageShape.build().toByteString());
        packageShapeCommand.setCommandId(UUID.randomUUID().toString());

        update.setUpdateId(UUID.randomUUID().toString());
        update.setTime(TimeManager.getSystemTime());
        update.addCommands(addShapeCommand);
        update.addCommands(packageShapeCommand);

        SrlUpdateList.Builder updateList = SrlUpdateList.newBuilder();
        updateList.addList(update);

        LOG.debug("RETURNING DATA {}", updateList);

        result.setChanges(updateList);

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
    public void addTemplate(final RpcController controller, final RecognitionServer.RecognitionTemplate request,
            final RpcCallback<Message.DefaultResponse> done) {
        // adds template to the database

        String templateId = request.getTemplateId();
        SrlStroke stroke = request.getStroke();
        SrlShape shape = request.getShape();
        SrlSketch sketch = request.getSketch();

        Message.DefaultResponse.Builder defaultResponse = Message.DefaultResponse.newBuilder();
        defaultResponse.setSuccessful(true);

        done.run(defaultResponse.build());
    }

    @Override
    public void recognize(final RpcController controller, final RecognitionServer.RecognitionUpdateList request,
            final RpcCallback<RecognitionServer.RecognitionResponse> done) {

        SrlUpdateList updateList = SrlUpdateList.getDefaultInstance();

        RecognitionServer.RecognitionResponse.Builder result = RecognitionServer.RecognitionResponse.newBuilder();

        result.setChanges(updateList);

        done.run(result.build());
    }
}
