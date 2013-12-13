package database.auth;

import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.DateTime;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class Authenticator {

	/**
	 * Checks to see if a user is allowed within the current groupList.
	 *
	 * looks up the groupId if there are groupId associated with this groupList
	 * @param userId the userId within the program that is trying to be authenticated
	 * @param list2
	 * @return
	 */
	public static boolean checkAuthentication(DB dbs, String userId, List<String> groups) {
		DBCollection new_user = dbs.getCollection("UserGroups");
		for (String group: groups) {
			if (group.startsWith("group")) {
				group.substring(5); // should be correct?
				ArrayList list = (ArrayList<Object>)new_user.findOne(group.substring(5)).get("UserList");
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

	public static boolean isTimeValid(long time, DateTime openDate, DateTime closeDate) {
		return time >= openDate.getMillisecond() && time <= closeDate.getMillisecond();	
	}
}
