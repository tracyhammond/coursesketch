package response;

import java.util.UUID;

import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.sketch.Sketch.SrlPoint;

public class AddStroke extends Command {
	private Stroke data;
	
	public AddStroke(SrlStroke input){
		type = CommandType.ADD_STROKE;
		
		data = new Stroke();
		data.setId(UUID.fromString(input.getId()));
		
		data.setName(input.getName());
		for (SrlPoint s_point : input.getPointsList()) {
			data.addPoint(new Point(s_point.getX(), s_point.getY(), s_point.getTime(),Point.nextID()));
		}
	}

	@Override
	/**
	 * adds a single stroke to the sketch for recognition
	 */
	public void execute(Sketch s) {
		s.add(data);
	}
}
