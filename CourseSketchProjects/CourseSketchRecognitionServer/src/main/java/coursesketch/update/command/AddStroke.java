package coursesketch.update.command;

import java.util.UUID;

import com.google.protobuf.ByteString;

import coursesketch.update.Command;
import protobuf.srl.sketch.Sketch.SrlPoint;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.commands.Commands.CommandType;

/**
 * Simple command to add a user-drawn stroke.
 *
 * @author Matthew Dillard
 *
 */
public class AddStroke extends Command {
    protected SrlStroke data;

    public AddStroke(SrlStroke input){
        /*
        id = UUID.fromString(input.getId());
        type = CommandType.ADD_STROKE;

        data = new SrlStroke();
        data.setId(UUID.fromString(input.getId()));

        data.setName(input.getName());
        for (SrlPoint s_point : input.getPointsList()) {
            data.addPoint(new SrlPoint(s_point.getX(), s_point.getY(), s_point.getTime(), SrlPoint.nextID()));
        }
        */
        data = input;
    }

    @Override
    public ByteString toByteString() {
        SrlStroke.Builder strokebuilder = SrlStroke.newBuilder();

        strokebuilder.setId(data.getId().toString());
        strokebuilder.setTime(data.getTime());
        strokebuilder.setName(data.getName());

        for (SrlPoint p: data.getPointsList()){
            SrlPoint.Builder pointbuilder = SrlPoint.newBuilder();
            pointbuilder.setX(p.getX());
            pointbuilder.setY(p.getY());
            pointbuilder.setTime(p.getTime());
            pointbuilder.setId(p.getId().toString());
            strokebuilder.addPoints(pointbuilder.build());
        }

        return strokebuilder.build().toByteString();
    }

    @Override
    /**
     * adds a single stroke to the sketch for recognition
     */
    public void execute(SrlSketch s) {
        // s.add(data);
    }
    @Override
    public void undo(SrlSketch s) {
        // s.remove(data);
    }
}
