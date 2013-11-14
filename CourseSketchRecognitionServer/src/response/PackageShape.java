package response;

import java.util.List;
import java.util.UUID;

import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.IdChain;
import srl.core.sketch.SContainer;
import srl.core.sketch.Sketch;

public class PackageShape extends Command {
	private IdChain OldContainer;
	private IdChain NewContainer;
	private List<String> contained;

	public PackageShape(ActionPackageShape input){
		type = CommandType.PACKAGE_SHAPE;
		
		OldContainer = input.getOldContainerId();
		NewContainer = input.getNewContainerId();
		contained = input.getShapesToBeContainedList();
	}

	@Override
	/**
	 * This one takes all the items with IDs contained in [contained]
	 * from [OldContainer] and puts them in the [NewContainer]
	 */
	public void execute(Sketch s) {
		// TODO these casts feel risky. Lets make sure they work!
		SContainer from;
		if(OldContainer == null)
			from = s;
		else
			from = (SContainer) s.get(UUID.fromString(OldContainer.getIdChain(OldContainer.getIdChainCount()-1)));
		
		SContainer to;
		if(NewContainer == null)
			to = s;
		else
			to = (SContainer) s.get(UUID.fromString(NewContainer.getIdChain(NewContainer.getIdChainCount()-1)));
		
		if(from == to)
			return;
		
		for(String id : contained){
			to.add(from.remove(UUID.fromString(id)));
		}
	}
}
