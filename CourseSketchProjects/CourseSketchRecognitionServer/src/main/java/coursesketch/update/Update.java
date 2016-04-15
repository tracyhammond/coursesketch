package coursesketch.update;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import protobuf.srl.sketch.Sketch.SrlSketch;

/**
 * Collection of commands that encapsulates one complete transmission between
 * the recognition server and anything else.
 *
 * @author Matthew Dillard
 *
 */
public class Update implements Iterable<Command>{
    private LinkedList<Command> commandList;
    private long time;

    /**
     * Default constructor
     */
    public Update(){
        commandList = new LinkedList<Command>();
        time = System.currentTimeMillis();
    }

    /**
     * Constructor used to set time
     * @param time Time
     */
    public Update(long time){
        commandList = new LinkedList<Command>();
        this.time = time;
    }

    /**
     * Simple function to add a command
     * @param c
     */
    public void add(Command c){
        commandList.add(c);
    }

    /**
     * @return Time of Update
     */
    public long getTime(){
        return time;
    }

    /**
     * Simple setter to manually set the time
     * @param t
     */
    public void setTime(long t){
        time = t;
    }

    /**
     * Simple getter function
     * @param index
     * @return Command
     */
    public Command getCommand(int index){
        return commandList.get(index);
    }

    /**
     * @return size of Update
     */
    public int size(){
        return commandList.size();
    }

    /**
     * @return Entire list of commands in this Update
     */
    public List<Command> getCommandList(){
        return Collections.unmodifiableList(commandList);
    }

    /**
     * Execute all commands from this update on this sketch
     * @param s PaleoSketch Sketch
     */
    public void execute(SrlSketch s){
        for(Command command: commandList){
            command.execute(s);
        }
    }

    /**
     * Undo all commands from this update on this sketch
     * @param s PaleoSketch Sketch
     */
    public void undo(SrlSketch s){
        for (int i = commandList.size(); i>=0; i--) {
            commandList.get(i).undo(s);
        }
    }

    @Override
    public Iterator<Command> iterator() {
        return commandList.iterator();
    }
}
