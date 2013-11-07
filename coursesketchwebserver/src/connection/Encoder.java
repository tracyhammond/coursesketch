package connection;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.request.Message.Request;
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
	
	public static UUID nextID() {
		counter += 0x10000L; // Overflow is perfectly fine.
		return new UUID(counter, System.nanoTime() | 0x8000000000000000L);
	}
}
