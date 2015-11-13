package com.coursesketch.test.utilities;

/**
 * Created by gigemjt on 9/5/15.
 */
public final class DatabaseHelper {
    /**
     * This can take a real object id and make it into one that probabilistically should not exist.
     * @param objectId The object id that we want to make invalid.
     * @return A string that should be a valid Id but should not point to anything in the database.
     */
    public static String createNonExistentObjectId(final String objectId) {
        return objectId.replaceAll("4", "7")
                .replaceAll("2", "4")
                .replaceAll("9", "2")
                .replaceAll("8", "9")
                .replaceAll("1", "8")
                .replaceAll("6", "1")
                .replaceAll("5", "6");
    }
}
