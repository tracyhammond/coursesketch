package database.utilities;

import database.DatabaseAccessException;
import org.bson.types.ObjectId;

/**
 * Created by gigemjt on 9/6/15.
 */
public class MongoUtilities {

    public static ObjectId createId(final String id) throws DatabaseAccessException {
        try {
            return new ObjectId(id);
        } catch(IllegalArgumentException e) {
            throw new DatabaseAccessException(e, false);
        }
    }
}
