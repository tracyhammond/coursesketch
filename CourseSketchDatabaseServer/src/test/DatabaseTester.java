package test;

import static database.institution.StringConstants.*;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import database.institution.DatabaseAccessException;
import database.institution.auth.AuthenticationException;

public class DatabaseTester {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException {
		MongoClient mongoClient = new MongoClient("localhost");
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("test");
		deleteCollections(db);
		try {
			String returnId = CourseTester.testCourses(db);
			System.out.println("Inserting assignment into course! " + returnId);
			String assignmentId = AssignmentTester.testAssignments(db, returnId);
			System.out.println("Inserting second assignment into course! " + returnId);
			String assignmentId2 = AssignmentTester.testAssignments(db, returnId);
			System.out.println("Creating bank problem");
			String bankId1 = BankProblemTester.testBankProblems(db, returnId);
			System.out.println("Creating bank problem2");
			String bankId2 = BankProblemTester.testBankProblems(db, returnId);
			System.out.println("Creating course problem");
			CourseProblemTester.testCourseProblems(db, returnId, assignmentId, bankId1);
			System.out.println("Creating course problem2");
			CourseProblemTester.testCourseProblems(db, returnId, assignmentId2, bankId2);
		} catch (DatabaseAccessException e) {
			e.printStackTrace();
		}
	}

	public static DB getDatabase() {
		return null;
	}

	public static void deleteCollections(DB dbs) {
		DBCollection collection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);
		collection.remove(new BasicDBObject());
		
		collection = dbs.getCollection(COURSE_COLLECTION);
		collection.remove(new BasicDBObject());
		
		collection = dbs.getCollection(ASSIGNMENT_COLLECTION);
		collection.remove(new BasicDBObject());
		
		collection = dbs.getCollection(PROBLEM_BANK_COLLECTION);
		collection.remove(new BasicDBObject());
	}
}
