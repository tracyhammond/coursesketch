package coursesketch.recognition;

import coursesketch.recognition.defaults.DefaultRecognition;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.pdollar.PDollarRecognizer;
import coursesketch.recognition.pdollar.Point;
import coursesketch.recognition.pdollar.RecognizerResults;
import coursesketch.services.RecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.utils.SketchUtil;
import utilities.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by gigemjt on 4/16/16.
 */
public class BasicRecognition extends DefaultRecognition {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionService.class);

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
        List<Sketch.SrlStroke> srlStrokes = new ArrayList<>();
        List<Point> pointCloud = convert(srlUpdateList, srlStrokes);
        final RecognizerResults recognizerResults = recognizer.Recognize(pointCloud);
        LOG.info("RECOGNIZED SKETCH AS " + recognizerResults.mName);

        Sketch.SrlInterpretation.Builder interpretation = Sketch.SrlInterpretation.newBuilder();
        interpretation.setLabel(recognizerResults.mName);
        interpretation.setConfidence(recognizerResults.mScore);
        interpretation.setComplexity(1);

        Commands.SrlUpdateList.Builder updateList = Commands.SrlUpdateList.newBuilder();
        updateList.addList(createUpdateFromResult(interpretation.build(), srlStrokes));

        return updateList.build();
    }

    public Commands.SrlUpdate createUpdateFromResult(Sketch.SrlInterpretation interpretation, List<Sketch.SrlStroke> affectedStrokes) {

        Sketch.SrlShape.Builder shape = Sketch.SrlShape.newBuilder();
        shape.setId(UUID.randomUUID().toString());
        shape.setTime(TimeManager.getSystemTime());
        shape.setIsUserCreated(false);
        shape.addInterpretations(interpretation);
        LOG.debug("CREATING A NEW SHAPE WITH ID: {}", shape.getId());

        Commands.SrlCommand.Builder addShapeCommand = Commands.SrlCommand.newBuilder();
        addShapeCommand.setCommandType(Commands.CommandType.ADD_SHAPE);
        addShapeCommand.setIsUserCreated(false);
        addShapeCommand.setCommandData(shape.build().toByteString());
        addShapeCommand.setCommandId(UUID.randomUUID().toString());

        Commands.SrlUpdate.Builder update = Commands.SrlUpdate.newBuilder();

        update.setUpdateId(UUID.randomUUID().toString());
        update.setTime(TimeManager.getSystemTime());

        final Commands.SrlCommand.Builder packageShape = Commands.SrlCommand.newBuilder();
        packageShape.setIsUserCreated(false);
        packageShape.setCommandId(UUID.randomUUID().toString());
        packageShape.setCommandType(Commands.CommandType.PACKAGE_SHAPE);

        final Commands.ActionPackageShape.Builder actionPackage = Commands.ActionPackageShape.newBuilder();
        final SketchUtil.IdChain.Builder idChain = SketchUtil.IdChain.newBuilder();

        idChain.addIdChain(shape.getId());
        actionPackage.setNewContainerId(idChain);

        for (Sketch.SrlStroke stroke: affectedStrokes) {
            actionPackage.addShapesToBeContained(stroke.getId());
            LOG.debug("PACKING SHAPES WITH ID: {}", stroke.getId());
        }

        packageShape.setCommandData(actionPackage.build().toByteString());

        update.addCommands(addShapeCommand);
        update.addCommands(packageShape);

        return update.build();
    }

    @Override public Sketch.SrlSketch recognize(final String s, final Sketch.SrlSketch srlSketch) throws RecognitionException {
        return null;
    }

    private List<Point> convert(Commands.SrlUpdateList srlUpdateList, List<Sketch.SrlStroke> affectedStrokes) {
        List<Point> points = new ArrayList<Point>();

        List<Commands.SrlUpdate> updates = srlUpdateList.getListList();
        for (Commands.SrlUpdate update : updates) {
            List<Commands.SrlCommand> commands = update.getCommandsList();
            for (Commands.SrlCommand command: commands) {
                Commands.CommandType commandType = command.getCommandType();
                if(commandType.equals(Commands.CommandType.ADD_STROKE)) {
                    try {
                        Sketch.SrlStroke stroke = Sketch.SrlStroke.parseFrom(command.getCommandData());
                        List<Sketch.SrlPoint> srlPoints = stroke.getPointsList();
                        List<Point> tempPoints = srlPointsToPoint(srlPoints, stroke.getId());
                        points.addAll(tempPoints);
                        affectedStrokes.add(stroke);
                    }
                    catch (com.google.protobuf.InvalidProtocolBufferException e) {
                        LOG.error("There was no stroke contained in the request.");
                    }
                }
            }
        }
        return points;
    }

    private List<Point> srlPointsToPoint(List<Sketch.SrlPoint> srlPoints, String strokeId) {
        List<Point> points = new ArrayList<Point>();
        for (Sketch.SrlPoint srlPoint : srlPoints) {
            Point point = new Point(srlPoint.getX(), srlPoint.getY(), strokeId);
            points.add(point);
        }
        return points;
    }
}
