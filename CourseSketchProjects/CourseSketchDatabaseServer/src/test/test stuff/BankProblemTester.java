package test;

import java.util.Date;

import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.Problem.SrlBankProblem.QuestionType;
import protobuf.srl.utils.Util.SrlPermission;

import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.institution.BankProblemManager;
import coursesketch.database.institution.Institution;

public class BankProblemTester {

	public static String testBankProblems(String courseId) throws AuthenticationException {
		SrlBankProblem.Builder testBuilder = SrlBankProblem.newBuilder();
		testBuilder.setQuestionType(QuestionType.SKETCH);
		testBuilder.setQuestionText("genetically engineered puppies that potty train themselves!");
		testBuilder.setCourseTopic("Course Topic");
		testBuilder.setSubTopic("Sub topic of course");
		testBuilder.setSource("Source location");
		System.out.println("MINUS TEST " + new Date(System.currentTimeMillis()- 1000000));
		System.out.println("NORMAL TEST " + new Date(System.currentTimeMillis()));
		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		permissions.addAdminPermission("david");
		permissions.addAdminPermission("larry");

		permissions.addUserPermission(courseId);

		testBuilder.setAccessPermission(permissions.build());
		System.out.println(testBuilder.toString());
		System.out.println("Inserting BankProblem");
		String problemId = null;
		try{
			System.out.println("Admin");
			problemId = Institution.mongoInsertBankProblem("david", testBuilder.buildPartial());
		}
		catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get assignment");
		}

		/*
		System.out.println("GETTING Problem AS ADMIN");
		SrlBankProblem builder = BankProblemManager.mongoGetBankProblem(dbs, problemId, "david");
		System.out.println(builder.toString());
		System.out.println("GETTING Problem AS USER");
		SrlBankProblem userBuilder = BankProblemManager.mongoGetBankProblem(dbs, problemId, courseId);
		System.out.println(userBuilder.toString());
		try {
			System.out.println("GETTING ASSIGNMENTS AS NO ONE");
			SrlBankProblem crashBuilder = BankProblemManager.mongoGetBankProblem(dbs, problemId, "NO_ONE");
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING" + crashBuilder.toString());
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate mongo get course");
		}

		try {
			System.out.println("UPDATING ASSIGNMENTS AS NO ONE");
			boolean updated = BankProblemManager.mongoUpdateBankProblem(dbs, problemId, "NO_ONE", userBuilder);
			System.out.println("SOMETHING FAILED, NO ONE SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		try {
			System.out.println("UPDATING COURSE AS USER");
			boolean updated = BankProblemManager.mongoUpdateBankProblem(dbs, problemId, courseId, userBuilder);
			System.out.println("SOMETHING FAILED, USER SHOULD HAVE NOTHING");
		} catch(AuthenticationException e) {
			System.out.println("Succesfully failed to authenticate");
		}

		System.out.println("UPDATING COURSE AS ADMIN");
		SrlBankProblem.Builder updateBuilder = SrlBankProblem.newBuilder();
		updateBuilder.setQuestionText("I HAVE A DIFFERENT DESCRIPTION NOW");
		boolean updated = BankProblemManager.mongoUpdateBankProblem(dbs, problemId, "larry", updateBuilder.buildPartial());
		*/
		return problemId;
	}
}
