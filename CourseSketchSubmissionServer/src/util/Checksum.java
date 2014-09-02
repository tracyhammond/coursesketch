package util;

import java.util.ArrayList;
import java.util.List;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.submission.Submission.SrlChecksum;

public class Checksum {
	public static final long MAX_TIME_SIZE = 2L << 63L - 2L; // 2 ^ 63 - 1 (or Long.maxValue)
	public static final long LONG_EXP = 64;
	public static final int MAX_LIST_SIZE_BITS = 16; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32
	public static final int MAX_LIST_SIZE = 2 << MAX_LIST_SIZE_BITS; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32
	public static final int MAX_COMMAND_SIZE = 2 << 16; // max size that the command bytes can take up
	public static final long MAX_UPDATE_DATA_SIZE = 2L << 32L; // max size that the command bytes can take up

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
	 * <li>The following 48 bits is �first command in an update % 2 ^ 48</li>
	 * </ul>
	 * The second step:
	 * <ul>
	 * <li> � update.time % 2 ^ 63 - 1
	 * </ul>
	 * @param list
	 * @return
	 */
	public static SrlChecksum computeChecksum(final List<SrlUpdate> list) {
		final int size = list.size() % MAX_LIST_SIZE;
		long totalTime = 0;
		int totalCommand = 0;
		long totalUpdateDataSize = 0;
		for (int i = 0; i <list.size(); i++) {
			totalTime  = (totalTime + list.get(i).getTime()) % MAX_TIME_SIZE;
			totalCommand = (totalCommand + (list.get(i).getCommands(0).getCommandType().getNumber() + 1)) % MAX_COMMAND_SIZE;
			totalUpdateDataSize = (totalUpdateDataSize + list.get(i).getSerializedSize()) % MAX_UPDATE_DATA_SIZE;
		}

		final int size_shift = 64 - Integer.SIZE + Integer.numberOfLeadingZeros(size);
		final int command_shift = 64 - MAX_LIST_SIZE_BITS - Integer.SIZE + Integer.numberOfLeadingZeros(totalCommand);
		final long result = ((long)size) << size_shift | ((long)totalCommand) << command_shift | totalUpdateDataSize;
		final SrlChecksum.Builder builder = SrlChecksum.newBuilder();
		builder.setFirstBits(result);
		builder.setSecondBits(totalTime);
		return builder.build();
	}

	/**
	 * Creates a list of checksums for each point in the list.
	 *
	 * The check sums build on each other so you can use this to get the index of when a check summed matched.
	 *
	 * Not that the sum at zero is equal to the checksum of the item at index zero
	 * @see Checksum#computeChecksum(List)
	 * @param list
	 * @return
	 */
	public static List<SrlChecksum> computeListedChecksum(final List<SrlUpdate> list) {
		ArrayList<SrlChecksum> listSummed = new ArrayList<SrlChecksum>();
		long totalTime = 0;
		int totalCommand = 0;
		long totalUpdateDataSize = 0;
		for (int i = 0; i <list.size(); i++) {
			int size = (i + 1) % MAX_LIST_SIZE;
			totalTime  = (totalTime + list.get(i).getTime()) % MAX_TIME_SIZE;
			totalCommand = (totalCommand + (list.get(i).getCommands(0).getCommandType().getNumber() + 1)) % MAX_COMMAND_SIZE;
			totalUpdateDataSize = (totalUpdateDataSize + list.get(i).getSerializedSize()) % MAX_UPDATE_DATA_SIZE;

			int size_shift = 64 - Integer.SIZE + Integer.numberOfLeadingZeros(size);
			int command_shift = 64 - MAX_LIST_SIZE_BITS - Integer.SIZE + Integer.numberOfLeadingZeros(totalCommand);
			long result = ((long)size) << size_shift | ((long)totalCommand) << command_shift | totalUpdateDataSize;
			SrlChecksum.Builder builder = SrlChecksum.newBuilder();
			builder.setFirstBits(result);
			builder.setSecondBits(totalTime);
			listSummed.add(builder.build());
		}

		return listSummed;
	}

	public static void main(String args[]) {
		int size = 65535;
		//System.out.println()
		long currentTime = System.currentTimeMillis();
		System.out.println("currentTime\t" + currentTime);
		long totalTime = (currentTime* 2 ) % MAX_TIME_SIZE;
		System.out.println("totalTime\t" + totalTime);
		long temp1 = ((long)size)<<(64 - log2(size));
		System.out.println("size by itself\t" + Long.toBinaryString(temp1));
		System.out.println("time by itself\t" + Long.toBinaryString(totalTime));
		System.out.println("currentTime B \t" + Long.toBinaryString(currentTime));
		System.out.println("currentTime B \t" + Long.toBinaryString(MAX_TIME_SIZE));
		long result = ((long)size)<<(64 - log2(size)) | totalTime;
		System.out.println("the total \t" + Long.toBinaryString(result));
		System.out.println("the total \t" + Long.toBinaryString(-1));
	}

	public static int log2(int value) {
	    return Integer.SIZE-Integer.numberOfLeadingZeros(value);
	}
}
