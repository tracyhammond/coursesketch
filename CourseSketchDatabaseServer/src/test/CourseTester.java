package test;

import java.util.ArrayList;
import java.util.Date;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.DB;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.institution.CourseManager;
import database.institution.Institution;

public class CourseTester {

	public static String testCourses(DB dbs) throws AuthenticationException, DatabaseAccessException {
		SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
		SrlCourse.Builder testBuilder1 = SrlCourse.newBuilder();
		testBuilder.setAccess(SrlCourse.Accessibility.PUBLIC);
		testBuilder.setSemester("FALL");
		testBuilder.setName("Discrete Mathematics");
		testBuilder.setDescription("mathematcs that do discrete things!");
		testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
		testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() + 1000000).getTime())));
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		permissions.addAdminPermission("larry");

		permissions.addModeratorPermission("raniero");
		permissions.addModeratorPermission("manoj");

		permissions.addUserPermission("vijay");
		permissions.addUserPermission("matt");

		testBuilder.setAccessPermission(permissions.build());
		System.out.println(testBuilder.toString());

		// testing inserting course
			System.out.println("INSERTING COURSE");
			String courseId = Institution.mongoInsertCourse("david", testBuilder.buildPartial());
			System.out.println("INSERTING COURSE SUCCESSFULT");
			System.out.println(courseId);
		// testing getting courses
			ArrayList<String> idHolder = new ArrayList<String>();
			idHolder.add(courseId);
			System.out.println("GETTING COURES AS ADMIN");
			System.out.println(Institution.mongoGetCourses(idHolder, "david"));

			System.out.println("GETTING COURES AS MOD");
			System.out.println(Institution.mongoGetCourses(idHolder, "manoj"));

			System.out.println("GETTING COURES AS USER");
			System.out.println(Institution.mongoGetCourses(idHolder, "matt"));

			try {
				System.out.println("GETTING COURES AS NO ONE");
				System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING" + Institution.mongoGetCourses(idHolder, "NO ONE"));
			} catch(AuthenticationException e) {
				System.out.println("Succesfully failed to authenticate mongo get course");
			}

		// testing updating course
		try {
			System.out.println("UPDATING COURSE AS NO ONE");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "NO_ONE", testBuilder.buildPartial());
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "vijay", testBuilder.buildPartial());
			System.out.println("SOMETHING FAILED, USER SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS MOD");
			boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "raniero", testBuilder.buildPartial());
			System.out.println("Mod can only do assignment list");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		testBuilder1.setDescription("I HAVE A DIFFERENT DESCRIPTION NOW");
		boolean updated = CourseManager.mongoUpdateCourse(dbs, courseId, "larry", testBuilder1.buildPartial());

		return courseId;
	}
	
}
