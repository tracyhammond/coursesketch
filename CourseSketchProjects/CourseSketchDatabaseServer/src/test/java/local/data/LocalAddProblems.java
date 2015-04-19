package local.data;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.utils.Util.QuestionType;
import protobuf.srl.utils.Util.SrlPermission;
import protobuf.srl.school.School.SrlProblem;

public class LocalAddProblems {
	public static void testProblems(String courseId, String assignmentId, String mastId) {

		String[] bankIds = new String[] ();

		for(int k = 0; k < 50; k ++) {

			SrlProblem.Builder testBuilder = SrlProblem.newBuilder();
			testBuilder.setName("Problem " + (k+1));
			testBuilder.setAssignmentId(assignmentId);
			testBuilder.setCourseId(courseId);
			testBuilder.setProblemBankId(bankIds[k]);
			/*
			SrlPermission.Builder permissions = SrlPermission.newBuilder();

			testBuilder.setAccessPermission(permissions.build());
			*/
			System.out.println(testBuilder.toString());

			// testing inserting course
				System.out.println("INSERTING PROBLEM");
				try {
					MongoInstitution.getInstance().insertCourseProblem(mastId, testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING PROBLEM SUCCESSFUL"); /*SUCCESSFULT*/
		}
	}

	public static void main(String args[]) {
		new MongoInstitution(false, null); // makes the database point locally
		new UserClient(false, null); // makes the database point locally
	}
 }
