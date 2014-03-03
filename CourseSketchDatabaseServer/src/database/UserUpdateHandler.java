package database;

import java.util.List;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlSchool;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import connection.TimeManager;
import static database.StringConstants.*;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.UpdateManager;

public class UserUpdateHandler {

	static long timeLimit = 2592000000L;
	
	public static void removeOldUpdates(DB db,String userId) throws AuthenticationException, DatabaseAccessException
	{
		int size = UpdateManager.mongoGetUpdate(db, userId).size();
		BasicDBList updateList = UpdateManager.mongoGetUpdate(db, userId);
		for(int i = 0;i < size;i++){
			long difference = TimeManager.getSystemTime()- (long)((BasicBSONObject) updateList.get(i)).get(TIME);
			if(timeLimit < difference)
			{
				UpdateManager.mongoDeleteUpdate(db, userId, (String)((BasicBSONObject) updateList.get(i)).get(UPDATEID), (String)((BasicBSONObject) updateList.get(i)).get(CLASSIFICATION));
			}
		}
	}
	
	public static void InsertUpdates(DB db, String userId, String id, String classification) throws AuthenticationException, DatabaseAccessException
	{
		UpdateManager.mongoInsertUpdate(db, userId, id, TimeManager.getSystemTime(), classification);
	}
	
	public static SrlSchool mongoGetAllRelevantUpdates(DB dbs, String userId, long time) throws AuthenticationException, DatabaseAccessException
	{
		int size = UpdateManager.mongoGetUpdate(dbs, userId).size();
		BasicDBList userUpdates = UpdateManager.mongoGetUpdate(dbs, userId);
		List<String> id = null;
		SrlSchool.Builder build = null;
		
		for(int i = 0;i < size;i++) {
			String classification = (String)((BasicBSONObject) userUpdates.get(i)).get(CLASSIFICATION);
			id.add((String)((BasicBSONObject) userUpdates.get(i)).get(UPDATEID));
			if(classification == "COURSE")
				build.addCourses(Institution.mongoGetCourses(id, userId).get(0));
			if(classification == "ASSIGNMENT")
				build.addAssignments(Institution.mongoGetAssignment(id, userId).get(0));
			if(classification == "PROBLEM")
				build.addBankProblems(Institution.mongoGetProblem(id, userId).get(0));
			
		}
		return build.build();
		
	}
	
}
