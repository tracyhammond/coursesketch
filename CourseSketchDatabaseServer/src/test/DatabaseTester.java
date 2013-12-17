package test;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;

public class DatabaseTester {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException {
		MongoClient mongoClient = new MongoClient("localhost");
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("test");
		try {
			String returnId = CourseTester.testCourses(db);
			System.out.println("Inserting assignment into course! " + returnId);
			String assignmentId = AssignmentTester.testAssignments(db, returnId);
			System.out.println("Creating bank problem");
			String bankId = BankProblemTester.testBankProblems(db, returnId);
			System.out.println("Creating course problem");
			CourseProblemTester.testCourseProblems(db, returnId, assignmentId, bankId);
		} catch (DatabaseAccessException e) {
			e.printStackTrace();
		}
	}

	public static DB getDatabase() {
		return null;
	}
}
