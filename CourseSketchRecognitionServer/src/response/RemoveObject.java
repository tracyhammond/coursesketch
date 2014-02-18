package response;

import java.util.UUID;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.IdChain;
import srl.core.sketch.SComponent;
import srl.core.sketch.Sketch;

/**
 * Command to remove a single object from the overall sketch.
 * 
 * @author Matthew Dillard
 *
 */
public class RemoveObject extends Command {
	private IdChain data;
	private SComponent scomponent = null;
	
	public RemoveObject(IdChain input){
		id = UUID.fromString(data.getIdChain(data.getIdChainCount()-1))
		type = CommandType.REMOVE_OBJECT;
		
		data = input;
	}

	@Override
	public ByteString toByteString() {
		return data.toByteString();
	}
	
	@Override
	/**
	 * Removes a single object and all its subcomponents
	 */
	public void execute(Sketch s) {
		scomponent = s.remove(UUID.fromString(data.getIdChain(data.getIdChainCount()-1)));
	}
	public void undo(Sketch s){
		s.add(scomponent);
	}
}
