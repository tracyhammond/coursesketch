package database.user;

import static database.DatabaseStringConstants.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import protobuf.srl.school.School.SrlUser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.PasswordHash;

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
	 * @throws DatabaseAccessException 
	 */
	public static ArrayList<String> getUserCourses(DB dbs, String userId) throws DatabaseAccessException {
		DBCollection users = dbs.getCollection(USER_COLLECTION);
		BasicDBObject query = new BasicDBObject(SELF_ID, userId);
		DBObject cursor = users.findOne(query);
		if (cursor == null) {
			throw new DatabaseAccessException("Can not find a user with that id", false);
		}
		return (ArrayList) cursor.get(COURSE_LIST);
	}

	public static void createUser(DB dbs, SrlUser user, String userId) throws DatabaseAccessException {
		DBCollection users = dbs.getCollection(USER_COLLECTION);
		 // NOSHIP: userId must be hashed using the userName as a salt?
		BasicDBObject query = null;
		System.out.println(userId);
		try {
			query = new BasicDBObject(SELF_ID, userId).append(COURSE_LIST, new ArrayList<String>())
					.append(CREDENTIALS, PasswordHash.createHash(user.getEmail())).append(EMAIL, user.getEmail()).append(ADMIN, PasswordHash.createHash(userId));
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

	/**
	 * After this method is called a user now has the 
	 * @param db
	 * @param userId
	 * @param courseId
	 */
	static void addCourseToUser(DB db, String userId, String courseId) {
		System.out.println("The users Id " + userId);
		DBCollection users = db.getCollection(USER_COLLECTION);
		BasicDBObject query =  new BasicDBObject("$addToSet", new BasicDBObject(COURSE_LIST, courseId));
		DBRef myDbRef = new DBRef(db, USER_COLLECTION, userId);
		DBObject corsor = myDbRef.fetch();
		System.out.println("coros" + corsor);
		System.out.println("query" + query);
		System.out.println("courseId " + courseId);
		users.update(corsor, query);
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
