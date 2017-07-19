package coursesketch.update.command;

import com.google.protobuf.ByteString;
import coursesketch.update.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlSketch;

/**
 * Redoes the command.
 *
 * @author Thomas COladonato
 *
 */
public class RedoObject extends Command {

    public RedoObject() {
        type = CommandType.REDO;
        throw new UnsupportedOperationException();
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
