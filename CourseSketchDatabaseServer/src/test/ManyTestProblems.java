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
				"Please draw a picture of yourself",
				"Prove that the square root of 3 is irrational.",
				"Suppose that Smartphone A has 256MBRAM and 32GB ROM, and the resolution of its camera is 8 MP; Smartphone B has 288 MB RAM and 64 GB ROM,"
				+ " and the resolution of its camera is 4 MP;"
				+ " and Smartphone C has 128 MB RAM and 32 GB ROM, and the resolution of its camera is 5 MP."
				+ " Determine the truth value of the following proposition. Show your work (using a truth table). <br>" +
				"\"Smartphone A has more RAM than Smartphone B if and only if Smartphone B has more RAM than Smartphone A.\"",
				"Let p and q be the propositions “The election is decided” and \"The votes have been counted,\" respectively. Express the following compound proposition as an English sentence: ~q ∨ (~p ∧ q)",
				
				"Let p, q, and r be the propositions<br>" +
				"p :You get an A on the final exam.<br>" +
				"q :You do every exercise in this book.<br>" +
				"r :You get an A in this class.<br>" +
				"Write the following statement in terms of p,q,r. \"You will get an A in this class if and only if you either" +
				"do every exercise in this book or you get an A on the final.\"",
				
				"Write this statement in the form of if p, then q. \"A sufficient condition for the warranty to be good is that you bought the computer less than a year ago.\"",
				
				"Construct a truth table for (p ↔ q) ⊕ (p ↔~q)",
				
				"Construct a truth table for (p AND q) AND ~(p OR q)",
				
				"Evaluate this using bit operators <br>" +
				 "(1 1011 XOR 0 1010) AND (1 0001 OR 1 1011)",

				 "p: John is happy = .6, q: Alex is happy = .7, r: Samantha is happy = .2<br>" +
				 "Evaluate the value of this statement using fuzzy logic.   Either John and Alex are happy, or Samantha isn't.",
				 
				 "Create a combinatorial circuit that is equivalent to p --> q"
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
			permissions2.addAdminPermission("larry");
			permissions2.addUserPermission(courseId);
			bankBuilder.setAccessPermission(permissions2.build());
			bankBuilder.setQuestionType(QuestionType.SKETCH);
			String resultantId = null;
			try {
				resultantId = Institution.mongoInsertBankProblem("david", bankBuilder.buildPartial());
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
