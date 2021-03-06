package test;

import protobuf.srl.utils.Util.SrlPermission;
import protobuf.srl.school.Problem.SrlBankProblem;

import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.institution.CourseProblemManager;
import coursesketch.database.institution.Institution;

public class CourseProblemTester {

	public static String testCourseProblems(String courseId, String assignmentId, String problemBankId) throws AuthenticationException, DatabaseAccessException {
		SrlProblem.Builder testBuilder = SrlProblem.newBuilder();
		testBuilder.setCourseId(courseId);
		testBuilder.setAssignmentId(assignmentId);
		testBuilder.setProblemBankId(problemBankId);
		testBuilder.setName("Raising Puppies");
		testBuilder.setDescription("genetically engineered puppies that potty train themselves!");
		testBuilder.setGradeWeight("either points or percent");
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		/*
		permissions.addAdminPermission("david");
		permissions.addAdminPermission("larry");

		permissions.addModeratorPermission("raniero");
		permissions.addModeratorPermission("manoj");

		permissions.addUserPermission("vijay");
		permissions.addUserPermission("matt");
		permissions.addUserPermission("saby");
		permissions.addUserPermission("stephanie");
		*/

		testBuilder.setAccessPermission(permissions.build());
		System.out.println(testBuilder.toString());
		// testing insertion
		System.out.println("Inserting PROBLEMS");
		String problemId = null;
		System.out.println("Admin");
		problemId = Institution.mongoInsertCourseProblem("david",testBuilder.buildPartial());
		testBuilder.setDescription("Added by moderator");
		System.out.println("Mod");
		problemId = Institution.mongoInsertCourseProblem("raniero",testBuilder.buildPartial());

		try
		{
			System.out.println("User");
			problemId = Institution.mongoInsertCourseProblem("matt",testBuilder.buildPartial());
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}
		/*
		System.out.println("GETTING PROBLEMS AS ADMIN");
		SrlProblem builder = CourseProblemManager.mongoGetCourseProblem(dbs, problemId, "david", System.currentTimeMillis());
		System.out.println(builder.toString());
		System.out.println("GETTING PROBLEMS AS MOD");
		SrlProblem modBuilder = CourseProblemManager.mongoGetCourseProblem(dbs, problemId, "manoj", System.currentTimeMillis());
		System.out.println(modBuilder.toString());
		System.out.println("GETTING PROBLEMS AS USER");
		SrlProblem userBuilder = CourseProblemManager.mongoGetCourseProblem(dbs, problemId, "matt", System.currentTimeMillis());
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING PROBLEMS AS NO ONE");
			SrlProblem crashBuilder = CourseProblemManager.mongoGetCourseProblem(dbs, problemId, "NO_ONE", System.currentTimeMillis());
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING" + crashBuilder.toString());
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}

		try {
			System.out.println("UPDATING PROBLEMS AS NO ONE");
			boolean updated = CourseProblemManager.mongoUpdateCourseProblem(dbs, problemId, "NO_ONE", userBuilder);
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = CourseProblemManager.mongoUpdateCourseProblem(dbs, problemId, "vijay", userBuilder);
			System.out.println("SOMETHING FAILED, USER SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS MOD");
			boolean updated = CourseProblemManager.mongoUpdateCourseProblem(dbs, problemId, "raniero", userBuilder);
			System.out.println("Mod can only do assignment list");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		SrlProblem.Builder updateBuilder = SrlProblem.newBuilder();
		updateBuilder.setDescription("I HAVE A DIFFERENT DESCRIPTION NOW");
		boolean updated = CourseProblemManager.mongoUpdateCourseProblem(dbs, problemId, "larry", updateBuilder.buildPartial());

		System.out.println("GETTING UPDATED COURSE AS ADMIN");
		SrlProblem postUpdate = CourseProblemManager.mongoGetCourseProblem(dbs, problemId, "david", System.currentTimeMillis());
		*/
		return problemId;
	}

}
