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

	public static class CourseBuilder
	{
		public CourseBuilder() 
	}




	
	MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
	DB db = mongoClient.getDB("institution");
	boolean auth = db.authenticate("headlogin","login".toCharArray());
	//System.out.println(auth);
	private static void MongoInsertCourse(String Description, String Name, String Access, String Semesester, String OpenDate, String CloseDate, String Image, String[] AssignmentList, String Admin, String Mod, String[] Users) throws GeneralSecurityException, InvalidKeySpecException
	{
		
		DBCollection new_user = dbs.getCollection(CollectionName);
		BasicDBObject query = new BasicDBObject(("Description",Description)
										 .append("Name",Name)
										 .append("Access", Access) Semesester
										 .append("Semesester",Semesester)
										 .append("OpenDate", OpenDate)
										 .append("CloseDate",CloseDate)
										 .append("Image", Image)
										 .append("AssignmentList",AssignmentList)
										 .append("Admin", Admin)
										 .append("Name",Name)
										 .append("Access", Access)
										 .append("Name",Name)
										 .append("Access", Access) 										 );
		new_user.insert(query);
		
		
		Register : “public”
Semesester: “Fall 2013”
OpenDate: “08/23/2014”
CloseDate: “12/16/2014”
Image: “image loc”
Assignment List: “0001-0001”, “0001-0002”, “0001-0003”
Admin “turner, larry”
Mod “vijay”
Users: “daniel, manoj, stephanie”
		
	}
	
	
	

}
