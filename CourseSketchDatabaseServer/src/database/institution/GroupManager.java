package database.institution;

import java.util.ArrayList;

import protobuf.srl.school.School.SrlGroup;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class GroupManager
{
	private static String mongoInsertGroup(DB dbs, SrlGroup group)
	{
		DBCollection new_user = dbs.getCollection("UserGroups");
		BasicDBObject query = new BasicDBObject("UserList",group.getUserIdList())
										.append("Name",group.getGroupName())
										.append("Admin",group.getAdminList());
										 
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return ("Group"+(String) corsor.get("_id"));
	}
	
	public static SrlGroup mongoGetCourse(DB dbs, String courseID,String userId) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("UserGroups");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);
		
		ArrayList<String> adminList = (ArrayList)corsor.get("Admin");
		boolean isAdmin;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		
		if(!isAdmin)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}
		
		SrlGroup.Builder exactGroup = SrlGroup.newBuilder();
		exactGroup.addAllUserId((ArrayList)corsor.get("UserList"));
		exactGroup.setGroupName((String)corsor.get("Name"));
		if (isAdmin) {
			exactGroup.addAllAdmin((ArrayList)corsor.get("Admin")); // admin
		}
		return exactGroup.build();
	}

	public static boolean mongoUpdateGroup(DB dbs, String courseID, String userId, SrlGroup group) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection("UserGroups");
		BasicDBObject query = new BasicDBObject("_id",courseID);
		DBObject corsor = courses.findOne(query);

		ArrayList<String> adminList = (ArrayList)corsor.get("Admin");
		boolean isAdmin;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);

		if(!isAdmin)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject updated = new BasicDBObject();
		if (isAdmin) {
			if (group.hasGroupName()) {
				updated.append("$set", new BasicDBObject("Name", group.getGroupName()));
			}
			if (group.getUserIdCount() > 0) {
				updated.append("$set", new BasicDBObject("UserList", group.getUserIdList()));
			}
			if (group.getAdminCount() > 0) {
				updated.append("$set", new BasicDBObject("UserList", group.getAdminList()));
			}
		}
		return true;
		
	}

}
