package database.user;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;

import protobuf.srl.school.School.SrlSchool;
import protobuf.srl.school.School.SrlUser;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;

public final class UserClient {
	private static UserClient instance;
	private DB db;
	private MongoClient DUMB_CLIENT = null;

	private UserClient(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
			DUMB_CLIENT = mongoClient;
			db = mongoClient.getDB(DATABASE);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private UserClient() {
		this("goldberglinux.tamu.edu");
	}

	private static UserClient getInstance() {
		if (instance==null)
			instance = new UserClient();
		return instance;
	}

	/**
	 * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
	 * @param testOnly
	 */
	public UserClient(boolean testOnly) {
		try {
			MongoClient mongoClient = new MongoClient("localhost");
			DUMB_CLIENT = mongoClient;
			if (testOnly) {
				db = mongoClient.getDB("test");
			} else {
				db = mongoClient.getDB(DATABASE);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}

	public static boolean insertUser(SrlUser user, String userId) throws DatabaseAccessException {
		UserManager.createUser(getInstance().db, user, userId);
		return true;
	}

	public static void addCourseToUser(String userId, String courseId) {
		UserManager.addCourseToUser(getInstance().db, userId, courseId);
	}

	public static ArrayList<String> getUserCourses(String userId) throws DatabaseAccessException {
		return UserManager.getUserCourses(getInstance().db, userId);
	}

	public static SrlSchool mongoGetReleventUpdates(String userId, long time) throws AuthenticationException, DatabaseAccessException {
		return UserUpdateHandler.mongoGetAllRelevantUpdates(getInstance().db, userId, time);
	}

	/**
	 * TODO: DELETE THIS CODE AS SOON AS GRADES ARE WORKING!
	 * @return
	 */
	public static MongoClient getDB() {
		return getInstance().DUMB_CLIENT;
	}
}
