package response;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import protobuf.srl.commands.Commands.CommandType;

import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;

/**
 * Collection of commands that encapsulates one complete transmission between
 * the recognition server and anything else.
 * 
 * @author Matthew Dillard
 *
 */
public class Update implements Iterable<Command>{
	private LinkedList<Command> data;
	private long time;
	private AddStroke stroke;
	
	/**
	 * Default constructor
	 */
	public Update(){
		data = new LinkedList<Command>();
		time = System.currentTimeMillis();
	}

	/**
	 * Constructor used to set time
	 * @param t Time
	 */
	public Update(long t){
		data = new LinkedList<Command>();
		time = t;
	}

	/**
	 * Simple function to add a command
	 * @param c
	 */
	public void add(Command c){
		data.add(c);
		if(c.getType() == CommandType.ADD_STROKE)
			stroke = (AddStroke)c;
	}
	
	/**
	 * @return Stroke of any AddStroke command in this update
	 */
	public Stroke getStroke(){
		return stroke.data;
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
		return data.get(index);
	}
	
	/**
	 * @return Entire list of commands in this Update
	 */
	public List<Command> getCommandList(){
		return data;
	}
	
	/**
	 * Execute all commands from this update on this sketch
	 * @param s PaleoSketch Sketch
	 */
	public void execute(Sketch s){
		for(Command c: data){
			c.execute(s);
		}
	}

	@Override
	public Iterator<Command> iterator() {
		return data.iterator();
	}
}
