package coursesketch.update.command;
import com.google.protobuf.ByteString;

import coursesketch.update.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlSketch;

/**
 * Undo
 *
 * @author Thomas COladonato
 *
 */
public class RedoObject extends Command {

    public RedoObject() {
        type = CommandType.REDO;
        //FIXME set the time to match client load time
    }
    @Override
    public ByteString toByteString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute(SrlSketch s) {
        // TODO Auto-generated method stub

    }

    @Override
    public void undo(SrlSketch s) {
        // TODO Auto-generated method stub

    }
}
