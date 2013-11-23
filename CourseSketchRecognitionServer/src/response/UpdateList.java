package response;

import java.util.LinkedList;
import java.util.List;

import srl.core.sketch.Sketch;

public class UpdateList{
	LinkedList<Update> syncList;
	
	public UpdateList(){
		syncList = new LinkedList<Update>();
	}
	
	public void add(Update up){
		syncList.add(up);
	}
	
	public List<Update> getList(){
		return syncList;
	}
	
	public Update get(int index){
		return syncList.get(index);
	}
	
	public Update back(){
		return syncList.get(syncList.size()-1);
	}
	
	/**
	 * Execute commands from a specific Update on a sketch,
	 * not sure why you'd want to, but I'm not stopping you
	 * @param s PaleoSketch Sketch
	 * @param index
	 */
	public void execute(Sketch s,int index){
		syncList.get(index).execute(s);
	}
	
	/**
	 * Executes the commands only from the last update
	 * @param s PaleoSketch Sketch
	 */
	public void executeLast(Sketch s){
		syncList.get(syncList.size()-1).execute(s);
	}
	
	/**
	 * Executes every command sequentially, able to recreate
	 * an entire sketch from scratch
	 * @param s PaleoSketch Sketch
	 */
	public void executeAll(Sketch s){
		for(Update up: syncList){
			up.execute(s);
		}
	}
}
