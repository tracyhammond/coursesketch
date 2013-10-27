package connection;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.Update;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;

public class Encoder {
	public static Request createRequestFromStroke(SrlStroke stroke) {
		Command com = createCommandFromBytes(stroke.toByteString(), CommandType.ADD_STROKE);
		Update date = createUpdateFromCommand(com);
		return createRequestFromUpdate(date);
	}

	public static Request createRequestFromShape(SrlShape shape) {
		Command com = createCommandFromBytes(shape.toByteString(), CommandType.ADD_SHAPE);
		Update date = createUpdateFromCommand(com);
		return createRequestFromUpdate(date);
	}

	public static Command createCommandFromBytes(ByteString date, CommandType type) {
		Command.Builder cmdBuilder = Command.newBuilder();
		cmdBuilder.setIsUserCreated(false);
		cmdBuilder.setCommandType(type);
		cmdBuilder.setCommandData(date);
		return cmdBuilder.build();
	}

	public static Update createUpdateFromCommand(Command com) {
		Update.Builder updateBuilder = Update.newBuilder();
		updateBuilder.addCommands(com);
		return updateBuilder.build();
	}

	public static Request createRequestFromUpdate(Update up) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setOtherData(up.toByteString());
		requestBuilder.setRequestType(MessageType.RECOGNITION);
		return requestBuilder.build();
	}
}