package database.user;

import static database.StringConstants.DATABASE;

import java.util.ArrayList;

import protobuf.srl.school.School.SrlUser;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;


public final class UserClient {
	private static UserClient instance;
	private DB db;

	private UserClient(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
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
}
