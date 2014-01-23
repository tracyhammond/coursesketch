package response;

import java.util.List;
import java.util.LinkedList;
import java.util.UUID;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.IdChain;
import srl.core.sketch.SContainer;
import srl.core.sketch.Sketch;

/**
 * Command to take a group of shapes from one container and
 * put them in a new one.
 * 
 * @author Matthew Dillard
 *
 */
public class PackageShape extends Command {
	private IdChain oldContainer;
	private IdChain newContainer;
	private List<String> contained;

	public PackageShape(ActionPackageShape input){
		type = CommandType.PACKAGE_SHAPE;
		
		oldContainer = input.getOldContainerId();
		newContainer = input.getNewContainerId();
		contained = input.getShapesToBeContainedList();
	}
	
	public PackageShape(SContainer from, SContainer to, List<String> moving){
		oldContainer = forgeChain(from);
		newContainer = forgeChain(to);
		
		contained = moving;
	}

	/**
	 * Helper function to make a list of the hierarchical containers
	 * from a single top level object. Designed for using communication
	 * using [data which keeps track of its own structure] with someone
	 * who has data that does not keep track
	 * @return IdChain
	 */
	private IdChain forgeChain(SContainer top){
		// FIXME this is not implemented
		List<String> chain = new LinkedList<String>();
				
		IdChain.Builder builder = IdChain.newBuilder();
		return builder.build();
	}
	
	@Override
	public ByteString toByteString() {
		ActionPackageShape.Builder build = ActionPackageShape.newBuilder();
		
		build.setOldContainerId(oldContainer);
		build.setNewContainerId(newContainer);
		build.addAllShapesToBeContained(contained);
		
		return build.build().toByteString();
	}
	
	@Override
	/**
	 * This one takes all the items with IDs contained in [contained]
	 * from [OldContainer] and puts them in the [NewContainer]
	 */
	public void execute(Sketch s) {
		// TODO these casts feel risky. Lets make sure they work!
		SContainer from;
		if(oldContainer == null)
			from = s;
		else
			from = (SContainer) s.get(UUID.fromString(oldContainer.getIdChain(oldContainer.getIdChainCount()-1)));
		
		SContainer to;
		if(newContainer == null)
			to = s;
		else
			to = (SContainer) s.get(UUID.fromString(newContainer.getIdChain(newContainer.getIdChainCount()-1)));
		
		if(from == to)
			return;
		
		for(String id : contained){
			to.add(from.remove(UUID.fromString(id)));
		}
	}
}
