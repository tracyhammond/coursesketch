package response;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Deque;
import java.util.List;

import srl.core.sketch.Sketch;

/**
 * Contatiner for Updates. This object maintains the history of transmissions
 * between the Recognition server and anyone else.
 * 
 * @author Matthew Dillard
 * @author Thomas Coladonato
 *
 */
public class UpdateDeque implements Iterable<Update>{
	//LinkedList<Update> syncList;
	Deque<Update> syncDeque;
	Deque<Update> undoDeque;
	
	/**
	 * Default constructor to make a list with an empty history
	 */
	public UpdateDeque(){
		//syncList = new LinkedList<Update>();
		syncDeque = new LinkedList<Update>();
		undoDeque = new LinkedList<Update>();
	}
	
	/**
	 * Adds a complete Update to the end of the list
	 * @param up
	 */
	public void add(Update up){
		syncDeque.addFirst(up);
		//may not be right
		undoDeque.clear();
	}
	
	/**
	 * Undo an update
	 */
	public void undo(){
		undoDeque.addFirst(syncDeque.removeFirst());
	}
	
	/**
	 * Redo an update
	 */
	public void redo(){
		syncDeque.addFirst(undoDeque.removeFirst());
	}
	
	
	/**
	 * @return Entire history of Updates
	 */
	public Deque<Update> getList(){
		return syncDeque;
	}
	
	/**
	 * Simple getter for the history
	 * Since the history is a Deque, this is slower than other methods.
	 * @param index
	 * @return Update at index
	 */
	public Update get(int index){
		return ((LinkedList<Update>) syncDeque).get(index);
	}
	
	/**
	 * @return most recent Update
	 */
	public Update front(){
		return syncDeque.peekFirst();
	}
	
	/**
	 * Execute commands from a specific Update on a sketch,
	 * not sure why you'd want to, but I'm not stopping you
	 * Slower than other methods because of random access.
	 * @param s PaleoSketch Sketch
	 * @param index
	 */
	public void execute(Sketch s,int index){
		((LinkedList<Update>) syncDeque).get(index).execute(s);
	}
	
	/**
	 * Executes the commands only from the last update
	 * @param s PaleoSketch Sketch
	 */
	public void executeLast(Sketch s){
		syncDeque.peekFirst().execute(s);
	}
	
	/**
	 * Executes every command sequentially, able to recreate
	 * an entire sketch from scratch
	 * @param s PaleoSketch Sketch
	 */
	public void executeAll(Sketch s){
		for(Update up: syncDeque){
			up.execute(s);
		}
	}

	@Override
	public Iterator<Update> iterator() {
		return syncDeque.iterator();
	}
}
