package test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.course.CourseBuilder;
import database.course.CourseManager;

public class DatabaseTester {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException 
	{
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("test");
		String returnId = testCourses(db);
		testAssignments(db,returnId);
	}
	
	public static DB getDatabase() {
		return null;
	}
	
	public static String testCourses(DB dbs) throws AuthenticationException {
		CourseBuilder testBuilder = new CourseBuilder();
		CourseBuilder testBuilder1 = new CourseBuilder();
		testBuilder.setAccess("public");
		testBuilder.setSemesester("FALL");
		testBuilder.setName("Discrete Mathematics");
		testBuilder.setDescription("mathematcs that do discrete things!");
		testBuilder.setOpenDate("yyyy mm dd hh ss " + (new Date(System.currentTimeMillis() - 1000000).getTime()));
		testBuilder.setCloseDate("yyyy mm dd hh ss " + (new Date(System.currentTimeMillis() + 1000000).getTime()));
		testBuilder.permissions.admin = new ArrayList<String>();
		testBuilder.permissions.admin.add("david");
		testBuilder.permissions.admin.add("larry");

		testBuilder.permissions.mod = new ArrayList<String>();
		testBuilder.permissions.mod.add("raniero");
		testBuilder.permissions.mod.add("manoj");

		testBuilder.permissions.users = new ArrayList<String>();
		testBuilder.permissions.users.add("vijay");
		testBuilder.permissions.users.add("matt");
		System.out.println(testBuilder.toString());

		System.out.println("INSERTING COURSE");
		String courseId = CourseManager.mongoInsertCourse(dbs, testBuilder);
		System.out.println("INSERTING COURSE SUCCESSFULT");
		System.out.println(courseId);
		// testing getting courses

		System.out.println("GETTING COURES AS ADMIN");
		CourseBuilder builder = CourseManager.mongoGetCourse(dbs, courseId, "david", System.currentTimeMillis());
		System.out.println(builder.toString());
		System.out.println("GETTING COURES AS MOD");
		CourseBuilder modBuilder = CourseManager.mongoGetCourse(dbs, courseId, "manoj", System.currentTimeMillis());
		System.out.println(modBuilder.toString());
		System.out.println("GETTING COURES AS USER");
		CourseBuilder userBuilder = CourseManager.mongoGetCourse(dbs, courseId, "matt", System.currentTimeMillis());
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING COURES AS NO ONE");
			CourseBuilder crashBuilder = CourseManager.mongoGetCourse(dbs, courseId, "NO_ONE", System.currentTimeMillis());
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING" + crashBuilder.toString());
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}

		try {
			System.out.println("UPDATING COURSE AS NO ONE");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "NO_ONE", userBuilder);
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "vijay", userBuilder);
			System.out.println("SOMETHING FAILED, USER SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS MOD");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "raniero", userBuilder);
			System.out.println("Mod can only do assignment list");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		testBuilder1.description = "I HAVE A DIFFERENT DESCRIPTION NOW";
		boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "larry", testBuilder1);

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		CourseBuilder postUpdate = CourseManager.mongoGetCourse(dbs, courseId, "david", System.currentTimeMillis());
		return courseId;
	}
	
	public static String testAssignments(DB dbs, String courseId) throws AuthenticationException {
		AssignmentBuilder testBuilder = new AssignmentBuilder();
		testBuilder.setCourseId(courseId);
		testBuilder.setName("Raising Puppies");
		testBuilder.setDescription("genetically engineered puppies that potty train themselves!");
		System.out.println("MINUS TEST " + new Date(System.currentTimeMillis()- 1000000));
		System.out.println("NORMAL TEST " + new Date(System.currentTimeMillis()));
		testBuilder.setOpenDate("yyyy mm dd hh ss " + (new Date(System.currentTimeMillis() - 1000000).getTime()));
		testBuilder.setCloseDate("yyyy mm dd hh ss " + (new Date(System.currentTimeMillis() + 1000000).getTime()));
		System.out.println("MINUS " + testBuilder.openDate);
		testBuilder.permissions.admin = new ArrayList<String>();
		testBuilder.permissions.admin.add("david");
		testBuilder.permissions.admin.add("larry");

		testBuilder.permissions.mod = new ArrayList<String>();
		testBuilder.permissions.mod.add("raniero");
		testBuilder.permissions.mod.add("manoj");

		testBuilder.permissions.users = new ArrayList<String>();
		testBuilder.permissions.users.add("saby");
		testBuilder.permissions.users.add("stephanie");
		testBuilder.permissions.users.add("matt");
		System.out.println(testBuilder.toString());
		
		System.out.println(testBuilder.toString());
		System.out.println("Inserting Assignments");
		String assignmentId = null;
		try{
			System.out.println("Admin");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"david",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}
		try
		{
			System.out.println("Mod");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"raniero",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}
		
		try
		{
			System.out.println("User");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"matt",testBuilder);
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}

		System.out.println("GETTING ASSIGNMENTS AS ADMIN");
		AssignmentBuilder builder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "david", System.currentTimeMillis());
		System.out.println(builder.toString());
		System.out.println("GETTING ASSIGNMENTS AS MOD");
		AssignmentBuilder modBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "manoj", System.currentTimeMillis());
		System.out.println(modBuilder.toString());
		System.out.println("GETTING ASSIGNMENTS AS USER");
		AssignmentBuilder userBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "matt", System.currentTimeMillis());
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING ASSIGNMENTS AS NO ONE");
			AssignmentBuilder crashBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "NO_ONE", System.currentTimeMillis());
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING" + crashBuilder.toString());
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}

		try {
			System.out.println("UPDATING ASSIGNMENTS AS NO ONE");
			boolean updated = AssignmentManager.mongoUpdateAssignment(dbs, assignmentId, "NO_ONE", userBuilder);
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = AssignmentManager.mongoUpdateAssignment(dbs, assignmentId, "vijay", userBuilder);
			System.out.println("SOMETHING FAILED, USER SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS MOD");
			boolean updated = AssignmentManager.mongoUpdateAssignment(dbs, assignmentId, "raniero", userBuilder);
			System.out.println("Mod can only do assignment list");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		userBuilder.description = "I HAVE A DIFFERENT DESCRIPTION NOW";
		boolean updated = AssignmentManager.mongoUpdateAssignment(dbs, assignmentId, "larry", userBuilder);

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		AssignmentBuilder postUpdate = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "david", System.currentTimeMillis());
		return assignmentId;
	}
	
}
