package database;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.DateTime;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import database.Institution;
import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.course.CourseBuilder;
import database.course.CourseManager;
import database.problem.CourseProblemBuilder;
import database.problem.CourseProblemManager;
import database.problem.ProblemBankBuilder;
import database.problem.ProblemManager;


public class Institution 
{
	private static Institution instance;
	private DB db;
	
	private Institution(String url)
	{
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		try {
			MongoClient mongoClient = new MongoClient(url);
			db = mongoClient.getDB("institution");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Institution(){
		this("goldberglinux.tamu.edu");
	}

	public static Institution getInstance(){
		if(instance==null)
			instance = new Institution();
		return instance;
	}
	
	// if user can only access between open date and close date
	// user can only access problem between assignment open and close date
	public static ArrayList<CourseBuilder> mongoGetCourses(List<String> courseID,String userId) throws AuthenticationException 
	{
		int courses = courseID.size()-1;
		long currentTime = System.currentTimeMillis();
		ArrayList<CourseBuilder> allCourses = null;
		
		while(courses >= 0)
		{
			allCourses.add(CourseManager.mongoGetCourse(getInstance().db, courseID.get(courses), userId,currentTime));
			courses--;
		}
		
		// need to return everything
		return allCourses;
		// do open close checking
	}
	
	public static ArrayList<CourseProblemBuilder> mongoGetCourseProblem(List<String> problemID,String userId) throws AuthenticationException 
	{
		int courseProblems = problemID.size()-1;
		long currentTime = System.currentTimeMillis();
		ArrayList<CourseProblemBuilder> allCourses = null;
		
		while(courseProblems >= 0)
		{
			allCourses.add(CourseProblemManager.mongoGetProblem(getInstance().db, problemID.get(courseProblems), userId, currentTime));
			courseProblems--;
	}
		
		// need to return everything
		return allCourses;
		// do open close checking
	}
	

	public static ArrayList<AssignmentBuilder> mongoGetAssignment(List<String> assignementID,String userId) throws AuthenticationException 
	{
		
		long currentTime = System.currentTimeMillis();
		ArrayList<AssignmentBuilder> allAssignments = null;
		
		for(int assignments = assignementID.size()-1; assignments >= 0; assignments--)
		{
			allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().db, assignementID.get(assignments), userId,currentTime));
		}
		
		// need to return everything
		return allAssignments;
		// do open close checking
	}
	public static ArrayList<ProblemBankBuilder> mongoGetProblem(List<String> problemID,String userId) throws AuthenticationException 
	{
		
		long currentTime = System.currentTimeMillis();
		ArrayList<ProblemBankBuilder> allProblems = null;
		
		for(int problem = problemID.size()-1; problem >= 0; problem --)
		{
			allProblems.add(ProblemManager.mongoGetProblem(getInstance().db, problemID.get(problem), userId));
		}
		
		// need to return everything
		return allProblems;
		// do open close checking
	}
	
	
	//do get methods for course, assignment, problem

//	MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
//	DB db = mongoClient.getDB("institution");
//	boolean auth = db.authenticate("headlogin","login".toCharArray());
	//System.out.println(auth);
	//private static void MongoInsertCourse(String Description, String Name, String Access, String Semesester, String OpenDate, String CloseDate, String Image, String[] AssignmentList, String Admin, String Mod, String[] Users) throws GeneralSecurityException, InvalidKeySpecException	
	

}
