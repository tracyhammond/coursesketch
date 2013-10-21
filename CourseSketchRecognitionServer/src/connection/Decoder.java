package connection;

import protobuf.srl.action.Action.Command;
import protobuf.srl.action.Action.Update;
import protobuf.srl.action.commands.Commands.AddShape;
import protobuf.srl.action.commands.Commands.AddStroke;
import protobuf.srl.action.commands.Commands.PackageShape;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
public class Decoder {

	/**
	 * Returns an update object that is added to the list of updates for that particular sketch.
	 *
	 * These updates can be put into a list of updates that are stored for the entire history of the sketch.
	 */
	public static Update parseNextUpdate(ByteString string) {
		try {
			return Update.parseFrom(string);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a {@link GeneratedMessage} as it is parsed from the Command.
	 *
	 * Returns null if the ByteBuffer does not exist.
	 * @param buffer
	 * @return
	 */
	public static GeneratedMessage prarseCommand(Command command) {
		try {
			GeneratedMessage result = null;
			switch(command.getCommandType()) {
				case ADD_STROKE:
					result = AddStroke.parseFrom(command.getCommandData());
					break;
				case ADD_SHAPE:
					result = AddShape.parseFrom(command.getCommandData());
					break;
				case PACKAGE_SHAPE:
					result = PackageShape.parseFrom(command.getCommandData());
					break;
				// etc...
			}
			return result;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
}
