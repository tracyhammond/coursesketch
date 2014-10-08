package database.institution.mongo;

import static database.DatabaseStringConstants.CLASSIFICATION;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.TIME;
import static database.DatabaseStringConstants.UPDATE;
import static database.DatabaseStringConstants.UPDATEID;
import static database.DatabaseStringConstants.UPDATE_COLLECTION;

import java.util.NoSuchElementException;

import org.bson.BasicBSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;

/**
 * Manages updates for mongo.
 * @author gigemjt
 */
public final class UpdateManager {

    /**
     * Private constructor.
     *
     */
    private UpdateManager() {
    }

    /**
     * Inserts a new update into the database for a specific user.
     * @param dbs the database where the update is being stored.
     * @param userId The user that the update is being added to.
     * @param id the id of the new update
     * @param time the time that the update was created.
     * @param classification the classification of the update. (course, assignment, ...)
     * @throws AuthenticationException Thrown if the user does not have access to the update.
     * @throws DatabaseAccessException Thrown if there is data missing.
     */
    public static void mongoInsertUpdate(final DB dbs, final String userId, final String id, final long time, final String classification)
            throws AuthenticationException, DatabaseAccessException {
        // pull the item and delete it from the list
        DBCollection users = dbs.getCollection(UPDATE_COLLECTION);
        BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID,
                id)));
        final DBObject corsor = retrieveUpdate(dbs, userId);
        users.update(corsor, query);

        // push the new file onto the classification
        users = dbs.getCollection(UPDATE_COLLECTION);
        query = new BasicDBObject("$push", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, id).append(
                TIME, time)));
        users.update(corsor, query);
    }

    /**
     * Returns all updates that are after a certain time.
     *
     * if the given time is <= 0 than the entire list is returned. If there are
     * no updates an empty list is returned.
     *
     * @param dbs the database where the update is stored.
     * @param userId the id of the user getting the update.
     * @param time The time of the last point when updates were grabbed.
     * @return A list of updates.
     * @throws AuthenticationException Thrown if the user does not have access to the user.
     * @throws DatabaseAccessException Thrown if the user does not exist.
     */
    public static BasicDBList mongoGetUpdate(final DB dbs, final String userId, final long time) throws AuthenticationException,
            DatabaseAccessException {
        final DBObject corsor = retrieveUpdate(dbs, userId);

        final BasicDBList updateList = (BasicDBList) corsor.get(UPDATE);
        if (updateList == null) {
            return new BasicDBList();
        }
        if (time <= 0) {
            return updateList;
        }
        final BasicDBList resultList = new BasicDBList();
        final int size = updateList.size();
        for (int i = 0; i < size; i++) {
            final long difference = ((Long) ((BasicBSONObject) updateList.get(i)).get(TIME)) - time;
            if (difference >= 0) {
                resultList.add(updateList.get(i));
            }
        }
        return resultList;
    }

    /**
     * Deletes an update.
     * @param dbs The database where the update is getting deleted.
     * @param userId The id of the user that is deleting the update.
     * @param id The id of the udpate that is being deleted.
     * @param classification The type of update (course, assignment, ...)
     * @throws AuthenticationException Thrown if the user does not have permission to delete the update.
     * @throws DatabaseAccessException Thrown if the user does not exist.
     */
    public static void mongoDeleteUpdate(final DB dbs, final String userId, final String id, final String classification)
            throws AuthenticationException, DatabaseAccessException {
        // pull the item and delete it from the list
        final DBCollection users = dbs.getCollection(UPDATE_COLLECTION);
        final BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(
                UPDATEID, id)));

        users.update(retrieveUpdate(dbs, userId), query);
    }

    /**
     * Either returns a new update or creates a new update then attempts to
     * return that update.
     *
     * @param dbs the database where the update is stored.
     * @param userId The user that holds the update list.
     * @return A database object representing the update.
     * @throws DatabaseAccessException thrown if an update fails to be created.
     */
    private static DBObject retrieveUpdate(final DB dbs, final String userId) throws DatabaseAccessException {
        try {
            return dbs.getCollection(UPDATE_COLLECTION).find(new BasicDBObject(SELF_ID, userId)).next();
        } catch (NoSuchElementException e) {
            // insert the update
            final BasicDBObject newUpdate = new BasicDBObject(SELF_ID, userId);
            dbs.getCollection(UPDATE_COLLECTION).insert(newUpdate);
            return dbs.getCollection(UPDATE_COLLECTION).find(new BasicDBObject(SELF_ID, userId)).next();
        }
    }
}
