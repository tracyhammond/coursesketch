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
		String[] name = new String[]{"Problem1", "Problem2", "Problem3", "Problem4", "Problem5", "Problem6", "Problem7", "Problem8", "Problem9", "Problem10", "Problem11", "Problem12", "Problem13", "Problem14", "Problem15", "Problem16", "Problem17", 
		"Problem18", "Problem19", "Problem20", "Problem21", "Problem22", "Problem23", "Problem24", "Problem25", "Problem26", "Problem27", "Problem28", "Problem29", "Problem30", "Problem31", "Problem32", "Problem33", "Problem34", 
		"Problem35", "Problem36", "Problem37", "Problem38", "Problem39"};
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
					Chapter 7 Problem 6 a, d, f
				*/
				"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(T \u2228 W) \u2283 A, (C \u2283 \u223C B), (A \u2283 C), \u223C \u223C B \u2215 \u2234 \u223C  (T \u2228 W)",
				
				"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(\u223C S \u2283 \u223C T), B \u2283 (X \u2228 Y), (\u223C T \u2283 B), \u223C S \u2215 \u2234 X \u2228 Y",
				
				"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(A \u2219 B) \u2283 (C \u2228 D), (B \u2219 A) \u2283 (A \u2219 B), (C \u2228 D) \u2283 (D \u2228 C) \u2215 \u2234 (B \u2219 A) \u2283 (D \u2228 C)",
				/*
					Chapter 7 Problem 7 a
				*/
				"Construct proofs for the following, using only the rules for the conditional and conjunction. <br>"
				+ "(C \u2219 D) \u2283 \u223C F, (A \u2283 C) \u2219 (B \u2283 D), (A \u2219 B) \u2215 \u2234 \u223C F",
				/*
					Chapter 7 Problem 8 a, b, c, d, e, f, g, h, i, j, k, l, n
				*/
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "D \u2283 (A \u2228 C), D \u2219 \u223C A \u2215 \u2234 C",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(B \u2283 A), (C \u2283 B), \u223C A \u2215 \u2234 \u223C C",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(A  \u2228 \u223C B), (\u223C C \u2228 B), \u223C A \u2215 \u2234 \u223C C",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(A \u2228 B) \u2283 \u223C C, (C \u2228 D), A \u2215 \u2234 D",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "F \u2283 (G \u2219 \u223C H), (Z \u2283 H), F \u2215 \u2234 \u223C Z",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(\u223C A \u2219 \u223C B) \u2283 C, (A \u2283 D), (B \u2283 D), \u223C D \u2215 \u2234 C",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(\u223C F \u2228 \u223C G) \u2283 (A \u2228 B), (F \u2283 C), (B \u2283 C), \u223C C \u2215 \u2234 A",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(A \u2228 B) \u2283 C, (C \u2228 D) \u2283 (E \u2228 F), A \u2219 \u223C E \u2215 \u2234 F",
				 
				 "For the following argument(s), construct a proof of the conclusion from the given premises, "
				 + "and justify every step that is not a premise; " 
				 + "you may use any of the 8 basic rules of inference. <br>"
				 + "(F \u2228 G) \u2283 \u223C A, A \u2228 W, F \u2219 T \u2215 \u2234 W",
				 
				"For the following argument(s), construct a proof of the conclusion from the given premises, "
				+ "and justify every step that is not a premise; " 
				+ "you may use any of the 8 basic rules of inference. <br>"
				+ "(A \u2228 B) \u2283 T, Z \u2283 (A \u2228 B), T \u2283 W, \u223C W \u2215 \u2234 \u223C Z",
				 
				"For the following argument(s), construct a proof of the conclusion from the given premises, "
				+ "and justify every step that is not a premise; " 
				+ "you may use any of the 8 basic rules of inference. <br>"
				+ "\u223C A \u2283 \u223C B, A \u2283 C, Z \u2283 W, \u223C C \u2219 \u223C W \u2215 \u2234 \u223C B \u2228 W",
				 
				"For the following argument(s), construct a proof of the conclusion from the given premises, "
				+ "and justify every step that is not a premise; " 
				+ "you may use any of the 8 basic rules of inference. <br>"
				+ "(A \u2228 B) \u2283 (C \u2228 D), C \u2283 E, A \u2219 \u223C E \u2215 \u2234 D \u2228 W",
				 
				"For the following argument(s), construct a proof of the conclusion from the given premises, "
				+ "and justify every step that is not a premise; " 
				+ "you may use any of the 8 basic rules of inference. <br>"
				+ "(\u223C A \u2228 \u223C B) \u2283 \u223C G, \u223C A \u2283 (F \u2283 G), (A \u2283 D) \u2219 \u223C D \u2215 \u2234 \u223C F",
				/*
					Chapter 8 Problem 4 a, b, c
				*/
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "(A \u2228 B) \u2283 \u223C C, D \u2283 (C \u2228 C), (F \u2219 (E \u2219 D)) \u2215 \u2234 \u223C (A \u2228 B)", 
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "(B \u2219 A) \u2283 (Y \u2219 X), C \u2283 (A \u2219 B) \u2215 \u2234 C \u2283 (X \u2219 Y)",
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "\u223C (S \u2219 T) \u2283 W, W \u2283 \u223C (A \u2228 B), A \u2215 \u2234 S",
				/*
					Chapter 8 Problem 6 a, k, m, n, o, p
				*/
				"Construct proofs for the following, using any of the rules."
				+ "A \u2283 B, B \u2283 \u223C C, C \u2228 D, \u223C D \u2215 \u2234 \u223C A	",
				"Construct proofs for the following, using any of the rules."
				+ "(P \u2219 G) \u2283 R, (R \u2219 S) \u2283 T, P \u2219 S, G \u2228 R \u2215 \u2234 R \u2228 T",
				"Construct proofs for the following, using any of the rules."
				+ "(F \u2219 \u223C G) \u2228 (T \u2219 \u223C W), W \u2219 H, \u223C (F \u2283 G) \u2283 (H \u2283 S) \u2215 \u2234 \u223C S",
				"Construct proofs for the following, using any of the rules."
				+ "(A \u2228 B) \u2283 (C \u2228 D), A \u2283 \u223C C, \u223C (F \u2219 \u223C A), F \u2215 \u2234 D",
				"Construct proofs for the following, using any of the rules."
				+ "B \u2283 (C \u2283 E), E \u2283 \u223C (J \u2228 H), \u223C S, J \u2228 S \u2215 \u2234 B \u2283 \u223C C",
				"Construct proofs for the following, using any of the rules."
				+ "A \u2283 \u223C B, \u223C C \u2283 B, \u223C A \u2283 \u223C C \u2215 \u2234 A \u2261 C",
				/*
					Chapter 9 Problem 4 b, d, e
				*/
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "(\u223C A \u2228 \u223C B) \u2283 \u223C C \u2215 \u2234 C \u2283 A", 
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "(A \u2283 B) \u2283 C, A \u2283 \u223C (E \u2228 F), E \u2228 B \u2215 \u2234 A \u2283 C", 
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "P \u2283 Q, (P \u2219 Q) \u2283 R, P \u2283 (R \u2283 S), (R \u2219 S) \u2283 T \u2215 \u2234 P \u2283 T", 
				/*
					Chapter 9 Problem 5 b, d, f
				*/
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "A \u2219 \u223C B \u2215 \u2234 \u223C (A \u2261 B)",
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "A \u2283 (C \u2219 D), B \u2283 \u223C (C \u2228 F) \u2215 \u2234 \u223C (A \u2219 B)",
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "W \u2283 X, (W \u2283 Y) \u2283 (Z \u2228 X), \u223C Z \u2215 \u2234 X",
				/*
					Chapter 9 Problem 6 b
				*/
				"Construct proofs for the following, "
				+ "using any rules from Units 7-9: <br>"
				+ "A \u2283 (B \u2283 C), (C \u2219 D) \u2283 E, F \u2283 \u223C (D \u2283 E) \u2215 \u2234 A \u2283 (B \u2283 \u223C F)",
				/*
					Chapter 9 Problem 7 b, c, g, h, i, j
				*/
				"Construct the proof(s) for the following theorem(s):"
				+ "(p \u2283 (p \u2219 q)) \u2228 (q \u2283 (p \u2219 q))",
				"Construct the proof(s) for the following theorem(s):"
				+ "(p \u2283 (q \u2283 (r \u2219 s))) \u2283 ((p \u2283 q) \u2283 (p \u2283 s))",
				"Construct the proof(s) for the following theorem(s):"
				+ "((p \u2228 q) \u2283 (r \u2219 s)) \u2283 (\u223C s \u2283 \u223C p)",
				"Construct the proof(s) for the following theorem(s):"
				+ "p \u2283 (\u223C p \u2283 q)",
				"Construct the proof(s) for the following theorem(s):"
				+ "(p \u2283 q) \u2283 ((p \u2283 (q \u2283 r)) \u2283 (p \u2283 r))",
				"Construct the proof(s) for the following theorem(s):"
				+ "(p \u2283 q) \u2283 ((p \u2283 \u223C q) \u2283 \u223C p)"
				};
		QuestionType[] questionType = new QuestionType[] {
				QuestionType.CHECK_BOX,
				QuestionType.FREE_RESP,
				QuestionType.MULT_CHOICE,
				QuestionType.SKETCH,
				QuestionType.SKETCH
		};
		for(int k = 0; k < 39; k ++) {
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
