package coursesketch.database.util;

import org.bson.Document;
import org.bson.types.ObjectId;
import protobuf.srl.utils.Util;

import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;

/**
 * Contains various Utilities for working with mongo databases.
 * Created by gigemjt on 9/6/15.
 */
public final class MongoUtilities {

    /**
     * Empty constructor.
     */
    private MongoUtilities() {
    }

    /**
     * Tries to convert a string into a mongo ObjectId.
     *
     * Throws a {@link DatabaseAccessException} if a valid id can not be created.
     * An id is valid if it is not null and conforms to the ObjectId Format.  It does not mean the id actually exist.
     * @param objectId The string id that we want convert to a mongo ObjectId.
     * @return {@link ObjectId} if it is successfully created.
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    public static Document convertStringToObjectId(final String objectId) throws DatabaseAccessException {
        if (objectId == null) {
            throw new DatabaseAccessException(new IllegalArgumentException("Object Id was given a null parameter"), false);
        }
        try {
            return new Document(SELF_ID, new ObjectId(objectId.trim()));
        } catch (IllegalArgumentException e) {
            throw new DatabaseAccessException(e, false);
        }
    }

    /**
     * Creates the document for a domain id.
     *
     * @param domainId The domain id to be converted into recognition data.
     * @return {@link Document} containing a domainId
     */
    public static Document createDomainId(final Util.DomainId domainId) {
        return new Document();
    }

    /**
     * Gets the user group from the database.
     *
     * @param databaseResult The result from the database
     * @return The list of users.
     */
    public static List<String> getUserGroup(final Document databaseResult) {
        return (List<String>) databaseResult.get(DatabaseStringConstants.USER_LIST);
    }
}
