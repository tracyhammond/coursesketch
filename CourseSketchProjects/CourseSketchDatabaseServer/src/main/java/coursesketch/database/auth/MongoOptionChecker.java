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

    private final DB database;

    public MongoOptionChecker(final ServerInfo info) {
        final MongoClient mongoClient = new MongoClient(info.getDatabaseUrl());
        database = mongoClient.getDB(info.getDatabaseName());
    }

    public MongoOptionChecker(final DB database) {
        this.database = database;
    }

    @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime) {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        return false;
    }

    @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        return false;
    }

    @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        final DBObject result = (DBObject) dataCreator.getDatabaseResult();
        final boolean published = result.get(DatabaseStringConstants.STATE_PUBLISHED);
        return false;
    }

    /**
     * @param collectionType The type of collection that is being checked.
     * @param itemId The id of the tiem that is being checked.
     * @return a data creator that grabs the data for any other uses by the option checker.
     */
    @Override public AuthenticationDataCreator createDataGrabber(final School.ItemType collectionType, final String itemId) {
        final String collectionName = DbSchoolUtility.getCollectionFromType(collectionType, true);
        final DBObject result = database.getCollection(collectionName).findOne(new ObjectId(itemId));
        return new AuthenticationDataCreator() {

            @Override public Object getDatabaseResult() {
                return result;
            }
        };
    }
}
