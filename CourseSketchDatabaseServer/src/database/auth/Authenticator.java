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
		DBCollection new_user = dbs.getCollection("User_Group");
		for (String group: groupList) {
			if (group.startsWith("group:")) {
				group.substring(6); // should be correct?
				new_user.find("?", group.substring(6));
			} else {
				if (group.equals(userId))
					return true;
			}
		}
		return false;
	}
}
