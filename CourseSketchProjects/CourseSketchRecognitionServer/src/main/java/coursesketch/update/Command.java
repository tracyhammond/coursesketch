package coursesketch.update;

import com.google.protobuf.ByteString;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlSketch;

import java.util.UUID;

/**
 * Placeholder class so that all the separate commands can be treated the same.
 *
 * - ADD_SHAPE
 * - ADD_STROKE
 * - PACKAGE_SHAPE
 * - REMOVE_OBJECT
 *
 * @author Matthew Dillard
 *
 */
public abstract class Command {
    /**
     * The type of the command.
     */
    protected CommandType type;

    /**
     * The type of the marker.
     */
    protected protobuf.srl.commands.Commands.Marker.MarkerType markerType = null;

    /**
     * The id of the command.
     */
    protected UUID id = null;

    /**
     * Returns the command type.
     *
     * <pre>
     * one of
     * ADD_STROKE
     * ADD_SHAPE
     * REMOVE_OBJECT
     * PACKAGE_SHAPE
     * </pre>
     *
     * @return CommandType The type of the command.
     */
    public CommandType getType() {
        return type;
    }

    /**
     * @return The marker type.
     */
    public protobuf.srl.commands.Commands.Marker.MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * For use in packaging up commands.
     *
     * @return ByteString a packaged command.
     */
    public abstract ByteString toByteString();

    /**
     * Execute singular command, intended to be called in sequence for every Update.
     *
     * @param sketch The sketch on which the command is executed.
     */
    public abstract void execute(SrlSketch sketch);

    /**
     * Undo as singular command, intended to be called in sequence for every Update.
     *
     * @param sketch The sketch on which the command is undone.
     */
    public abstract void undo(SrlSketch sketch);

    /**
     * @return UUID Returns ID of this command.
     */
    public UUID getId() {
        return id;
    }
}
