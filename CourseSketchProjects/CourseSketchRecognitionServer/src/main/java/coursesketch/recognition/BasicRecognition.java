package coursesketch.recognition;

import coursesketch.recognition.defaults.DefaultRecognition;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.pdollar.PDollarRecognizer;
import coursesketch.recognition.pdollar.Point;
import coursesketch.recognition.pdollar.RecognizerResults;
import protobuf.srl.commands.Commands;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.utils.SketchUtil;
import utilities.TimeManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by gigemjt on 4/16/16.
 */
public class BasicRecognition extends DefaultRecognition {

    PDollarRecognizer recognizer = new PDollarRecognizer();

    public BasicRecognition(final TemplateDatabaseInterface templateDatabase) {
        super(templateDatabase);
    }

    @Override public Commands.SrlUpdateList addUpdate(final String s, final Commands.SrlUpdate srlUpdate) throws RecognitionException {
        return Commands.SrlUpdateList.getDefaultInstance();
    }

    @Override public Commands.SrlUpdateList setUpdateList(final String s, final Commands.SrlUpdateList srlUpdateList) throws RecognitionException {
        return Commands.SrlUpdateList.getDefaultInstance();
    }

    @Override public Sketch.SrlSketch setSketch(final String s, final Sketch.SrlSketch srlSketch) throws RecognitionException {
        return null;
    }

    @Override public Commands.SrlUpdateList recognize(final String s, final Commands.SrlUpdateList srlUpdateList) throws RecognitionException {
        List<Point> pointCloud = convert(srlUpdateList);
        final RecognizerResults recognizerResults = recognizer.Recognize(pointCloud);

        Sketch.SrlInterpretation.Builder interpretation = Sketch.SrlInterpretation.newBuilder();
        interpretation.setLabel(recognizerResults.mName);
        interpretation.setConfidence(recognizerResults.mScore);
        interpretation.setComplexity(1);

        Commands.SrlUpdateList.Builder updateList = Commands.SrlUpdateList.newBuilder();
        updateList.addList(createUpdateFromResult(interpretation.build(), null));

        return updateList.build();
    }

    public Commands.SrlUpdate createUpdateFromResult(Sketch.SrlInterpretation interpretation, List<Sketch.SrlStroke> affectedStrokes) {

        Sketch.SrlShape.Builder shape = Sketch.SrlShape.newBuilder();
        shape.setId(UUID.randomUUID().toString());
        shape.setTime(TimeManager.getSystemTime());
        shape.setIsUserCreated(false);
        shape.addInterpretations(interpretation);

        SketchUtil.IdChain.Builder shapeIdChain = SketchUtil.IdChain.newBuilder();
        shapeIdChain.addIdChain(shape.getId());

        Commands.SrlCommand.Builder addShapeCommand = Commands.SrlCommand.newBuilder();
        addShapeCommand.setCommandType(Commands.CommandType.ADD_SHAPE);
        addShapeCommand.setIsUserCreated(false);
        addShapeCommand.setCommandData(shape.build().toByteString());
        addShapeCommand.setCommandId(UUID.randomUUID().toString());

        Commands.SrlUpdate.Builder update = Commands.SrlUpdate.newBuilder();

        update.setUpdateId(UUID.randomUUID().toString());
        update.setTime(TimeManager.getSystemTime());
        update.addCommands(addShapeCommand);

        return update.build();
    }

    @Override public Sketch.SrlSketch recognize(final String s, final Sketch.SrlSketch srlSketch) throws RecognitionException {
        return null;
    }

    private List<Point> convert(Commands.SrlUpdateList srlUpdateList) {
        return null;
    }
}
