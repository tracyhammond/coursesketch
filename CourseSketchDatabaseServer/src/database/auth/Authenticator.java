package database.auth;

import static util.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.DateTime;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.RequestConverter;

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
		//DBCollection new_user = dbs.getCollection(USER_GROUP_COLLECTION);
		for (String group: groups) {
			if (group.startsWith(GROUP_PREFIX)) {
				DBRef myDbRef = new DBRef(dbs, USER_GROUP_COLLECTION, new ObjectId(group.substring(GROUP_PREFIX_LENGTH)));
				DBObject corsor = myDbRef.fetch();
				ArrayList list = (ArrayList)corsor.get(USER_LIST);
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
	
	public static class AuthType {
		public boolean user = false;
		public boolean mod = false;
		public boolean admin = false;
		public boolean checkDate = false;
		public boolean checkAdminOrMod = false;

		/**
		 * Returns true if one of the values in AuthType is true.
		 */
		public boolean validRequest() {
			return user || mod || admin || checkDate || checkAdminOrMod;
		}
	}
	
	/**
	 * Checks to make sure that the user is authenticated for all values that are true.
	 *
	 * @param dbs The database.institution to use
	 * @param courseId The Id of the course we are checking
	 * @param userId The user we are checking is valid
	 * @param user True if we want to check if the person is a user
	 * @param mod True if we want to check if the person is a mod
	 * @param admin True if we want to check if the person is an admin
	 * @param checkDate True if we want to check if the date is valid
	 * @return True if all checked values are valid
	 * @throws DatabaseAccessException
	 */
	public static boolean mognoIsAuthenticated(DB dbs, String collection, String itemId,String userId, long checkTime,
			Authenticator.AuthType checkType) throws DatabaseAccessException {

		if(!checkType.validRequest()) {
			return false;
		}

		DBRef myDbRef = new DBRef(dbs, collection, new ObjectId(itemId));
		DBObject corsor = myDbRef.fetch();
		if (corsor == null) {
			throw new DatabaseAccessException(collection + " was not found with the following ID " + itemId);
		}
		boolean validUser = false;
		if (checkType.user) {
			ArrayList usersList =  (ArrayList<Object>) corsor.get(USERS); //convert to ArrayList<String>
			validUser = Authenticator.checkAuthentication(dbs, userId, usersList);
		}

		boolean validModOrAdmin = false;

		boolean validMod = false;
		if (checkType.mod || checkType.checkAdminOrMod) {
			ArrayList modList =  (ArrayList<Object>) corsor.get(MOD); //convert to ArrayList<String>
			boolean temp = Authenticator.checkAuthentication(dbs, userId, modList);
			if (checkType.mod) {
				validMod = temp;
			}
			if (checkType.checkAdminOrMod) {
				validModOrAdmin = temp;
			}
		}

		boolean validAdmin = false;
		if (checkType.admin || checkType.checkAdminOrMod) {
			ArrayList adminList =  (ArrayList<Object>) corsor.get(ADMIN); //convert to ArrayList<String>
			boolean temp = Authenticator.checkAuthentication(dbs, userId, adminList);
			if (checkType.admin) {
				validAdmin = temp;
			}
			if (checkType.checkAdminOrMod) {
				validModOrAdmin = temp || validModOrAdmin;
			}
		}

		boolean validDate = false;
		if (checkType.checkDate) {
			validDate = Authenticator.isTimeValid(checkTime,
				RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(ACCESS_DATE)).longValue()),
				RequestConverter.getProtoFromMilliseconds(((Number)corsor.get(CLOSE_DATE)).longValue()));
		}
		return (validUser == checkType.user) && (validMod == checkType.mod) &&
				(validAdmin == checkType.admin) && (validDate == checkType.checkDate) &&
				(validModOrAdmin == checkType.checkAdminOrMod);
	}
}
