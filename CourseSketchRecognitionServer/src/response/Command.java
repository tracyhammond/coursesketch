package response;

import srl.core.sketch.Sketch;

/**
 * Placeholder class so that all the separate commands can be treated the same
 * 
 * @author matt
 *
 */

public abstract class Command {
	/**
	 * execute singular command, intended to be called in sequence for every Update
	 * @param s PaleoSketch Sketch
	 */
	public abstract void execute(Sketch s);
}
