package test;

import java.util.Date;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.DB;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.course.CourseManager;

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
	
}
