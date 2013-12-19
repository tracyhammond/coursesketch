package database.user.manager;

import static database.institution.StringConstants.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.PasswordHash;
import database.institution.DatabaseAccessException;

public class UserManager {

	// get grades
	// update grades
	// insert grades
	// get real world identity (need second password)
	// isert real world identity
	// update real world identity (need second password)
	// 
	
	/**
	 * Returns the list of courses that a users has registered for
	 * @param userId
	 * @return
	 */
	public static ArrayList<String> getUserCourses(DB dbs, String userName) {
		DBCollection users = dbs.getCollection(USER_COLLECTION);
		BasicDBObject query = new BasicDBObject(SELF_ID, userName);
		DBObject cursor = users.findOne(query);
		return (ArrayList) cursor.get(COURSE_LIST);
	}

	public static void createUser(DB dbs, String userName, String email) throws DatabaseAccessException {
		DBCollection users = dbs.getCollection(USER_COLLECTION);
		 // NOSHIP: userId must be hashed using the userName as a salt?
		BasicDBObject query = null;
		try {
			query = new BasicDBObject(SELF_ID, userName).append(COURSE_LIST, new ArrayList<String>()).append(CREDENTIALS, PasswordHash.createHash(email)).append(EMAIL, email);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		if (query == null) {
			throw new DatabaseAccessException("An error occured creating password hash");
		}
		users.insert(query);
	}

	/**
	 * On first insert we also take in email as credentials.  All other ones we take in a password!
	 * @param dbs
	 * @param userId
	 * @param userData
	 */
	public static void createUserData(DB dbs, String userName, String userId, String email, String userData) {
		DBCollection users = dbs.getCollection(USER_COLLECTION);
		BasicDBObject query = new BasicDBObject(COURSE_LIST, new ArrayList<String>());
		users.insert(query);
	}

	/*
	public void registerUserForCourse(DB dbs, String userId, String CourseId) {
		DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(userId));
		DBObject corsor = myDbRef.fetch();
		DBObject updateObj = null;
		DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
		updateObj = new BasicDBObject(ASSIGNMENT_LIST, assignmentId);
		courses.update(corsor, new BasicDBObject ("$addToSet",updateObj));
	}
	*/
}
