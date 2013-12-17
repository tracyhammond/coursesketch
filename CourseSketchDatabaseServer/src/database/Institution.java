package database;

import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.course.CourseManager;
import database.problem.BankProblemManager;
import database.problem.CourseProblemManager;


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
	public static ArrayList<SrlCourse> mongoGetCourses(List<String> courseID,String userId) throws AuthenticationException 
	{
		int courses = courseID.size()-1;
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlCourse> allCourses = new ArrayList<SrlCourse>();
		
		while(courses >= 0)
		{
			try {
				allCourses.add(CourseManager.mongoGetCourse(getInstance().db, courseID.get(courses), userId,currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			}
			courses--;
		}
		
		// need to return everything
		return allCourses;
		// do open close checking
	}
	
	public static ArrayList<SrlProblem> mongoGetCourseProblem(List<String> problemID,String userId) throws AuthenticationException 
	{
		int courseProblems = problemID.size()-1;
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlProblem> allCourses = new ArrayList<SrlProblem>();
		
		while(courseProblems >= 0)
		{
			try {
				allCourses.add(CourseProblemManager.mongoGetCourseProblem(getInstance().db, problemID.get(courseProblems), userId, currentTime));
			}catch(DatabaseAccessException e) {
				e.printStackTrace();
			}
			courseProblems--;
		}
		
		// need to return everything
		return allCourses;
		// do open close checking
	}

	public static ArrayList<SrlAssignment> mongoGetAssignment(List<String> assignementID,String userId) throws AuthenticationException 
	{
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
		for(int assignments = assignementID.size()-1; assignments >= 0; assignments--)
		{
			try {
				allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().db, assignementID.get(assignments), userId,currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			}
		}
		// need to return everything
		return allAssignments;
		// do open close checking
	}
	public static ArrayList<SrlBankProblem> mongoGetProblem(List<String> problemID,String userId) throws AuthenticationException 
	{
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlBankProblem> allProblems = new ArrayList<SrlBankProblem>();
		for(int problem = problemID.size()-1; problem >= 0; problem--)
		{
			allProblems.add(BankProblemManager.mongoGetBankProblem(getInstance().db, problemID.get(problem), userId));
		}
		// need to return everything
		return allProblems;
		// do open close checking
	}

}
