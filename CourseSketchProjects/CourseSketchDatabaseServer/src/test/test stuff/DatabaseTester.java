package test;

import static util.StringConstants.*;

import com.mongodb.Document;
import com.mongodb.DB;
import com.mongodb.MongoCollection<Document>;

import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.institution.Institution;
import coursesketch.database.util.user.UserClient;

public class DatabaseTester {
	public static void main(String args[]) throws AuthenticationException {
		//MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		new Institution(true);
		new UserClient(true);
		//deleteCollections(db);
		try {
			String returnId = CourseTester.testCourses();
			System.out.println("Inserting assignment into course! " + returnId);
			String assignmentId = AssignmentTester.testAssignments(returnId);
			System.out.println("Inserting second assignment into course! " + returnId);
			String assignmentId2 = AssignmentTester.testAssignments(returnId);
			System.out.println("Creating bank problem");
			String bankId1 = BankProblemTester.testBankProblems(returnId);
			System.out.println("Creating bank problem2");
			String bankId2 = BankProblemTester.testBankProblems(returnId);
			System.out.println("Creating course problem");
			CourseProblemTester.testCourseProblems(returnId, assignmentId, bankId1);
			System.out.println("Creating course problem2");
			CourseProblemTester.testCourseProblems(returnId, assignmentId2, bankId2);

			System.out.println("Test Users");
			UserTester.testUsers(returnId);
		} catch (DatabaseAccessException e) {
			e.printStackTrace();
		}
	}

	public static DB getDatabase() {
		return null;
	}

	public static void deleteCollections(DB dbs) {
		MongoCollection<Document> collection = dbs.getCollection(COURSE_PROBLEM_COLLECTION);
		collection.deleteOne(new Document());

		collection = dbs.getCollection(COURSE_COLLECTION);
		collection.deleteOne(new Document());

		collection = dbs.getCollection(ASSIGNMENT_COLLECTION);
		collection.deleteOne(new Document());

		collection = dbs.getCollection(PROBLEM_BANK_COLLECTION);
		collection.deleteOne(new Document());

		collection = dbs.getCollection(USER_GROUP_COLLECTION);
		collection.deleteOne(new Document());

		collection = dbs.getCollection(USER_COLLECTION);
		collection.deleteOne(new Document());
	}
}
