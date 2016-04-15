package coursesketch.update.command;

import java.util.UUID;

import com.google.protobuf.ByteString;

import coursesketch.update.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.utils.SketchUtil.IdChain;
import protobuf.srl.sketch.Sketch.SrlSketch;

/**
 * Command to remove a single object from the overall sketch.
 *
 * @author Matthew Dillard
 *
 */
public class RemoveObject extends Command {
    private IdChain data;
    private Sketch.SrlObject removedCompontent = null;

    public RemoveObject(IdChain input){
        id = UUID.fromString(data.getIdChain(data.getIdChainCount()-1));
        type = CommandType.REMOVE_OBJECT;

        data = input;
    }

    @Override
    public ByteString toByteString() {
        return data.toByteString();
    }

    @Override
    /**
     * Removes a single object and all its subcomponents.
     */
    public void execute(SrlSketch s) {
        // scomponent = s.remove(UUID.fromString(data.getIdChain(data.getIdChainCount()-1)));
    }

    public void undo(SrlSketch s) {
       // s.add(scomponent);
    }
}
