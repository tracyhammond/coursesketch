package response;

import protobuf.srl.commands.Commands.CommandType;
import srl.core.sketch.Sketch;

/**
 * Placeholder class so that all the separate commands can be treated the same
 * 
 * @author matt
 *
 */

public abstract class Command {
	protected CommandType type;
	
	/**
	 * Returns the command type, one of
	 * ADD_STROKE
	 * ADD_SHAPE
	 * REMOVE_OBJECT
	 * PACKAGE_SHAPE
	 * @return CommandType
	 */
	public CommandType getType(){
		return type;
	}
	
	/**
	 * execute singular command, intended to be called in sequence for every Update
	 * @param s PaleoSketch Sketch
	 */
	public abstract void execute(Sketch s);
}
