package connection;

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
	
	private DatabaseClient(String url) throws UnknownHostException{
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		MongoClient mongoClient = new MongoClient(url);
		db = mongoClient.getDB("login");
		
	}
	
	private DatabaseClient(){
	}

	public static void main(String[] args) throws Exception {


		//db = mongoClient.getDB("login");
		//boolean auth = db.authenticate("headlogin","login".toCharArray());
		///System.out.println(auth);
		
		//MongoAddUser("CourseSketchUsers",db,"manoj","student","manojisawesome@gmail.com");
		
		//if(auth)
		//{
			//System.out.println(MongoIdentify("CourseSketchUsers",db,"manoj","student"));
			/*
			BasicDBObject document = new BasicDBObject();
			document.put("name", "sa");
			table.insert(document);
			*/
		//}
		
		
//		for (String s : mongoClient.getDatabaseNames()) {
//			   System.out.println(s);
//			}
		
//		Set<String> collectionNames = db.getCollectionNames();
//        for (String s : collectionNames) {
//            System.out.println(s);
//        }
		
	}
	
	public static DatabaseClient getInstance(){
		if(instance==null)
			instance = new DatabaseClient();
		return instance;
	}
	
	static boolean MongoIdentify(String u, String p) throws NoSuchAlgorithmException, InvalidKeySpecException, UnknownHostException {
		
		boolean auth = instance.db.authenticate("headlogin","login".toCharArray());
		DBCollection table = instance.db.getCollection("CourseSketchUsers");
		BasicDBObject query = new BasicDBObject("UserName",u);
		
		DBObject corsor = table.findOne(query);
		
		System.out.println();
		System.out.println(corsor.get("Password"));
		return PasswordHash.validatePassword(p.toCharArray(),corsor.get("Password").toString());
		
		//return corsor.hasNext();
	}
	
	
	private static void MongoAddUser(String CollectionName, DB dbs, String u, String p,String EmailValue) throws GeneralSecurityException, InvalidKeySpecException {
		DBCollection new_user = dbs.getCollection(CollectionName);
		BasicDBObject query = new BasicDBObject("UserName",u).append("Password",PasswordHash.createHash(p)).append("Email", EmailValue);
		new_user.insert(query);
		
	}

}
