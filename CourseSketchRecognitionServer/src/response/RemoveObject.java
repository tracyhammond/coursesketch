package response;

import java.util.UUID;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.IdChain;

import srl.core.sketch.Sketch;

public class RemoveObject extends Command {
	private IdChain data;
	
	public RemoveObject(IdChain input){
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
		s.remove(UUID.fromString(data.getIdChain(data.getIdChainCount()-1)));
	}
}
