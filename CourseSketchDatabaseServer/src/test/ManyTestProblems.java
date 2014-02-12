package test;

import java.util.Date;

import protobuf.srl.school.School.SrlAssignment.LatePolicy;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlBankProblem.QuestionType;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.institution.Institution;

public class ManyTestProblems {
	public static void testProblems(String courseId, String assignmentId) {
		String[] name = new String[]{"Problem1", "Problem2", "Problem3", "Problem4", "Problem5", "Problem6", "Problem7", "Problem8", "Problem9", "Problem10", "Problem11"};
		//10 is fuzzy logic
		/*
		String[] descsription = new String[]{"This is the first problem",
				
				"This is the second problem",
				
				"This is the last problem",
				
				"Wait i added this problem",
						
				"blah blah blah"};
		*/
		String[] questionText = new String[] {
				/*
					Problem 6 a, d, f
				*/
				"For the following argument, construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(T \u2228 W) \u2283 A, (C \u2283 \u223C B), (A \u2283 C), \u223C \u223C B \u2215 \u2234 \u223C  (T \u2228 W)",
				
				"For the following argument, construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(\u223C S \u2283 \u223C T), B \u2283 (X \u2228 Y), (\u223C T \u2283 B), \u223C S \u2215 \u2234 X \u2228 Y",
				
				"For the following argument, construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(A \u2219 B) \u2283 (C \u2228 D), (B \u2219 A) \u2283 (A \u2219 B), (C \u2228 D) \u2283 (D \u2228 C) \u2215 \u2234 (B \u2219 A) \u2283 (D \u2228 C)",
				/*
					Problem 7 a
				*/
				"Construct proofs for the following, using only the rules for the conditional and conjunction. <br>"
				+ "(C \u2219 D) \u2283 \u223C F, (A \u2283 C) \u2219 (B \u2283 D), (A \u2219 B) \u2215 \u2234 \u223C F",
				/*
					Problem 8 a, b, c, d, e, f, g, h, i, j, k, l, n
				*/
				 
				 
				};
		QuestionType[] questionType = new QuestionType[] {
				QuestionType.CHECK_BOX,
				QuestionType.FREE_RESP,
				QuestionType.MULT_CHOICE,
				QuestionType.SKETCH,
				QuestionType.SKETCH
		};
		for(int k = 0; k < 11; k ++) {
			SrlBankProblem.Builder bankBuilder = SrlBankProblem.newBuilder();
			bankBuilder.setQuestionText(questionText[k]);
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
			testBuilder.setName(name[k]);
			//testBuilder.setDescription(descsription[k]);
			testBuilder.setGradeWeight("50%");
			testBuilder.setAssignmentId(assignmentId);
			testBuilder.setCourseId(courseId);
			testBuilder.setProblemBankId(resultantId);
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAdminPermission("larry");
	
			permissions.addModeratorPermission("raniero");
			permissions.addModeratorPermission("manoj");
	
			permissions.addUserPermission("vijay");
			permissions.addUserPermission("matt");
	
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
}
