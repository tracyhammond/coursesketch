package database.institution;

import static util.StringConstants.*;

import java.util.NoSuchElementException;

import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;

public class UpdateManager {

	public static void mongoInsertUpdate(DB dbs, String userId, String ID, long Time, String classification) throws AuthenticationException,
			DatabaseAccessException {
		// pull the item and delete it from the list
		DBCollection users = dbs.getCollection(UPDATE_COLLECTION);
		BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID,
				ID)));
		DBObject corsor = retrieveUpdate(dbs, userId);
		users.update(corsor, query);

		// push the new file onto the classification
		users = dbs.getCollection(UPDATE_COLLECTION);
		query = new BasicDBObject("$push", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, ID).append(
				TIME, Time)));
		users.update(corsor, query);
	}

	/**
	 * Returns all updates that are after a certain time.
	 *
	 * if the given time is <= 0 than the entire list is returned.
	 * If there are no updates an empty list is returned
	 * @param dbs
	 * @param userId
	 * @param time
	 * @return
	 * @throws AuthenticationException
	 * @throws DatabaseAccessException
	 */
	public static BasicDBList mongoGetUpdate(DB dbs, String userId, long time) throws AuthenticationException, DatabaseAccessException {
		//DBRef myDbRef = new DBRef(dbs, "Users", new ObjectId(userId));
		//DBObject corsor = myDbRef.fetch();
		DBObject corsor = retrieveUpdate(dbs, userId);

		BasicDBList updateList = (BasicDBList) corsor.get(UPDATE);
		if (updateList == null) {
			return new BasicDBList();
		}
		if (time <= 0) {
			return updateList;
		}
		BasicDBList resultList = new BasicDBList();
		int size = updateList.size();
		for (int i = 0; i < size; i++) {
			long difference = ((Long) ((BasicBSONObject) updateList.get(i)).get(TIME)) - time;
			if (difference >= 0) {
				resultList.add(updateList.get(i));
			}
		}
		return resultList;
	}

	public static void mongoDeleteUpdate(DB dbs, String userId, String ID, String classification) throws AuthenticationException,
			DatabaseAccessException {
		// pull the item and delete it from the list
		DBCollection users = dbs.getCollection(UPDATE_COLLECTION);
		BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID,
				ID)));

		users.update(retrieveUpdate(dbs, userId), query);
	}

	/**
	 * Either returns a new update or creates a new update then attempts to return that update.
	 * @param dbs
	 * @param userId
	 * @return
	 * @throws DatabaseAccessException
	 */
	private static DBObject retrieveUpdate(DB dbs, String userId) throws DatabaseAccessException {
		try {
			return dbs.getCollection(UPDATE_COLLECTION).find(new BasicDBObject(SELF_ID, userId)).next();
		} catch (NoSuchElementException e) {
			// insert the update
			BasicDBObject newUpdate = new BasicDBObject(SELF_ID, userId);
			dbs.getCollection(UPDATE_COLLECTION).insert(newUpdate);
			return dbs.getCollection(UPDATE_COLLECTION).find(new BasicDBObject(SELF_ID, userId)).next();
		}
	}
}
