package util;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.submission.Submission.SrlChecksum;

import java.util.ArrayList;
import java.util.List;

public class Checksum {
    public static final long MAX_TIME_SIZE = 2L << 63L - 2L; // 2 ^ 63 - 1 (or Long.maxValue)
    public static final long LONG_EXP = 64;
    public static final int MAX_LIST_SIZE_BITS = 16; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32
    public static final int MAX_LIST_SIZE =
            2 << MAX_LIST_SIZE_BITS; // max size a list can be in a checksum this is 2 ^ 24 (which is the midpoint between 16 and 32
    public static final int MAX_COMMAND_SIZE = 2 << 16; // max size that the command bytes can take up
    public static final long MAX_UPDATE_DATA_SIZE = 2L << 32L; // max size that the command bytes can take up

    private static final class SumHolder {
        public long totalTime = 0;
        public int totalCommand = 0;
        public long totalUpdateDataSize = 0;

        public void addUpdate(SrlUpdate update) {
            totalTime = (totalTime + update.getTime()) % MAX_TIME_SIZE;
            totalCommand = (totalCommand * 7 + (update.getCommands(0).getCommandType().getNumber() + 1)) % MAX_COMMAND_SIZE;
            totalUpdateDataSize = (totalUpdateDataSize * 7 + update.getSerializedSize()) % MAX_UPDATE_DATA_SIZE;
        }
    }

    /**
     * Returns a long which is the checkSum of the update list.
     *
     * For identical list the checksum is guaranteed to return the same value.
     *
     * For any two list that are not the same. They should not appear the same (unless every item happened at the same instance and have the exact
     * same id.
     *
     * The checksum is composed of the following in two steps
     * The first step:
     * <ul>
     * <li>The first 16 bits is the list size.</li>
     * <li>The following 48 bits is first command in an update % 2 ^ 48</li>
     * </ul>
     * The second step:
     * <ul>
     * <li>  update.time % 2 ^ 63 - 1
     * </ul>
     *
     * @param list
     * @return
     */
    public static SrlChecksum computeChecksum(final List<SrlUpdate> list) {
        final int size = list.size() % MAX_LIST_SIZE;
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            holder.addUpdate(list.get(i));
        }

        return computeSumFromHolder(holder, size);
    }

    private static SrlChecksum computeSumFromHolder(SumHolder holder, int size) {
        final int size_shift = 64 - Integer.SIZE + Integer.numberOfLeadingZeros(size);
        final int command_shift = 64 - MAX_LIST_SIZE_BITS - Integer.SIZE + Integer.numberOfLeadingZeros(holder.totalCommand);
        final long result = ((long) size) << size_shift | ((long) holder.totalCommand) << command_shift | holder.totalUpdateDataSize;
        final SrlChecksum.Builder builder = SrlChecksum.newBuilder();
        builder.setFirstBits(result);
        builder.setSecondBits(holder.totalTime);
        return builder.build();
    }

    /**
     * Creates a list of checksums for each point in the list.
     *
     * The check sums build on each other so you can use this to get the index of when a check summed matched.
     *
     * Not that the sum at zero is equal to the checksum of the item at index zero
     *
     * @param list
     * @return
     * @see Checksum#computeChecksum(List)
     */
    public static List<SrlChecksum> computeListedChecksum(final List<SrlUpdate> list) {
        ArrayList<SrlChecksum> listSummed = new ArrayList<SrlChecksum>();
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            int size = (i + 1) % MAX_LIST_SIZE;
            holder.addUpdate(list.get(i));

            listSummed.add(computeSumFromHolder(holder, size));
        }

        return listSummed;
    }

    /**
     * Returns the index of list1 where the two list diverge.
     *
     * The check sums build on each other so you can use this to get the index of when a check summed matched.
     *
     * Not that the sum at zero is equal to the checksum of the item at index zero
     *
     * @param list1
     *         the list of {@link SrlUpdate} that is used as the reference index.
     * @param list2
     *         the list of {@link protobuf.srl.commands.Commands.SrlUpdateList} the list used to as a comparison.
     * @return the index in list1 where there is a difference between checksums.  Returns -1 if there is no difference.
     * Returns -2 if the second list is smaller than the first list.
     * @see Checksum#computeChecksum(List)
     */
    public static int indexOfDifference(final List<SrlUpdate> list1, final List<SrlUpdate> list2) {
        // list
        if (list1.size() < list2.size()) {
            return -2;
        }
        final SumHolder holder1 = new SumHolder();
        final SumHolder holder2 = new SumHolder();
        for (int i = 0; i < list2.size(); i++) {
            int size = (i + 1) % MAX_LIST_SIZE;
            holder1.addUpdate(list1.get(i));
            holder2.addUpdate(list2.get(i));
            if (!computeSumFromHolder(holder1, size).equals(computeSumFromHolder(holder2, size))) {
                return i;
            }
        }
        if (list2.size() < list1.size()) {
            return list2.size();
        }
        return -1;
    }

    /**
     * TODO: make this more efficient!
     * Returns the index for the given checksum in the list of updates.
     *
     * This will return the last index of the item in this list.
     * EX:
     * If the checksum matches the list with one one item in it (index zero) then it will return 0
     * If the checksum matches the entire list then it will return list.size() - 1
     *
     * @param list
     * @param sum
     * @return the index if it is located or -1 if there is no match
     */
    public static int checksumIndex(final List<SrlUpdate> list, SrlChecksum sum) {
        List<SrlChecksum> sums = computeListedChecksum(list);
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            int size = (i + 1) % MAX_LIST_SIZE;
            holder.addUpdate(list.get(i));
            if (computeSumFromHolder(holder, size).equals(sum)) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String args[]) {
        int size = 65535;
        //System.out.println()
        long currentTime = System.currentTimeMillis();
        System.out.println("currentTime\t" + currentTime);
        long totalTime = (currentTime * 2) % MAX_TIME_SIZE;
        System.out.println("totalTime\t" + totalTime);
        long temp1 = ((long) size) << (64 - log2(size));
        System.out.println("size by itself\t" + Long.toBinaryString(temp1));
        System.out.println("time by itself\t" + Long.toBinaryString(totalTime));
        System.out.println("currentTime B \t" + Long.toBinaryString(currentTime));
        System.out.println("currentTime B \t" + Long.toBinaryString(MAX_TIME_SIZE));
        long result = ((long) size) << (64 - log2(size)) | totalTime;
        System.out.println("the total \t" + Long.toBinaryString(result));
        System.out.println("the total \t" + Long.toBinaryString(-1));
    }

    public static int log2(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }
}
