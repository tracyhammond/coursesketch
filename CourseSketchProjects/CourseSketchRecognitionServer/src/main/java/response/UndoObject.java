package response;
import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.CommandType;
import srl.core.sketch.Sketch;

/**
 * Undo
 * 
 * @author Thomas COladonato
 *
 */
public class UndoObject extends Command {
	
	public UndoObject() {
		type = CommandType.UNDO;
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
