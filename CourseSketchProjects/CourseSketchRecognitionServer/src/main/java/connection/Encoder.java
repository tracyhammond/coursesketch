package connection;

import java.util.UUID;

import com.google.protobuf.ByteString;

import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;

public class Encoder {
	/**
	 * counter will be incremented by 0x10000 for each new SComponent that is
	 * created counter is used as the most significant bits of the UUID
	 * 
	 * initialized to 0x4000 (the version -- 4: randomly generated UUID) along
	 * with 3 bytes of randomness: Math.random()*0x1000 (0x0 - 0xFFF)
	 * 
	 * the randomness further reduces the chances of collision between multiple
	 * sketches created on multiple computers simultaneously
	 * 
	 * (taken from SCComponent)
	 */
	public static long counter = 0x4000L | (long) (Math.random() * 0x1000);

	public static SrlCommand createCommandFromBytes(ByteString date, CommandType type) {
		SrlCommand.Builder cmdBuilder = SrlCommand.newBuilder();
		cmdBuilder.setIsUserCreated(false);
		cmdBuilder.setCommandType(type);
		cmdBuilder.setCommandData(date);
		return cmdBuilder.build();
	}

	/**
	 * Given a list of commands create an update.
	 */
	public static SrlUpdate createUpdateFromCommands(SrlCommand... coms) {
		SrlUpdate.Builder updateBuilder = SrlUpdate.newBuilder();
		for(SrlCommand com: coms) {
			updateBuilder.addCommands(com);
		}
		updateBuilder.setTime(System.currentTimeMillis());
		updateBuilder.setUpdateId(nextID().toString());
		return updateBuilder.build();
	}

	/**
	 * Given a list of commands create a request.
	 */
	public static Request createRequestFromCommands(String sessionInfo, SrlCommand ... coms) {
		SrlUpdate up = createUpdateFromCommands(coms);
		return createRequestFromUpdate(sessionInfo, up);
	}

	public static Request createRequestFromUpdate(String sessionInfo, SrlUpdate up) {
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setOtherData(up.toByteString());
		requestBuilder.setRequestType(MessageType.RECOGNITION);
		requestBuilder.setSessionInfo(sessionInfo);
		return requestBuilder.build();
	}

	public static UUID nextID() {
		counter += 0x10000L; // Overflow is perfectly fine.
		return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
	}
}