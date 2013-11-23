package connection;

import java.nio.ByteBuffer;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
public class Decoder {

	/**
	 * Returns a {@link Request} as it is parsed from the ByteBuffer.
	 *
	 * Returns null if the ByteBuffer does not exist.
	 * @param buffer
	 * @return
	 */
	public static Request parseRequest(ByteBuffer buffer) {
		try {
			return Request.parseFrom(buffer.array());
		} catch (InvalidProtocolBufferException e) {
			//e.printStackTrace();
			return null;
		}
	}
}
