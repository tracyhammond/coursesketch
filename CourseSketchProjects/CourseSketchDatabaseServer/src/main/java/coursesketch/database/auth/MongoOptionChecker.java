package coursesketch.database.auth;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import coursesketch.server.interfaces.ServerInfo;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.DbSchoolUtility;
import org.bson.Document;
import protobuf.srl.utils.Util;

import static coursesketch.database.util.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Checks the coursesketch.util.util for auxiliary permission data.
 *
 * For example this will be used for checking the dates or if the item requires registration but does not check if there is a user or an admin.
 * Created by gigemjt on 9/4/15.
 */
@SuppressWarnings("PMD.CommentRequired")
public final class MongoOptionChecker implements AuthenticationOptionChecker {

    /**
     * The message that is returned when a field is missing while checking for authentication.
     */
    private static final String MISSING_OBJECT_MESSAGE = "Document does not contain value for key ";

    /**
     * The coursesketch.util.util that needs to look for the option checker.
     */
    private final MongoDatabase database;

    /**
     * Creates a {@link MongoOptionChecker} with {@link ServerInfo}.
     *
     * @param info
     *         The location at where to find the coursesketch.util.util.
     */
    public MongoOptionChecker(final ServerInfo info) {
        final MongoClient mongoClient = new MongoClient(info.getDatabaseUrl());
        database = mongoClient.getDatabase(info.getDatabaseName());
    }

    /**
     * Creates a {@link MongoOptionChecker} with {@link MongoDatabase}.
     *
     * @param database
     *         The coursesketch.util.util that contains information that needs to be checked for the mongo option checker.
     */
    public MongoOptionChecker(final MongoDatabase database) {
        this.database = database;
    }

    @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime) throws DatabaseAccessException {
        final Document result = (Document) dataCreator.getDatabaseResult();
        if (!result.containsKey(DatabaseStringConstants.ACCESS_DATE)) {
            throw new DatabaseAccessException(MISSING_OBJECT_MESSAGE + DatabaseStringConstants.ACCESS_DATE);
        }
        if (!result.containsKey(DatabaseStringConstants.CLOSE_DATE)) {
            throw new DatabaseAccessException(MISSING_OBJECT_MESSAGE + DatabaseStringConstants.CLOSE_DATE);
        }
        final long accessDate = (long) result.get(DatabaseStringConstants.ACCESS_DATE);
        final long closeDate = (long) result.get(DatabaseStringConstants.CLOSE_DATE);
        return accessDate <= checkTime && checkTime <= closeDate;
    }

    @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final Document result = (Document) dataCreator.getDatabaseResult();
        final Object access = result.get(DatabaseStringConstants.COURSE_ACCESS);
        if (access == null) {
            throw new DatabaseAccessException(MISSING_OBJECT_MESSAGE + DatabaseStringConstants.COURSE_ACCESS);
        }
        final Util.Accessibility accessValue = Util.Accessibility.valueOf((int) access);
        return !(accessValue == Util.Accessibility.PUBLIC || accessValue == Util.Accessibility.SUPER_PUBLIC);
    }

    @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final Document result = (Document) dataCreator.getDatabaseResult();
        final Object published = result.get(DatabaseStringConstants.STATE_PUBLISHED);
        if (published == null) {
            throw new DatabaseAccessException(MISSING_OBJECT_MESSAGE + DatabaseStringConstants.STATE_PUBLISHED);
        }
        return (boolean) published;
    }

    /**
     * {@inheritDoc}
     * @return a new instance of data creator that interfaces with mongo and grabs the data for any other uses by the option checker.
     */
    @Override public AuthenticationDataCreator createDataGrabber(final Util.ItemType collectionType, final String itemId)
            throws DatabaseAccessException {
        final String collectionName = DbSchoolUtility.getCollectionFromType(collectionType);
        final Document result = database.getCollection(collectionName).find(convertStringToObjectId(itemId)).first();
        if (result == null) {
            throw new DatabaseAccessException(DbSchoolUtility.getCollectionFromType(collectionType) + " Was not found in " + collectionName
                    + " with id " + itemId);
        }
        return new AuthenticationDataCreator() {
            /**
             * @return {@link Document} The value that the coursesketch.util.util found for authentication.
             */
            @Override public Object getDatabaseResult() {
                return result;
            }
        };
    }
}
