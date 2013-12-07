package test;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.auth.AuthenticationException;
import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;

public class AssignmentTester 
{
public static void main(String args[]) throws UnknownHostException, AuthenticationException 
	{
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("Test");
		testCourses(db);
	}
	
	public static DB getDatabase() {
		return null;
	}
	
	public static boolean testCourses(DB dbs) throws AuthenticationException {
		AssignmentBuilder testBuilder = new AssignmentBuilder();
		testBuilder.setCourseId("");
		testBuilder.setName("Homework1");
		testBuilder.setType("Fill Blank");
		testBuilder.setOther("");
		testBuilder.setDescription("You are expected to be awesome");
		testBuilder.setResources("www.larryisawesome.com");
		testBuilder.setLatePolicy("augest2");
		testBuilder.setGradeWeigh("6");
		testBuilder.setOpenDate("march5");
		testBuilder.setDueDate("march6");
		testBuilder.setCloseDate("march7");
		testBuilder.setImageUrl("www.google.com");
		testBuilder.permissions.admin = new String[] {"david", "larry"};
		testBuilder.permissions.mod = new String[] {"raniero", "manoj"};
		testBuilder.permissions.users = new String[] {"vijay", "matt"};
		
		System.out.println(testBuilder.toString());
		System.out.println("Inserting Courses");
		String assignmentId = null;
		try{
			System.out.println("Admin");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"david",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}
		try
		{
			System.out.println("Mod");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"raniero",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}
		
		try
		{
			System.out.println("User");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"matt",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}
	/*
		
		
		AssignmentBuilder builder = AssignmentManager.mongoGetAssignment(dbs, courseId, "david");
		AssignmentBuilder modBuilder = AssignmentManager.mongoGetAssignment(dbs, courseId, "manoj");
		AssignmentBuilder userBuilder = AssignmentManager.mongoGetAssignment(dbs, courseId, "matt");
		
	*/
		
	}

}
