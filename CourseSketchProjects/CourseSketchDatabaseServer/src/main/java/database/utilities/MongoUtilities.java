package database.utilities;

import database.DatabaseAccessException;
import org.bson.types.ObjectId;

/**
 * Contains various Utilities for working with mongo databases.
 * Created by gigemjt on 9/6/15.
 */
public final class MongoUtilities {

    /**
     * Empty constructor.
     */
    private MongoUtilities() { }

    /**
     * Tries to convert a string into a mongo ObjectId.
     *
     * Throws a {@link DatabaseAccessException} if a valid id can not be created.
     * An id is valid if it is not null and conforms to the ObjectId Format.  It does not mean the id actually exist.
     * @param objectId The string id that we want convert to a mongo ObjectId.
     * @return {@link ObjectId} if it is successfully created.
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    public static ObjectId convertStringToObjectId(final String objectId) throws DatabaseAccessException {
        if (objectId == null) {
            throw new DatabaseAccessException(new IllegalArgumentException("Object Id was given a null parameter"), false);
        }
        try {
            return new ObjectId(objectId.trim());
        } catch (IllegalArgumentException e) {
            throw new DatabaseAccessException(e, false);
        }
    }
}
