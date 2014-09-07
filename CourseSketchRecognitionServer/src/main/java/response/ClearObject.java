package response;
import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.CommandType;
import srl.core.sketch.Sketch;

/**
 * Clear Stack
 * 
 * @author Thomas COladonato
 *
 */
public class ClearObject extends Command {
	
	public ClearObject() {
		type = CommandType.MARKER;
		markerType = protobuf.srl.commands.Commands.Marker.MarkerType.CLEAR;
		//FIXME set the time to match client load time
	}
	@Override
	public ByteString toByteString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(Sketch s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undo(Sketch s) {
		// TODO Auto-generated method stub
		
	}
}
