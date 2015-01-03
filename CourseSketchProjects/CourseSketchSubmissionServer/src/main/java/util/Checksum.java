package util;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.submission.Submission.SrlChecksum;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the checksum for the {@link protobuf.srl.commands.Commands.SrlUpdate}.
 *
 * This can be used to ensure the integrity of the list and possibly for other uses as well.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class Checksum {

    /**
     * The largest value for the time portion. before it goes back to zero.
     */
    public static final long MAX_TIME_SIZE = 2L << 63L - 2L; // (2 ^ 63) - 2 (or Long.maxValue)

    /**
     * How many digits are in a long.
     */
    public static final long LONG_EXP = 64;

    /**
     * The max number of bits that can be used to represent the size of the list.
     * max size a list can be in a checksum this is 2 ^ 16
     */
    public static final int MAX_LIST_SIZE_BITS = 16;
    /**
     * The max value that is represented by the size of the list.
     * max size a list can be in a checksum this is 2 ^ 16
     */
    public static final int MAX_LIST_SIZE = 2 << MAX_LIST_SIZE_BITS;

    /**
     * max size that the command bytes can take up.
     */
    public static final int MAX_COMMAND_SIZE = 2 << 16;

    /**
     * Max size that the command bytes can take up.
     */
    public static final long MAX_UPDATE_DATA_SIZE = 2L << 32L;

    /**
     * The return value to indicate that the lists are of incorrect lengths and as a result can not be compared.
     */
    public static final int WRONG_LIST_SIZE_ERROR = -2;

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
     *         The list of {@link protobuf.srl.commands.Commands.SrlUpdate}.
     * @return {@link protobuf.srl.submission.Submission.SrlChecksum} the final value.
     */
    public static SrlChecksum computeChecksum(final List<SrlUpdate> list) {
        final int size = list.size() % MAX_LIST_SIZE;
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            holder.addUpdate(list.get(i));
        }

        return computeSumFromHolder(holder, size);
    }

    /**
     * Computes the {@link protobuf.srl.submission.Submission.SrlChecksum} given the holder and the size.
     *
     * @param holder
     *         The holder of the partially computed checksum values.
     * @param size
     *         the size of the list at the current point.
     * @return The final checksum value.
     */
    private static SrlChecksum computeSumFromHolder(final SumHolder holder, final int size) {
        final int sizeShift = (int) LONG_EXP - Integer.SIZE + Integer.numberOfLeadingZeros(size);
        final int commandShift = (int) LONG_EXP - MAX_LIST_SIZE_BITS - Integer.SIZE + Integer.numberOfLeadingZeros(holder.totalCommand);
        final long result = ((long) size) << sizeShift | ((long) holder.totalCommand) << commandShift | holder.totalUpdateDataSize;
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
     *         The list of updates to compute the checksum.
     * @return A list of checksums that matches the checksum at each point.
     * @see Checksum#computeChecksum(List)
     */
    public static List<SrlChecksum> computeListedChecksum(final List<SrlUpdate> list) {
        final ArrayList<SrlChecksum> listSummed = new ArrayList<>();
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            final int size = (i + 1) % MAX_LIST_SIZE;
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
     * Returns {@link #WRONG_LIST_SIZE_ERROR} if the second list is smaller than the first list.
     * @see Checksum#computeChecksum(List)
     */
    public static int indexOfDifference(final List<SrlUpdate> list1, final List<SrlUpdate> list2) {
        // list
        if (list2.size() < list1.size()) {
            return WRONG_LIST_SIZE_ERROR;
        }
        final SumHolder holder1 = new SumHolder();
        final SumHolder holder2 = new SumHolder();
        for (int i = 0; i < list1.size(); i++) {
            final int size = (i + 1) % MAX_LIST_SIZE;
            holder1.addUpdate(list1.get(i));
            holder2.addUpdate(list2.get(i));
            if (!computeSumFromHolder(holder1, size).equals(computeSumFromHolder(holder2, size))) {
                return i;
            }
        }
        if (list1.size() < list2.size()) {
            return list1.size();
        }
        return -1;
    }

    /**
     * Returns the index for the given checksum in the list of updates.
     *
     * This will return the last index of the item in this list.
     * EX:
     * If the checksum matches the list with one one item in it (index zero) then it will return 0
     * If the checksum matches the entire list then it will return list.size() - 1
     *
     * @param list
     *         The list of updates.
     * @param sum
     *         The sum that is being compared to.
     * @return the index if it is located or -1 if there is no match
     */
    public static int checksumIndex(final List<SrlUpdate> list, final SrlChecksum sum) {
        final SumHolder holder = new SumHolder();
        for (int i = 0; i < list.size(); i++) {
            final int size = (i + 1) % MAX_LIST_SIZE;
            holder.addUpdate(list.get(i));
            if (computeSumFromHolder(holder, size).equals(sum)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Holds the partial sum used in computing the checksum values.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private static final class SumHolder {
        /**
         * The cumulative time of each update.
         * It is computed as ( totalTime + newTime ) % {@link #MAX_TIME_SIZE}.
         *
         * Useful for telling the difference between two updates that are the exactly the same besides the time they took place
         */
        public long totalTime = 0;

        /**
         * The cumulative value of each command type
         * It is computed as ( totalCommand * 7 + newCommandValue ) % {@link #MAX_TIME_SIZE}.
         *
         * This takes the command type number for the value.
         * This is useful for integrity checking ensuring that each list has the same value in it.
         * This only looks at the first command of the {@link protobuf.srl.commands.Commands.SrlUpdate}.
         */
        public int totalCommand = 0;

        /**
         * The cumulative value of the binary data
         * It is computed as ( totalCommand * 7 + newDataSize ) % {@link #MAX_UPDATE_DATA_SIZE}.
         *
         * This is a shortcut of ensuring that the bytes of the list are the same without checking every single byte.
         * This can be used to tell the difference between two strokes for example where everything else is the same.
         * Or two updates when the first command is the same.
         */
        public long totalUpdateDataSize = 0;

        /**
         * Adds a new {@link protobuf.srl.commands.Commands.SrlUpdate} to the current checksum and compuytes values for it.
         *
         * @param update
         *         The new update that is being added to the checksum.
         */
        public void addUpdate(final SrlUpdate update) {
            totalTime = (totalTime + update.getTime()) % MAX_TIME_SIZE;
            totalCommand = (totalCommand * 7 + (update.getCommands(0).getCommandType().getNumber() + 1)) % MAX_COMMAND_SIZE;
            totalUpdateDataSize = (totalUpdateDataSize * 7 + update.getSerializedSize()) % MAX_UPDATE_DATA_SIZE;
        }
    }
}
