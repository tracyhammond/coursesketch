package coursesketch.recognition;

import coursesketch.recognition.defaults.DefaultRecognition;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.pdollar.Point;
import coursesketch.services.RecognitionService;
import coursesketch.update.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.commands.Commands;
import protobuf.srl.sketch.Sketch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 4/16/16.
 */
public class BasicRecognition extends DefaultRecognition {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionService.class);

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


        return Commands.SrlUpdateList.getDefaultInstance();
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
                        List<Point> tempPoints = srlPointsToPoint(srlPoints);
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

    private List<Point> srlPointsToPoint(List<Sketch.SrlPoint> srlPoints) {
        List<Point> points = new ArrayList<Point>();
        for (Sketch.SrlPoint srlPoint : srlPoints) {
            Point point = new Point(srlPoint.getX(), srlPoint.getY(), srlPoint.getId().hashCode());
            points.add(point);
        }
        return points;
    }
}
