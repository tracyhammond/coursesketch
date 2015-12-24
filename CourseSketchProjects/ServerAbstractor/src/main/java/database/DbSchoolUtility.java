package database;

import protobuf.srl.school.School;

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
     * @param type {@link protobuf.srl.school.School.ItemType}.
     * @return A string representing the ItemType.
     */
    public static String getCollectionFromType(final School.ItemType type) {
        return type.name();
    }

    /**
     * Returns a string representing the type of school item it is based on the enum.
     *
     * This method is used to make it easier to handle upgrades or changes to protobuf names.
     * @param type {@link protobuf.srl.school.School.ItemType}.
     * @param legacy true if the legacy names should be used instead of the new names.
     * @return A string representing the ItemType.
     */
    public static String getCollectionFromType(final School.ItemType type, final boolean legacy) {
        if (!legacy) {
            return getCollectionFromType(type);
        }
        switch (type) {
            case COURSE: return DatabaseStringConstants.COURSE_COLLECTION;
            case ASSIGNMENT: return DatabaseStringConstants.ASSIGNMENT_COLLECTION;
            case COURSE_PROBLEM: return DatabaseStringConstants.COURSE_PROBLEM_COLLECTION;
            case BANK_PROBLEM: return DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
            case LECTURE: return DatabaseStringConstants.LECTURE_COLLECTION;
            default: return "NO_COLLECTION";
        }
    }

    /**
     * Returns the {@code ItemType} that created the given {@code ItemType}.
     *
     * A course item type returns itself.
     * A bank problem also returns itself.
     * @param item {@link protobuf.srl.school.School.ItemType}.
     * @return The item type that is supposed to be the parent.
     */
    public static School.ItemType getParentItemType(final School.ItemType item) {
        switch (item) {
            case COURSE: return School.ItemType.COURSE;
            case ASSIGNMENT: return School.ItemType.COURSE;
            case COURSE_PROBLEM: return School.ItemType.ASSIGNMENT;
            case BANK_PROBLEM: return School.ItemType.BANK_PROBLEM;
            case LECTURE: return School.ItemType.COURSE;
            default: return null;
        }
    }

}
