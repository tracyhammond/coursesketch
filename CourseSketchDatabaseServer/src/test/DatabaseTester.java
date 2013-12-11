package test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.RequestConverter;
import database.assignment.AssignmentBuilder;
import database.assignment.AssignmentManager;
import database.auth.AuthenticationException;
import database.course.CourseManager;

public class DatabaseTester {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException 
	{
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("test");
		String returnId = testCourses(db);
		//testAssignments(db,returnId);
	}

	public static DB getDatabase() {
		return null;
	}

	public static String testCourses(DB dbs) throws AuthenticationException {
		SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
		SrlCourse.Builder testBuilder1 = SrlCourse.newBuilder();
		testBuilder.setAccess(SrlCourse.Accessibility.PUBLIC);
		testBuilder.setSemester("FALL");
		testBuilder.setName("Discrete Mathematics");
		testBuilder.setDescription("mathematcs that do discrete things!");
		testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
		testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() + 1000000).getTime())));
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		permissions.addAdminPermission("david");
		permissions.addAdminPermission("larry");

		permissions.addModeratorPermission("raniero");
		permissions.addModeratorPermission("manoj");

		permissions.addUserPermission("vijay");
		permissions.addUserPermission("matt");

		testBuilder.setAccessPermission(permissions.build());
		System.out.println(testBuilder.toString());

		System.out.println("INSERTING COURSE");
		String courseId = CourseManager.mongoInsertCourse(dbs, testBuilder.buildPartial());
		System.out.println("INSERTING COURSE SUCCESSFULT");
		System.out.println(courseId);
		// testing getting courses

		System.out.println("GETTING COURES AS ADMIN");
		SrlCourse builder = CourseManager.mongoGetCourse(dbs, courseId, "david", System.currentTimeMillis());
		System.out.println(builder.toString());
		System.out.println("GETTING COURES AS MOD");
		SrlCourse modBuilder = CourseManager.mongoGetCourse(dbs, courseId, "manoj", System.currentTimeMillis());
		System.out.println(modBuilder.toString());
		System.out.println("GETTING COURES AS USER");
		SrlCourse userBuilder = CourseManager.mongoGetCourse(dbs, courseId, "matt", System.currentTimeMillis());
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING COURES AS NO ONE");
			SrlCourse crashBuilder = CourseManager.mongoGetCourse(dbs, courseId, "NO_ONE", System.currentTimeMillis());
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
		testBuilder1.setDescription("I HAVE A DIFFERENT DESCRIPTION NOW");
		boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "larry", testBuilder1.buildPartial());

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		SrlCourse postUpdate = CourseManager.mongoGetCourse(dbs, courseId, "david", System.currentTimeMillis());
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
