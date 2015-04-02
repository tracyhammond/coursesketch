package connection;

import java.nio.ByteBuffer;

import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.request.Message.Request;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.sketch.Sketch.SrlSketch;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class Decoder {

	/**
	 * Returns an update object that is added to the list of updates for that particular sketch.
	 *
	 * These updates can be put into a list of updates that are stored for the entire history of the sketch.
	 */
	public static SrlUpdate parseNextUpdate(ByteString string) {
		try {
			return SrlUpdate.parseFrom(string);
		} catch (InvalidProtocolBufferException e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	public static Request parseRequest(ByteBuffer buffer) {
		try {
			return Request.parseFrom(buffer.array());
		} catch (InvalidProtocolBufferException e) {
			//e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a {@link GeneratedMessage} as it is parsed from the Command.
	 *
	 * Returns null if the ByteBuffer does not exist.
	 * @param buffer
	 * @return
	 */
	public static GeneratedMessage prarseCommand(SrlCommand command) {
		try {
			GeneratedMessage result = null;
			switch(command.getCommandType()) {
				case ADD_STROKE:
					//result = AddStroke.parseFrom(command.getCommandData());
					//break;
				case ADD_SHAPE:
					//result = AddShape.parseFrom(command.getCommandData());
					//break;
				case PACKAGE_SHAPE:
					result = ActionPackageShape.parseFrom(command.getCommandData());
					break;
				// etc...
			default:
				break;
			}
			return result;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static SrlSketch parseSketch(com.google.protobuf.ByteString buffer) {
		try {
			return Sketch.SrlSketch.parseFrom(buffer);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
}