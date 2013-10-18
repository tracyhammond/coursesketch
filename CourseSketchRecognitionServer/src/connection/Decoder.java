package connection;

import java.nio.ByteBuffer;

import protobuf.srl.action.Action.Command;
import protobuf.srl.request.Message.Request;

import com.google.protobuf.InvalidProtocolBufferException;
public class Decoder {

	/**
	 * Returns a {@link Request} as it is parsed from the ByteBuffer.
	 *
	 * Returns null if the ByteBuffer does not exist.
	 * @param buffer
	 * @return
	 */
	public static Command prarsecommand(ByteBuffer buffer) {
		try {
			return Command.parseFrom(buffer.array());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
}
