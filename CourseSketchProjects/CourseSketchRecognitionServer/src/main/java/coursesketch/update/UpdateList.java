package coursesketch.update;

import protobuf.srl.sketch.Sketch.SrlSketch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Contatiner for Updates. This object maintains the history of transmissions
 * between the Recognition server and anyone else.
 *
 * @author Matthew Dillard
 *
 */
public class UpdateList implements Iterable<Update> {
    LinkedList<Update> syncList;

    /**
     * Default constructor to make a list with an empty history
     */
    public UpdateList() {
        syncList = new LinkedList<Update>();
    }

    /**
     * Adds a complete Update to the end of the list
     * @param up
     */
    public void add(final Update up) {
        syncList.add(up);
    }

    /**
     * @return Entire history of Updates
     */
    public List<Update> getList() {
        return syncList;
    }

    /**
     * Simple getter for the history
     * @param index
     * @return Update at index
     */
    public Update get(int index) {
        return syncList.get(index);
    }

    /**
     * @return most recent Update
     */
    public Update back() {
        return syncList.get(syncList.size() - 1);
    }

    /**
     * Execute commands from a specific Update on a sketch,
     * not sure why you'd want to, but I'm not stopping you
     * @param s PaleoSketch Sketch
     * @param index
     */
    public void execute(SrlSketch s, int index) {
        syncList.get(index).execute(s);
    }

    /**
     * Executes the commands only from the last update
     * @param s PaleoSketch Sketch
     */
    public void executeLast(SrlSketch s) {
        syncList.get(syncList.size() - 1).execute(s);
    }

    /**
     * Executes every command sequentially, able to recreate
     * an entire sketch from scratch
     * @param s PaleoSketch Sketch
     */
    public void executeAll(SrlSketch s) {
        for (Update up : syncList) {
            up.execute(s);
        }
    }

    @Override
    public Iterator<Update> iterator() {
        return syncList.iterator();
    }
}
