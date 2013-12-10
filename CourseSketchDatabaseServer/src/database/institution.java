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

import database.Institution;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.course.CourseBuilder;
import database.course.CourseManager;


public class Institution 
{
	private static Institution instance;
	private DB db;
	
	private Institution(String url) throws UnknownHostException
	{
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		MongoClient mongoClient = new MongoClient(url);
		db = mongoClient.getDB("login");
	}
	
	private Institution(){
	}

	public static Institution getInstance(){
		if(instance==null)
			instance = new Institution();
		return instance;
	}
	
	// if user can only access between open date and close date
	// user can only access problem between assignment open and close date
	public static CourseBuilder mongoGetCourse(String courseID,String userId) throws AuthenticationException {
		return CourseManager.mongoGetCourse(getInstance().db, courseID, userId);
		// do open close checking
	}
	
	//do get methods for course, assignment, problem

//	MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
//	DB db = mongoClient.getDB("institution");
//	boolean auth = db.authenticate("headlogin","login".toCharArray());
	//System.out.println(auth);
	//private static void MongoInsertCourse(String Description, String Name, String Access, String Semesester, String OpenDate, String CloseDate, String Image, String[] AssignmentList, String Admin, String Mod, String[] Users) throws GeneralSecurityException, InvalidKeySpecException	
	

}
