package mongodb_client;

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


public class institution 
{
	
	MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
	DB db = mongoClient.getDB("institution");
	boolean auth = db.authenticate("headlogin","login".toCharArray());
	//System.out.println(auth);
	
	
	

}
