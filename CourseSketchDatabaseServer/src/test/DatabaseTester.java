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
	
	public static boolean testCourses(DB dbs) throws AuthenticationException {
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
		String courseId = CourseManager.mongoInsertCourse(dbs, testBuilder);

		// testing getting
		CourseBuilder builder = CourseManager.mongoGetCourse(dbs, courseId, "david");
		CourseBuilder modBuilder = CourseManager.mongoGetCourse(dbs, courseId, "manoj");
		CourseBuilder userBuilder = CourseManager.mongoGetCourse(dbs, courseId, "matt");
		try {
			CourseBuilder crashBuilder = CourseManager.mongoGetCourse(dbs, courseId, "NO_ONE");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}
	}
}
