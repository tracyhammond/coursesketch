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
public class UndoObject extends Command {

    public UndoObject() {
        type = CommandType.UNDO;
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
