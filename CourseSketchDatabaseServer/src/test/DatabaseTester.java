package test;

import com.mongodb.DB;

import database.course.CourseBuilder;

public class DatabaseTester {
	public static void main(String args[]) {
		
	}
	
	public static DB getDatabase() {
		return null;
	}
	
	public static boolean testCourses() {
		CourseBuilder testBuilder = new CourseBuilder();
		testBuilder.setAccess("public");
		testBuilder.setSemesester("FALL");
		testBuilder.setName("Discrete Mathematics");
		testBuilder.setDescription("mathematcs that do discrete things!");
	}
}
