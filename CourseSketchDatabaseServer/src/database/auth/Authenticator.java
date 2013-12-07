package database.auth;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class Authenticator {

	/**
	 * Checks to see if a user is allowed within the current groupList.
	 *
	 * looks up the groupId if there are groupId associated with this groupList
	 * @param userId the userId within the program that is trying to be authenticated
	 * @param groupList
	 * @return
	 */
	public static boolean checkAuthentication(DB dbs, String userId, String[] groupList) {
		DBCollection new_user = dbs.getCollection("UserGroups");
		for (String group: groupList) {
			if (group.startsWith("group")) {
				group.substring(5); // should be correct?
				String[] list = (String[])new_user.findOne(group.substring(5)).get("UserList");
				if (checkAuthentication(dbs, userId, list)) {
					return true;
				}
			} else {
				if (group.equals(userId))
					return true;
			}
		}
		return false;
	}
}
