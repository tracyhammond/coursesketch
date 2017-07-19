package coursesketch.update;

import protobuf.srl.sketch.Sketch;
import protobuf.srl.sketch.Sketch.SrlSketch;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Collection of commands that encapsulates one complete transmission between
 * the recognition server and anything else.
 *
 * @author Matthew Dillard
 *
 */
public class Update implements Iterable<Command> {
    /**
     * List of commands.
     */
    private LinkedList<Command> commandList;

    /**
     * The time the update was created.
     */
    private long time;

    /**
     * Default constructor.
     */
    public Update() {
        commandList = new LinkedList<Command>();
        time = System.currentTimeMillis();
    }

    /**
     * Constructor used to set time.
     *
     * @param time Time
     */
    public Update(long time) {
        commandList = new LinkedList<Command>();
        this.time = time;
    }

    public Sketch.SrlStroke getStroke() {
        return null;
    }

    /**
     * Simple function to add a command.
     *
     * @param command The command being added to the list.
     */
    public void add(Command command) {
        commandList.add(command);
    }

    /**
     * @return Time of Update.
     */
    public long getTime() {
        return time;
    }

    /**
     * Simple setter to manually set the time.
     *
     * @param time The time this update was created.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Simple getter function.
     *
     * @param index The index at which the command is in the list.
     * @return Command The command that is getting returned.
     */
    public Command getCommand(int index) {
        return commandList.get(index);
    }

    /**
     * @return size of Update.
     */
    public int size() {
        return commandList.size();
    }

    /**
     * @return Entire list of commands in this Update.
     */
    public List<Command> getCommandList() {
        return Collections.unmodifiableList(commandList);
    }

    /**
     * Execute all commands from this update on this sketch.
     *
     * @param sketch The sketch on which the update is being executed.
     */
    public void execute(SrlSketch sketch) {
        for (Command command : commandList) {
            command.execute(sketch);
        }
    }

    /**
     * Undo all commands from this update on this sketch.
     *
     * @param sketch The sketch on which the update is being undone.
     */
    public void undo(SrlSketch sketch) {
        for (int i = commandList.size(); i >= 0; i--) {
            commandList.get(i).undo(sketch);
        }
    }

    @Override
    public Iterator<Command> iterator() {
        return commandList.iterator();
    }
}
