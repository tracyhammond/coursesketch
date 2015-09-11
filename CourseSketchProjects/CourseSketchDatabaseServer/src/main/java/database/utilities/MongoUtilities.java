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
     * Creates an id and throws a {@link DatabaseAccessException} if a valid one can not be created.
     *
     * An id is valid if it is not null and conforms to the ObjectId Format.  It does not mean the id actually exist.
     * @param objectId The id that a mongo id is wanted to be created out of.
     * @return {@link ObjectId} if it about to be created.
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    public static ObjectId createId(final String objectId) throws DatabaseAccessException {
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
