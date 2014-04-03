package response;

import java.util.UUID;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.CommandType;
import srl.core.sketch.Sketch;

/**
 * Placeholder class so that all the separate commands can be treated the same
 * - ADD_SHAPE
 * - ADD_STROKE
 * - PACKAGE_SHAPE
 * - REMOVE_OBJECT
 * 
 * @author Matthew Dillard
 *
 */

public abstract class Command {
	protected CommandType type;
	protected protobuf.srl.commands.Commands.Marker.MarkerType markerType = null;
	protected UUID id = null;
	
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
	
	public protobuf.srl.commands.Commands.Marker.MarkerType getMarkerType() {
		return markerType;
	}
	
	/**
	 * for use in packaging up commands
	 * @return ByteString
	 */
	public abstract ByteString toByteString();
	
	/**
	 * execute singular command, intended to be called in sequence for every Update
	 * @param s PaleoSketch Sketch
	 */
	public abstract void execute(Sketch s);
	/**
	 * undo as singular command, intended to be called in sequence for every Update
	 * @param s PaleoSketch Sketch
	 */
	public abstract void undo(Sketch s);
	/**
	 * Returns ID
	 * @return UUID
	 */
	public UUID getId(){
		return id;
	}
}
