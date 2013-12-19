package database.user;

import protobuf.srl.school.School.SrlUser;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;


public final class UserClient {
	private static UserClient instance;
	private DB db;

	private UserClient(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
			db = mongoClient.getDB("institution");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private UserClient() {
		//this("goldberglinux.tamu.edu");
		this("localhost");
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
			db = mongoClient.getDB("test");
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}

	public static boolean insertUser(SrlUser user, String userId) throws DatabaseAccessException {
		UserManager.createUser(getInstance().db, user, userId);
		return true;
	}

	public static void addCourseToUser(String userName, String courseId) {
		UserManager.addCourseToUser(getInstance().db, userName, courseId);
	}
}
