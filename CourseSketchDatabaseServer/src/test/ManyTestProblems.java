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
		String[] name = new String[]{"Problem1", "Problem2", "Problem3", "Problem4", "Problem5"};
		String[] descsription = new String[]{"This is the first problem",
				
				"This is the second problem",
				
				"This is the last problem",
				
				"Wait i added this problem",
						
				"blah blah blah"};
		String[] questionText = new String[] {
				"The question text for this problem is seen here",
				"The question text for this problem is now a question text",
				"Yeah yeah yeah question text",
				"Hey you count to 10",
				"Draw a kitty kat!"};
		QuestionType[] questionType = new QuestionType[] {
				QuestionType.CHECK_BOX,
				QuestionType.FREE_RESP,
				QuestionType.MULT_CHOICE,
				QuestionType.SKETCH,
				QuestionType.SKETCH
		};
		for(int k = 0; k < 5; k ++) {
			SrlBankProblem.Builder bankBuilder = SrlBankProblem.newBuilder();
			bankBuilder.setQuestionText(questionText[k]);
			SrlPermission.Builder permissions2 = SrlPermission.newBuilder();
			permissions2.addAdminPermission("larry");
			permissions2.addUserPermission(courseId);
			bankBuilder.setAccessPermission(permissions2.build());
			bankBuilder.setQuestionType(questionType[k]);
			String resultantId = null;
			try {
				resultantId = Institution.mongoInsertBankProblem("david", bankBuilder.buildPartial());
			} catch (AuthenticationException e1) {
				e1.printStackTrace();
			}
			
			SrlProblem.Builder testBuilder = SrlProblem.newBuilder();
			testBuilder.setName(name[k]);
			testBuilder.setDescription(descsription[k]);
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
					Institution.mongoInsertCourseProblem("david", testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING PROBLEM SUCCESSFULT");
		}
	}
}
