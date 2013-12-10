package test;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.auth.AuthenticationException;
import database.course.CourseBuilder;
import database.course.CourseManager;

public class DatabaseTester {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException 
	{
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("test");
		testCourses(db);
	}
	
	public static DB getDatabase() {
		return null;
	}
	
	public static String testCourses(DB dbs) throws AuthenticationException {
		CourseBuilder testBuilder = new CourseBuilder();
		testBuilder.setAccess("public");
		testBuilder.setSemesester("FALL");
		testBuilder.setName("Discrete Mathematics");
		testBuilder.setDescription("mathematcs that do discrete things!");
		testBuilder.setOpenDate("");
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
		CourseBuilder builder = CourseManager.mongoGetCourse(dbs, courseId, "david");
		System.out.println(builder.toString());
		System.out.println("GETTING COURES AS MOD");
		CourseBuilder modBuilder = CourseManager.mongoGetCourse(dbs, courseId, "manoj");
		System.out.println(modBuilder.toString());
		System.out.println("GETTING COURES AS USER");
		CourseBuilder userBuilder = CourseManager.mongoGetCourse(dbs, courseId, "matt");
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING COURES AS NO ONE");
			CourseBuilder crashBuilder = CourseManager.mongoGetCourse(dbs, courseId, "NO_ONE");
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
		userBuilder.description = "I HAVE A DIFFERENT DESCRIPTION NOW";
		boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "larry", userBuilder);

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		CourseBuilder postUpdate = CourseManager.mongoGetCourse(dbs, courseId, "david");
		return courseId;
	}
}
