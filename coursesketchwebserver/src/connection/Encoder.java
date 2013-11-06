package connection;

import java.nio.ByteBuffer;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
public class Encoder {

	/**
	 * Returns a {@link Request} as it is parsed from the ByteBuffer.
	 *
	 * Returns null if the ByteBuffer does not exist.
	 * @param buffer
	 * @return
	 */
	public static Request requestIDBuilder(Request req, String sessionInfo) {
		Request.Builder breq = Request.newBuilder();
		breq.mergeFrom(req);
		breq.setSessionInfo(sessionInfo);
		return breq.build();
	}
}
