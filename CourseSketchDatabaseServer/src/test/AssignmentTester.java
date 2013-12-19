package test;

import java.util.Date;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.DB;

import database.institution.DatabaseAccessException;
import database.institution.RequestConverter;
import database.institution.auth.AuthenticationException;
import database.institution.managers.AssignmentManager;

public class AssignmentTester {

	public static String testAssignments(DB dbs, String courseId) throws AuthenticationException, DatabaseAccessException {
		SrlAssignment.Builder testBuilder = SrlAssignment.newBuilder();
		testBuilder.setCourseId(courseId);
		testBuilder.setName("Raising Puppies");
		testBuilder.setDescription("genetically engineered puppies that potty train themselves!");
		System.out.println("MINUS TEST " + new Date(System.currentTimeMillis()- 1000000));
		System.out.println("NORMAL TEST " + new Date(System.currentTimeMillis()));
		testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
		testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() + 1000000).getTime())));
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		permissions.addAdminPermission("david");
		permissions.addAdminPermission("larry");

		permissions.addModeratorPermission("raniero");
		permissions.addModeratorPermission("manoj");

		permissions.addUserPermission("vijay");
		permissions.addUserPermission("matt");
		permissions.addUserPermission("saby");
		permissions.addUserPermission("stephanie");
		
		testBuilder.setAccessPermission(permissions.build());
		System.out.println(testBuilder.toString());
		System.out.println("Inserting Assignments");
		String assignmentId = null;
		System.out.println("Admin");
		assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"david",testBuilder.buildPartial());
		System.out.println("Mod");
	//	testBuilder.setDescription("Added by moderator");
	//	assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"raniero",testBuilder.buildPartial());
		try
		{
			System.out.println("User");
			assignmentId = AssignmentManager.mongoInsertAssignment(dbs,"matt",testBuilder.buildPartial());
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}

		System.out.println("GETTING ASSIGNMENTS AS ADMIN");
		SrlAssignment builder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "david", System.currentTimeMillis());
		System.out.println(builder.toString());
		System.out.println("GETTING ASSIGNMENTS AS MOD");
		SrlAssignment modBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "manoj", System.currentTimeMillis());
		System.out.println(modBuilder.toString());
		System.out.println("GETTING ASSIGNMENTS AS USER");
		SrlAssignment userBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "matt", System.currentTimeMillis());
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING ASSIGNMENTS AS NO ONE");
			SrlAssignment crashBuilder = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "NO_ONE", System.currentTimeMillis());
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
		SrlAssignment.Builder updateBuilder = SrlAssignment.newBuilder();
		updateBuilder.setDescription("I HAVE A DIFFERENT DESCRIPTION NOW");
		boolean updated = AssignmentManager.mongoUpdateAssignment(dbs, assignmentId, "larry", updateBuilder.buildPartial());

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		SrlAssignment postUpdate = AssignmentManager.mongoGetAssignment(dbs, assignmentId, "david", System.currentTimeMillis());
		return assignmentId;
	}
	
}
