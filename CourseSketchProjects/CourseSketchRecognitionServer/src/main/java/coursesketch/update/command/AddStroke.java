package coursesketch.update.command;

import java.util.UUID;

import com.google.protobuf.ByteString;

import coursesketch.update.Command;
import protobuf.srl.sketch.Sketch.SrlPoint;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.sketch.Sketch.SrlPoint;

/**
 * Simple command to add a user-drawn stroke.
 *
 * @author Matthew Dillard
 *
 */
public class AddStroke extends Command {
    protected SrlStroke data;

    public AddStroke(SrlStroke input){
        id = UUID.fromString(input.getId());
        type = CommandType.ADD_STROKE;

        data = new Stroke();
        data.setId(UUID.fromString(input.getId()));

        data.setName(input.getName());
        for (SrlPoint s_point : input.getPointsList()) {
            data.addPoint(new Point(s_point.getX(), s_point.getY(), s_point.getTime(),Point.nextID()));
        }
    }

    @Override
    public ByteString toByteString() {
        SrlStroke.Builder strokebuilder = SrlStroke.newBuilder();

        strokebuilder.setId(data.getId().toString());
        strokebuilder.setTime(data.getTimeEnd());
        strokebuilder.setName(data.getName());

        for (Point p: data.getPoints()){
            SrlPoint.Builder pointbuilder = SrlPoint.newBuilder();
            pointbuilder.setX(p.x);
            pointbuilder.setY(p.y);
            pointbuilder.setTime(p.time);
            pointbuilder.setId(p.getId().toString());
            strokebuilder.addPoints(pointbuilder.build());
        }

        return strokebuilder.build().toByteString();
    }

    @Override
    /**
     * adds a single stroke to the sketch for recognition
     */
    public void execute(Sketch s) {
        s.add(data);
    }
    @Override
    public void undo(Sketch s) {
        s.remove(data);
    }
}
