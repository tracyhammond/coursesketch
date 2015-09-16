package database;

import protobuf.srl.school.School;

/**
 * Created by gigemjt on 9/16/2015.
 */
public final class DbSchoolUtility {
    public static String getCollectionFromType(School.ItemType type) {
        return type.name();
    }

    public static String getCollectionFromType(School.ItemType type, boolean legacy) {
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
     * <p/>
     * A course item type returns itself.
     * A bank problem also returns itself.
     * @param item
     * @return The item type that is supposed to be the parent.
     */
    public static School.ItemType getParentItemType(School.ItemType item) {
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
