package coursesketch.database.auth;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.DbSchoolUtility;
import org.bson.types.ObjectId;
import protobuf.srl.school.School;

/**
 * Checks different data for
 * Created by gigemjt on 9/4/15.
 */
@SuppressWarnings("PMD.CommentRequired")
public final class MongoOptionChecker implements AuthenticationOptionChecker {

    /**
     * The database that needs to look for the option checker.
     */
    private final DB database;

    /**
     * Creates a {@link MongoOptionChecker} with {@link ServerInfo}.
     *
     * @param info
     *         The location at where to find the database.
     */
    public MongoOptionChecker(final ServerInfo info) {
        final MongoClient mongoClient = new MongoClient(info.getDatabaseUrl());
        database = mongoClient.getDB(info.getDatabaseName());
    }

    /**
     * Creates a {@link MongoOptionChecker} with {@link DB}.
     *
     * @param database
     *         The database that contains information that needs to be checked for the mongo option checker.
     */
    public MongoOptionChecker(final DB database) {
        this.database = database;
    }

    @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime) throws DatabaseAccessException {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        if (!result.containsField(DatabaseStringConstants.ACCESS_DATE)) {
            throw new DatabaseAccessException("DBObject does not contain value for key " + DatabaseStringConstants.ACCESS_DATE);
        }
        if (!result.containsField(DatabaseStringConstants.CLOSE_DATE)) {
            throw new DatabaseAccessException("DBObject does not contain value for key " + DatabaseStringConstants.CLOSE_DATE);
        }
        final long accessDate = (long) result.get(DatabaseStringConstants.ACCESS_DATE);
        final long closeDate = (long) result.get(DatabaseStringConstants.CLOSE_DATE);
        return accessDate <= checkTime && checkTime <= closeDate;
    }

    @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        final Object access = result.get(DatabaseStringConstants.COURSE_ACCESS);
        if (access == null) {
            throw new DatabaseAccessException("DBObject does not contain value for key " + DatabaseStringConstants.COURSE_ACCESS);
        }
        final School.SrlCourse.Accessibility accessValue = School.SrlCourse.Accessibility.valueOf((int) access);
        return !(accessValue == School.SrlCourse.Accessibility.PUBLIC || accessValue == School.SrlCourse.Accessibility.SUPER_PUBLIC);
    }

    @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        final Object published = result.get(DatabaseStringConstants.STATE_PUBLISHED);
        if (published == null) {
            throw new DatabaseAccessException("DBObject does not contain value for key " + DatabaseStringConstants.STATE_PUBLISHED);
        }
        return (boolean) published;
    }

    /**
     * @param collectionType
     *         The type of collection that is being checked.
     * @param itemId
     *         The id of the tiem that is being checked.
     * @return a data creator that grabs the data for any other uses by the option checker.
     */
    @Override public AuthenticationDataCreator createDataGrabber(final School.ItemType collectionType, final String itemId)
            throws DatabaseAccessException {
        final String collectionName = DbSchoolUtility.getCollectionFromType(collectionType, true);
        final DBObject result = database.getCollection(collectionName).findOne(new ObjectId(itemId));
        if (result == null) {
            throw new DatabaseAccessException(DbSchoolUtility.getCollectionFromType(collectionType) + " Was not found in " + collectionName
                    + " with id " + itemId);
        }
        return new AuthenticationDataCreator() {
            /**
             * @return {@link DBObject} The value that the database found for authentication.
             */
            @Override public Object getDatabaseResult() {
                return result;
            }
        };
    }
}
