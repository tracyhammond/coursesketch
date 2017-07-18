package coursesketch.recognition;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.update.Command;
import coursesketch.update.Update;
import coursesketch.update.UpdateDeque;
import coursesketch.update.command.AddShape;
import coursesketch.update.command.AddStroke;
import coursesketch.update.command.ClearObject;
import coursesketch.update.command.PackageShape;
import coursesketch.update.command.RedoObject;
import coursesketch.update.command.RemoveObject;
import coursesketch.update.command.UndoObject;
import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.utils.SketchUtil.IdChain;

import java.util.LinkedList;

/**
 * In charge of responding to updates.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.ExcessiveMethodLength" })
public class Response {
    /**
     * A list of updates.
     */
    private final UpdateDeque syncDeque;
    /**
     * The sketch.
     */
    private final SrlSketch sketch;

    /**
     * Default constructor that initializes the recognizer with all primitives on
     */
    public Response() {
        syncDeque = new UpdateDeque();
        sketch = SrlSketch.getDefaultInstance();
    }

    /**
     * Point of communication, takes an update and runs the recognizer
     * and returns the results.
     *
     * @param srlUpdate The update that has been added.
     * @return The recognized data.
     * @throws InvalidProtocolBufferException Unsupported Command.
     */
    public SrlUpdate recognize(SrlUpdate srlUpdate) throws InvalidProtocolBufferException {
        syncDeque.add(parseUpdate(srlUpdate));
        syncDeque.executeLast(sketch);
        if (syncDeque.front().getStroke() == null) {
            return null;
        }

        //perform recognition
        Update actions = new Update();
        // actions.add(new AddShape(m_recognizer.recognize(syncDeque.front().parseStroke())));

        //List<String> ids = new LinkedList<String>();
        //ids.add(syncDeque.front().getStroke().getId().toString());
        // syncDeque.front().parseStroke()
        // actions.add(new PackageShape(null, null, ids));

        actions.setTime(System.currentTimeMillis());
        syncDeque.add(actions);
        syncDeque.executeLast(sketch);

        return repackage(actions);
    }

    /**
     * Executes an {@link SrlUpdateList} on a new sketch.
     *
     * @param updates List of updates.
     * @return A new sketch with only those updates.
     * @throws InvalidProtocolBufferException If it is unable to add the updates.
     */
    public static SrlSketch viewTest(SrlUpdateList updates) throws InvalidProtocolBufferException {
        SrlSketch returnSketch = SrlSketch.getDefaultInstance();
        UpdateDeque list = new UpdateDeque();
        for (SrlUpdate update : updates.getListList()) {
            list.add(parseUpdate(update));
            //list.executeLast(returnSketch);
        }
        //returnSketch = new Sketch();
        list.executeAll(returnSketch);
        return returnSketch;
    }

    /**
     * Parses a Protobuf type update into a usable commands.
     *
     * @param srlUpdate The update that is being parsed.
     *
     * @throws InvalidProtocolBufferException Unsupported Command
     */
    private static Update parseUpdate(SrlUpdate srlUpdate) throws InvalidProtocolBufferException {
        //System.out.println("Number of commands " + srlUpdate.getCommandsCount());
        Update update = new Update(srlUpdate.getTime());

        for (SrlCommand srlCommand : srlUpdate.getCommandsList()) {
            Command command = null;
            switch (srlCommand.getCommandType()) {
                case ADD_STROKE:
                    command = new AddStroke(SrlStroke.parseFrom(srlCommand.getCommandData()));
                    break;
                case ADD_SHAPE:
                    command = new AddShape(SrlShape.parseFrom(srlCommand.getCommandData()));
                    break;
                case PACKAGE_SHAPE:
                    command = new PackageShape(ActionPackageShape.parseFrom(srlCommand.getCommandData()));
                    break;
                case REMOVE_OBJECT:
                    command = new RemoveObject(IdChain.parseFrom(srlCommand.getCommandData()));
                    break;
                case UNDO:
                    command = new UndoObject();
                    break;
                case REDO:
                    command = new RedoObject();
                    break;
                case MARKER:
                    break;
                case CLEAR:
                    command = new ClearObject();
                    break;
                case ASSIGN_ATTRIBUTE:
                    break;
                case CLEAR_STACK:
                    break;
                case CLOSE_SYNC:
                    break;
                case FORCE_INTERPRETATION:
                    break;
                case OPEN_SYNC:
                    break;
                case REMOVE_ATTRIBUTE:
                    break;
                case REWRITE:
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported command: " + srlCommand.getCommandType());
            }
            if (command != null) {
                update.add(command);
            }
        }
        return update;
    }

    /**
     * Empty function designed to return a properly packaged update.
     *
     * @param update Update.
     * @return SrlUpdate A repackged update.
     */
    private static SrlUpdate repackage(Update update) {

        SrlUpdate.Builder updateBuilder = SrlUpdate.newBuilder();

        updateBuilder.setTime(update.getTime());

        LinkedList<SrlCommand> container = new LinkedList<SrlCommand>();
        for (Command command : update.getCommandList()) {
            SrlCommand.Builder commandBuilder = SrlCommand.newBuilder();

            commandBuilder.setCommandType(command.getType());
            commandBuilder.setCommandData(command.toByteString());

            container.add(commandBuilder.build());
        }
        updateBuilder.addAllCommands(container);

        return updateBuilder.build();
    }
}
