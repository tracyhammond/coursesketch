package connection;

import java.nio.ByteBuffer;

import protobuf.srl.request.Message.Request;
import protobuf.srl.sketch.Sketch;
import protobuf.srl.sketch.Sketch.SRL_Sketch;

import com.google.protobuf.InvalidProtocolBufferException;
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
			e.printStackTrace();
			return null;
		}
	}
	public static SRL_Sketch parseSketch(com.google.protobuf.ByteString buffer) {
		try {
			return Sketch.SRL_Sketch.parseFrom(buffer);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
}
