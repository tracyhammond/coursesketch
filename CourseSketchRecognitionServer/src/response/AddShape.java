package response;

import java.util.UUID;

import com.google.protobuf.ByteString;

import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Interpretation;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlInterpretation;
import protobuf.srl.sketch.Sketch.SrlShape;

/**
 * This Command object creates an empty container at first to have recognition
 * be performed on, will return all interpretations in its bytestring.
 * 
 * @author Matthew Dillard
 *
 */
public class AddShape extends Command {
	private Shape data;
	
	public AddShape(SrlShape input){
		type = CommandType.ADD_SHAPE;
		
		data = new Shape();
		data.setId(UUID.fromString(input.getId()));
		data.setName(input.getName());
		//FIXME set the time to match client load time
	}

	@Override
	public ByteString toByteString() {
		SrlShape.Builder shapebuilder = SrlShape.newBuilder();
		
		shapebuilder.setId(data.getId().toString());
		shapebuilder.setTime(data.getTimeEnd());
		
		SrlInterpretation.Builder interpretationbuilder = SrlInterpretation.newBuilder();
		for (Interpretation i: data.getNBestList()){
			interpretationbuilder.setLabel(i.label);
			interpretationbuilder.setConfidence(i.confidence);
			
			shapebuilder.addInterpretations(interpretationbuilder.build());
		}
		
		return shapebuilder.build().toByteString();
	}
	
	@Override
	public void execute(Sketch s) {
		s.add(data);
	}
}
