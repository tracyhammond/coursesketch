package connection;

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

public class Database {
	public Database() {
		
	}
	public static void main(String[] args) throws Exception {

		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("login");
		boolean auth = db.authenticate("headlogin","login".toCharArray());
		System.out.println(auth);
		
		//MongoAddUser("CourseSketchUsers",db,"manoj","student","manojisawesome@gmail.com");
		
		if(auth)
		{
		//	System.out.println(MongoIdentify("CourseSketchUsers",db,"manoj","student"));
			/*
			BasicDBObject document = new BasicDBObject();
			document.put("name", "sa");
			table.insert(document);
			*/
		}
	}
}
