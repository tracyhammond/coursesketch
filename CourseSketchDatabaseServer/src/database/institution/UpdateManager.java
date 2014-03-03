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

import database.DatabaseAccessException;
import database.auth.AuthenticationException;

public class UpdateManager 
{
	public static void mongoInsertUpdate(DB dbs, String userId, String ID, long Time,String classification) throws AuthenticationException, DatabaseAccessException
	{	
		//pull the item and delete it from the list
		DBCollection users = dbs.getCollection("CourseSketchUsers");
		BasicDBObject query =  new BasicDBObject("$pull",new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, ID)));
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		users.update(corsor, query);
		
		//push the new file onto the classification
		users = dbs.getCollection("CourseSketchUsers");
		query =  new BasicDBObject("$push", new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, ID).append(TIME,Time)));
		myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		corsor = myDbRef.fetch();
		users.update(corsor, query);
	}
	
	public static BasicDBList mongoGetUpdate(DB dbs, String userId) throws AuthenticationException, DatabaseAccessException
	{
		DBCollection courses = dbs.getCollection("CourseSketchUsers");
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		
		BasicDBList adminList = (BasicDBList) corsor.get(UPDATE);
		//ArrayList<> modList = (ArrayList<ArrayList<string>>) corsor.get(MOD);
		//ArrayList modList = (ArrayList<Object>) adminList.get(0);
		return adminList;
		//long difference = 1019384 - (long)((BasicBSONObject) adminList.get(0)).get(TIME);
		//System.out.println(((BasicBSONObject) adminList.get(0)).get(TIME));
		//System.out.println(difference);
		//for(int i = 0; i < 6; i++){
			//System.out.println(adminList.get(i));
		//}
		
		
	}
	
	public static void mongoDeleteUpdate(DB dbs, String userId, String ID, String classification) throws AuthenticationException, DatabaseAccessException
	{
		//pull the item and delete it from the list
		DBCollection users = dbs.getCollection("CourseSketchUsers");
		BasicDBObject query =  new BasicDBObject("$pull",new BasicDBObject(UPDATE, new BasicDBObject(CLASSIFICATION, classification).append(UPDATEID, ID)));
		DBRef myDbRef = new DBRef(dbs, "CourseSketchUsers", new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		users.update(corsor, query);
	}
		
}
