package response;

import java.util.LinkedList;
import java.util.List;

import srl.core.sketch.Sketch;

public class Update {
	private LinkedList<Command> data;
	private long time;
	
	public Update(long input){
		data = new LinkedList<Command>();
		time = input;
	}
	
	public void add(Command c){
		data.add(c);
	}
	
	public long getTime(){
		return time;
	}
	
	public Command getCommand(int index){
		return data.get(index);
	}
	
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
}
