package test;

import com.mongodb.DB;

import database.auth.AuthenticationException;
import database.course.CourseBuilder;
import database.course.CourseManager;

public class DatabaseTester {
	public static void main(String args[]) {
		
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
		testBuilder.permissions.admin = new String[] {"david", "larry"};
		testBuilder.permissions.mod = new String[] {"raniero", "manoj"};
		testBuilder.permissions.users = new String[] {"vijay", "matt"};
		System.out.println(testBuilder.toString());
		
		System.out.println("INSERTING COURSE");
		String courseId = CourseManager.mongoInsertCourse(dbs, testBuilder);
		System.out.println("INSERTING COURSE SUCCESSFULT");
		// testing getting courses
		
		System.out.println("GETTING COURES AS ADMIN");
		CourseBuilder builder = CourseManager.mongoGetCourse(dbs, courseId, "david");
		System.out.println("GETTING COURES AS MOD");
		CourseBuilder modBuilder = CourseManager.mongoGetCourse(dbs, courseId, "manoj");
		System.out.println("GETTING COURES AS USER");
		CourseBuilder userBuilder = CourseManager.mongoGetCourse(dbs, courseId, "matt");
		try {
			System.out.println("GETTING COURES AS NO ONE");
			CourseBuilder crashBuilder = CourseManager.mongoGetCourse(dbs, courseId, "NO_ONE");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}
		
		try {
			System.out.println("UPDATING COURSE AS NO ONE");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "NO_ONE", userBuilder);
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "vijay", userBuilder);
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS MOD");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "raniero", userBuilder);
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		userBuilder.description = "I HAVE A DIFFERENT DESCRIPTION NOW";
		boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "larry", userBuilder);

		return courseId;
	}
}
