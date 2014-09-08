package test;

import java.util.Date;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlAssignment.LatePolicy;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlBankProblem.QuestionType;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.institution.Institution;

public class TestProblemsSmall {
	public static void testProblems() throws AuthenticationException, DatabaseAccessException {

		String assignmentId = "52d55a580364615fe8a4496d";
		String courseId = "52d55a580364615fe8a4496c";

		SrlAssignment.Builder testAssignment = SrlAssignment.newBuilder();
		testAssignment.setName("Assignment 2");
		//testBuilder.setDescription(descsription[k]);
		testAssignment.setGradeWeight("50%");
		testAssignment.setCourseId(courseId);
		assignmentId = Institution.mongoInsertAssignment("0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332", testAssignment.buildPartial());

		String[] questionText = new String[] {
				"When planning a party you want to know whom to invite. Among the people you would like to invite are three touchy friends.You know that if Jasmine attends,<br>"
				+ " she will become unhappy if Samir is there, Samir will attend only if Kanti will be there, and Kanti will not attend unless Jasmine also does."
				+ "<br>Which combinations of these three friends can you invite so as not to make someone unhappy?<br>"
				+ "Write the logic expression using S,J,K.  Write out a truth table displaying the results.<br>"
				+ " Circle the selections of friends that will work for your party. ",

				"Construct a combinatorial circuit using inverters, OR gates, and AND gates that produces the output<br>"
				+ " ((¬p ∨¬r)∧¬q) ∨ (¬p ∧ (q ∨ r)) from input bits p,q, and r."
				};
		QuestionType[] questionType = new QuestionType[] {
				QuestionType.CHECK_BOX,
				QuestionType.FREE_RESP,
				QuestionType.MULT_CHOICE,
				QuestionType.SKETCH,
				QuestionType.SKETCH
		};
		for(int k = 0; k < 20; k ++) {
			SrlBankProblem.Builder bankBuilder = SrlBankProblem.newBuilder();
			bankBuilder.setQuestionText("");
			SrlPermission.Builder permissions2 = SrlPermission.newBuilder();
			permissions2.addUserPermission(courseId);
			bankBuilder.setAccessPermission(permissions2.build());
			bankBuilder.setQuestionType(QuestionType.SKETCH);
			String resultantId = null;
			try {
				resultantId = Institution.mongoInsertBankProblem("0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332", bankBuilder.buildPartial());
			} catch (AuthenticationException e1) {
				e1.printStackTrace();
			}

			SrlProblem.Builder testBuilder = SrlProblem.newBuilder();
			testBuilder.setName("Problem " + k);
			//testBuilder.setDescription(descsription[k]);
			testBuilder.setGradeWeight("50%");
			testBuilder.setAssignmentId(assignmentId);
			testBuilder.setCourseId(courseId);
			testBuilder.setProblemBankId(resultantId);
			testBuilder.setProblemNumber(k);
			SrlPermission.Builder permissions = SrlPermission.newBuilder();

			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());
	
			// testing inserting course
				System.out.println("INSERTING PROBLEM");
				try {
					Institution.mongoInsertCourseProblem("0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332", testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING PROBLEM SUCCESSFULT");
		}
	}
	
	public static void main(String args[]) throws AuthenticationException, DatabaseAccessException {
		testProblems();
	}
}
