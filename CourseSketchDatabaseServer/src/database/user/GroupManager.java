package database.user;

import static database.StringConstants.*;

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
	public static String mongoInsertGroup(DB dbs, SrlGroup group)
	{
		DBCollection new_user = dbs.getCollection(USER_GROUP_COLLECTION);
		BasicDBObject query = new BasicDBObject(USER_LIST,group.getUserIdList())
										.append(NAME,group.getGroupName())
										.append(ADMIN,group.getAdminList());

		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static SrlGroup mongoGetGroup(DB dbs, String groupId, String userId) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
		BasicDBObject query = new BasicDBObject("_id",groupId);
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

	public static boolean mongoUpdateGroup(DB dbs, String groupID, String userId, SrlGroup group) throws AuthenticationException
	{
		DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
		BasicDBObject query = new BasicDBObject(SELF_ID,groupID);
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
