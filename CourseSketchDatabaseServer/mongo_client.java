package mongodb_client;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import java.util.List;
import java.util.Set;


public class mongo_client {

	public static void main(String[] args) throws Exception {

		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		
		DB db = mongoClient.getDB("admin");
		boolean auth = db.authenticate("super","super".toCharArray());
		System.out.println(auth);
		
		if(auth)
		{
		
			DBCollection table = db.getCollection("example");
			System.out.println("Collection created successfully");
			mongoClient.setWriteConcern(WriteConcern.JOURNALED);
			table.insert(new BasicDBObject("i", 0));
			
			/*
			BasicDBObject document = new BasicDBObject();
			document.put("name", "sa");
			table.insert(document);
			*/
		}
		
		
//		for (String s : mongoClient.getDatabaseNames()) {
//			   System.out.println(s);
//			}
		
//		Set<String> collectionNames = db.getCollectionNames();
//        for (String s : collectionNames) {
//            System.out.println(s);
//        }
		
		mongoClient.close();
		
	}

}
