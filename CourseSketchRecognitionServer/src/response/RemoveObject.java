package response;

import java.util.UUID;

import protobuf.srl.commands.Commands.IdChain;

import srl.core.sketch.Sketch;

public class RemoveObject extends Command {
	private IdChain data;
	
	public RemoveObject(IdChain input){
		data = input;
	}

	@Override
	/**
	 * Removes a single object and all its subcomponents
	 */
	public void execute(Sketch s) {
		s.remove(UUID.fromString(data.getIdChain(data.getIdChainCount()-1)));
	}
}
