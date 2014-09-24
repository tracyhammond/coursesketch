package database.user;

import static database.DatabaseStringConstants.*;

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
	public static String mongoInsertGroup(final DB dbs, final SrlGroup group)
	{
	    final DBCollection new_user = dbs.getCollection(USER_GROUP_COLLECTION);
	    final BasicDBObject query = new BasicDBObject(USER_LIST, group.getUserIdList())
										.append(NAME, group.getGroupName())
										.append(ADMIN, group.getAdminList());

		new_user.insert(query);
		final DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static SrlGroup mongoGetGroup(final DB dbs, final String groupId, final String userId) throws AuthenticationException
	{
	    final DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
	    final BasicDBObject query = new BasicDBObject("_id", groupId);
	    final DBObject corsor = courses.findOne(query);

	    final ArrayList<String> adminList = (ArrayList) corsor.get("Admin");
		boolean isAdmin;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);

		if (!isAdmin)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		final SrlGroup.Builder exactGroup = SrlGroup.newBuilder();
		exactGroup.addAllUserId((ArrayList) corsor.get("UserList"));
		exactGroup.setGroupName((String) corsor.get("Name"));
		if (isAdmin) {
			exactGroup.addAllAdmin((ArrayList) corsor.get("Admin")); // admin
		}
		return exactGroup.build();
	}

	public static boolean mongoUpdateGroup(final DB dbs, final String groupID, final String userId, final SrlGroup group)
	        throws AuthenticationException
	{
	    final DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
	    final BasicDBObject query = new BasicDBObject(SELF_ID, groupID);
	    final DBObject corsor = courses.findOne(query);

	    final ArrayList<String> adminList = (ArrayList) corsor.get("Admin");
		boolean isAdmin;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);

		if (!isAdmin)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		final BasicDBObject updated = new BasicDBObject();
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
