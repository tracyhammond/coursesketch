package database.user.manager;

import protobuf.srl.school.School.SrlUser;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.institution.DatabaseAccessException;


public final class UserClient {
	private static UserClient instance;
	private DB db;

	private UserClient(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
			db = mongoClient.getDB("user_db");
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

	public static boolean insertUser(ByteString data) throws DatabaseAccessException {
		SrlUser user = null;
		try {
			user = SrlUser.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		UserManager.createUser(getInstance().db, user.getUsername(), user.getEmail());
		return true;
	}
}
