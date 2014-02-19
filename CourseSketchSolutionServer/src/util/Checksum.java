package util;

import java.util.ArrayList;
import java.util.UUID;

import protobuf.srl.commands.Commands.SrlUpdate;

public class Checksum {
	public static final long MOD_VALUE = 2L << 48L; // 2^32 - 1  which is 63 - 31 (where long is 2^63 -1 and int is 2 ^ 31 -1
	public static final long LONG_EXP = 64;
	public static final int MAX_LIST_SIZE = 2 << 16; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32
	public static final int MAX_COMMAND_SIZE = 2 << 12; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32

	/**
	 * Returns a long which is the checkSum of the update list.
	 *
	 * For identical list the checksum is guaranteed to return the same value.
	 *
	 * For any two list that are not the same. They should not appear the same (unless every item happened at the same instance and have the exact same id.
	 *
	 * The checksum is composed of the following in two steps
	 * The first step:
	 * <ul>
	 * <li>The first 16 bits is the list size.</li>
	 * <li>The following 48 bits is ·command.time % 2 ^ 48.</li>
	 * </ul>
	 * The second step:
	 * <ul>
	 * <li> get the UUID from the last update in the list. Split it into most significant bits and least significant bit</li>
	 * <li> perform the xor of the number in the first step and the most significant bits then the least significant bits.</li>
	 * </ul>
	 * @param list
	 * @return
	 */
	public static long checkSum(ArrayList<SrlUpdate> list) {
		int size = list.size() % MAX_LIST_SIZE;

		long totalTime = 0;
		UUID id = UUID.fromString(list.get(list.size() -1).getUpdateId());
		for (int i = 0; i <list.size(); i++) {
			totalTime  = (totalTime + list.get(i).getTime()) % MOD_VALUE;
		}

		int size_shift = 64 - Integer.SIZE + Integer.numberOfLeadingZeros(size);
		long result = ((long)size) << size_shift | totalTime;
		return result ^ id.getMostSignificantBits() ^ id.getLeastSignificantBits();
	}

	public static void main(String args[]) {
		int size = 65535;
		//System.out.println()
		long currentTime = System.currentTimeMillis();
		System.out.println("currentTime\t" + currentTime);
		long totalTime = (currentTime* 2 ) % MOD_VALUE;
		System.out.println("totalTime\t" + totalTime);
		long temp1 = ((long)size)<<(64 - log2(size));
		System.out.println("size by itself\t" + Long.toBinaryString(temp1));
		System.out.println("time by itself\t" + Long.toBinaryString(totalTime));
		System.out.println("currentTime B \t" + Long.toBinaryString(currentTime));
		System.out.println("currentTime B \t" + Long.toBinaryString(MOD_VALUE));
		long result = ((long)size)<<(64 - log2(size)) | totalTime;
		System.out.println("the total \t" + Long.toBinaryString(result));
		System.out.println("the total \t" + Long.toBinaryString(-1));
	}

	public static int log2(int value) {
	    return Integer.SIZE-Integer.numberOfLeadingZeros(value);
	}
}
