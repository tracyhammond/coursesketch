package database.institution.managers;

import java.util.ArrayList;
import java.util.List;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.institution.DatabaseAccessException;
import database.institution.auth.AuthenticationException;


public final class Institution {
	private static Institution instance;
	private DB db;

	private Institution(String url) {
		try {
			MongoClient mongoClient = new MongoClient(url);
			db = mongoClient.getDB("institution");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Institution() {
		//this("goldberglinux.tamu.edu");
		this("localhost");
	}

	private static Institution getInstance() {
		if(instance==null)
			instance = new Institution();
		return instance;
	}

	/**
	 * Returns a list of courses given a list of Ids for the courses
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlCourse> mongoGetCourses(List<String> courseIds,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlCourse> allCourses = new ArrayList<SrlCourse>();
		for(String courseId : courseIds) {
			try {
				allCourses.add(CourseManager.mongoGetCourse(getInstance().db, courseId, userId,currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			}
		}
		return allCourses;
	}

	/**
	 * Returns a list of problems given a list of Ids for the course problems.
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlProblem> mongoGetCourseProblem(List<String> problemID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlProblem> allCourses = new ArrayList<SrlProblem>();
		for(int index = 0; index < problemID.size(); index ++) {
			try {
				allCourses.add(CourseProblemManager.mongoGetCourseProblem(getInstance().db, problemID.get(index), userId, currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			} catch (AuthenticationException e) {
				if (e.getType() != AuthenticationException.INVALID_DATE) {
					throw e;
				}
			}
		}
		return allCourses;
	}

	/**
	 * Returns a list of problems given a list of Ids for the course problems.
	 * @throws AuthenticationException
	 */
	public static ArrayList<SrlAssignment> mongoGetAssignment(List<String> assignementID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlAssignment> allAssignments = new ArrayList<SrlAssignment>();
		for (int assignments = assignementID.size() - 1; assignments >= 0; assignments--) {
			try {
				allAssignments.add(AssignmentManager.mongoGetAssignment(getInstance().db, assignementID.get(assignments),
						userId, currentTime));
			} catch (DatabaseAccessException e) {
				e.printStackTrace();
			} catch (AuthenticationException e) {
				if (e.getType() != AuthenticationException.INVALID_DATE) {
					throw e;
				}
			}
		}
		return allAssignments;
	}

	public static ArrayList<SrlBankProblem> mongoGetProblem(List<String> problemID,String userId) throws AuthenticationException {
		long currentTime = System.currentTimeMillis();
		ArrayList<SrlBankProblem> allProblems = new ArrayList<SrlBankProblem>();
		for (int problem = problemID.size() - 1; problem >= 0; problem--) {
			allProblems.add(BankProblemManager.mongoGetBankProblem(getInstance().db, problemID.get(problem), userId));
		}
		return allProblems;
	}
	
	public static ArrayList<SrlCourse> getAllPublicCourses() {
		return CourseManager.mongoGetAllPublicCourses(getInstance().db);
	}
}
