package database.institution;

import static database.StringConstants.*;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlSchool;
import protobuf.srl.school.School.SrlUser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.BasicDBList;

import connection.TimeManager;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;

public class UpdateManager 
{
	public static void mongoInsertUpdate(DB dbs, String userId, String ID, long Time, String classification) throws AuthenticationException,
			DatabaseAccessException {
		// pull the item and delete it from the list
		DBCollection users = dbs.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID,
				ID)));
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		users.update(corsor, query);

		// push the new file onto the classification
		users = dbs.getCollection("CourseSketchUsers");
		query = new BasicDBObject("$push", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, ID).append(
				TIME, Time)));
		myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		corsor = myDbRef.fetch();
		users.update(corsor, query);
	}

	/**
	 * Returns all updates that are after a certain time.
	 *
	 * if the given time is <= 0 than the entire list is returned.
	 * @param dbs
	 * @param userId
	 * @param time
	 * @return
	 * @throws AuthenticationException
	 * @throws DatabaseAccessException
	 */
	public static BasicDBList mongoGetUpdate(DB dbs, String userId, long time) throws AuthenticationException, DatabaseAccessException
	{
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();

		BasicDBList updateList = (BasicDBList) corsor.get(UPDATE);
		if (time <= 0) {
			return updateList;
		}
		BasicDBList resultList = new BasicDBList();
		int size = updateList.size();
		for (int i = 0; i < size; i ++) {
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
		DBCollection users = dbs.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("$pull", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID,
				ID)));
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		users.update(corsor, query);
	}

}
