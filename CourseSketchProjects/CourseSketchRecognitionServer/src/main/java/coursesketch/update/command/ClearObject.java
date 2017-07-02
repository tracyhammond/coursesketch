package coursesketch.update.command;

import com.google.protobuf.ByteString;
import coursesketch.update.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlSketch;

/**
 * Clear Stack
 *
 * @author Thomas COladonato
 *
 */
public class ClearObject extends Command {

    public ClearObject() {
        type = CommandType.CLEAR;
    }

    @Override
    public ByteString toByteString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undo(SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }
}
