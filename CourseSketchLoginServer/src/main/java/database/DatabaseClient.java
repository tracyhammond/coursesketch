package database;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import connection.PasswordHash;


public class DatabaseClient {
	private static DatabaseClient instance;
	private DB db;

	private DatabaseClient(String url) {
		System.out.println("creating new database instance");
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(url);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		db = mongoClient.getDB("login");
		if (db == null) {
			System.out.println("Db is null!");
		}
	}

	private DatabaseClient() {
		this("goldberglinux.tamu.edu");
		//this("localhost");
	}

	/**
	 * Used only for the purpose of testing overwrite the instance with a test instance that can only access a test database
	 * @param testOnly
	 */
	public DatabaseClient(boolean testOnly) {
		try {
			MongoClient mongoClient = new MongoClient("localhost");
			if (testOnly) {
				db = mongoClient.getDB("test");
			} else {
				db = mongoClient.getDB("login");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	public static void main(String[] args) throws Exception {

	}

	public static DatabaseClient getInstance() {
		if(instance==null)
			instance = new DatabaseClient();
		return instance;
	}

	public static final String mongoIdentify(String u, String p) throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException {

		//boolean auth = getInstance().db.authenticate("headlogin","login".toCharArray());
		DBCollection table = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",u);

		DBObject corsor = table.findOne(query);

		if (corsor==null)
			return null;
		if (PasswordHash.validatePassword(p.toCharArray(),corsor.get("Password").toString())) {
			String result = corsor.get("ServerId") + ":" + corsor.get("ClientId");
			return result;
		} else {
			return null;
		}
		//return corsor.hasNext();
	}

	public static final boolean MongoAddUser(String user, String password,String email, boolean isInstructor) throws GeneralSecurityException, InvalidKeySpecException {
		DBCollection new_user = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",user);
		DBObject corsor = new_user.findOne(query);
		if(corsor == null)
		{
			query = new BasicDBObject("UserName",user)
				.append("Password",PasswordHash.createHash(password))
				.append("Email", email)
				.append("IsInstructor",isInstructor)
				.append("ServerId", Encoder.fancyID())
				.append("ClientId", Encoder.nextID().toString());
			new_user.insert(query);
			return true;
		}
		return false;
	}

	public static final boolean mongoIsInstructor(String user) {
		boolean auth = getInstance().db.authenticate("headlogin","login".toCharArray());
		DBCollection table = getInstance().db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",user);

		DBObject cursor = table.findOne(query);
		if (cursor == null) {
			System.out.println("Unable to find user!");
			return false;
		}
		
		String instructor = "" + cursor.get("IsInstructor");
		System.out.println("Instructor value " + instructor);
		if (cursor.get("IsInstructor") == null || instructor.equals("null")) {
			System.out.println("no value for instructor");
			return false;
		}
		return instructor.equals("true");
	}

}
