package coursesketch.database.util;

import protobuf.srl.utils.Util;

/**
 * A set of utility methods to make it easier for dealing with school items.
 *
 * Created by gigemjt on 9/16/2015.
 */
public final class DbSchoolUtility {

    /**
     * Empty constructor.
     */
    private DbSchoolUtility() {
    }

    /**
     * Returns a string representing the type of school item it is based on the enum.
     *
     * This method is used to make it easier to handle upgrades or changes to protobuf names.
     * @param type {@link Util.ItemType}.
     * @return A string representing the ItemType.
     */
    public static String getCollectionFromType(final Util.ItemType type) {
        return type.name();
    }

    /**
     * Returns the {@code ItemType} that created the given {@code ItemType}.
     *
     * A course item type returns itself.
     * A bank problem also returns itself.
     * @param item {@link Util.ItemType}.
     * @return The item type that is supposed to be the parent.
     */
    public static Util.ItemType getParentItemType(final Util.ItemType item) {
        switch (item) {
            case COURSE: return Util.ItemType.COURSE;
            case ASSIGNMENT: return Util.ItemType.COURSE;
            case COURSE_PROBLEM: return Util.ItemType.ASSIGNMENT;
            case BANK_PROBLEM: return Util.ItemType.BANK_PROBLEM;
            default: return null;
        }
    }

}
