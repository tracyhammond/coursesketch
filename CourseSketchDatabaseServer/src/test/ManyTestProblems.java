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
		String[] name = new String[]{"Problem1", "Problem2", "Problem3", "Problem4", "Problem5", "Problem6", "Problem7", "Problem8", "Problem9", "Problem10", "Problem11", "Problem12", "Problem13", "Problem14", "Problem15", "Problem16", 
		"Problem17", "Problem18", "Problem19", "Problem20", "Problem21", "Problem22", "Problem23", "Problem24", "Problem25", "Problem26", "Problem27", "Problem28", "Problem29", "Problem30", "Problem31", "Problem32", "Problem33", "Problem34", 
		"Problem35", "Problem36", "Problem37", "Problem38", "Problem39", "Problem40", "Problem41", "Problem42"/*, "Problem43", "Problem44", "Problem45", "Problem46", "Problem47", "Problem48", "Problem49"*/};
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
					Assignment 6
				*/
				"2.4.2a: What is the term a<sub>8</sub> of the sequence {a<sub>n</sub>} if a<sub>n</sub> = 2n-1?",
				"2.4.2b: What is the term a<sub>8</sub> of the sequence {a<sub>n</sub>} if a<sub>n</sub> = 7?",
				"2.4.2c: What is the term a<sub>8</sub> of the sequence {a<sub>n</sub>} if a<sub>n</sub> = 1+(-1)<sup>n</sup>?",
				"2.4.2d: What is the term a<sub>8</sub> of the sequence {a<sub>n</sub>} if a<sub>n</sub> = -(-2)<sup>n</sup>?",
				"2.4.4a: What are the terms a<sub>0</sub>,a<sub>1</sub>,a<sub>2</sub>,and a<sub>3</sub> of the sequence {a<sub>n</sub>} where a<sub>n</sub> = (-2)<sup>n</sup>?",
				"2.4.4b: What are the terms a<sub>0</sub>,a<sub>1</sub>,a<sub>2</sub>,and a<sub>3</sub> of the sequence {a<sub>n</sub>} where a<sub>n</sub> = 3?",
				"2.4.4c: What are the terms a<sub>0</sub>,a<sub>1</sub>,a<sub>2</sub>,and a<sub>3</sub> of the sequence {a<sub>n</sub>} where a<sub>n</sub> = 7+4<sup>n</sup>?",
				"2.4.4d: What are the terms a<sub>0</sub>,a<sub>1</sub>,a<sub>2</sub>,and a<sub>3</sub> of the sequence {a<sub>n</sub>} where a<sub>n</sub> = 2<sup>n</sup>+(-2)<sup>n</sup>?",
				"2.4.10a: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. a<sub>n</sub>=-2, a<sub>n-1</sub>a<sub>0</sub>=-1",
				"2.4.10b: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. a<sub>n-1</sub>-a<sub>n-2</sub>, a<sub>0</sub>=2, a<sub>1</sub>=-1",
				"2.4.10c: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. 3a<sub>n-1</sub><sup>2</sup>, a<sub>0</sub>=1",
				"2.4.12a: Show that the sequence {a<sub>n</sub>} is a solution of the recurrence relation a<sub>n</sub>=-3a<sub>n-1</sub>+4a<sub>n-2</sub> if a<sub>n</sub> = 0",
				"2.4.12b: Show that the sequence {a<sub>n</sub>} is a solution of the recurrence relation a<sub>n</sub>=-3a<sub>n-1</sub>+4a<sub>n-2</sub> if a<sub>n</sub> = 1",
				"2.4.12c: Show that the sequence {a<sub>n</sub>} is a solution of the recurrence relation a<sub>n</sub>=-3a<sub>n-1</sub>+4a<sub>n-2</sub> if a<sub>n</sub> = (-4)<sup>n</sup>",
				"2.4.12d: Show that the sequence {a<sub>n</sub>} is a solution of the recurrence relation a<sub>n</sub>=-3a<sub>n-1</sub>+4a<sub>n-2</sub> if a<sub>n</sub> = 2(-4)<sup>n</sup>+3",
				"2.4.16a: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = -a<sub>n-1</sub>, a<sub>0</sub>=5",
				"2.4.16b: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = a<sub>n-1</sub>+2, a<sub>0</sub>=3",
				"2.4.16c: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = a<sub>n-1</sub>-n, a<sub>0</sub>=4",
				"2.4.16d: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = 2a<sub>n-1</sub>-3, a<sub>0</sub>=-1",
				"2.4.16e: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = (n+1)a<sub>n-1</sub>, a<sub>0</sub>=2",
				"2.4.16f: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> = 2na<sub>n-1</sub>, a<sub>0</sub>=3",
				"2.4.16g: Find the solution to each of these recurrence relations with the given initial conditions: a<sub>n</sub> =-a<sub>n-1</sub>+n-1, a<sub>0</sub>=7",
				"2.4.26a: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 3,6,11,18,27,38,51,66,83,102,...",
				"2.4.26b: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.7,11,15,19,23,27,31,35,39,43,...",
				"2.4.26c: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,10,11,100,101,110,111,1000,1001,1010,1011,...",
				"2.4.26d: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,2,2,2,3,3,3,3,3,5,5,5,5,5,...",
				"2.4.26e: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.0,2,8,26,80,242,728,2186,6560,19682,...",
				"2.4.26f: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,3,15,105,945,10395,135135,2027025,34459425,...", 
				"2.4.26g: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.1,0,0,1,1,1,0,0,0,0,1,1,1,1,1,...",
				"2.4.26h: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.2,4,16,256,65536,4294967296,...",
				"2.4.30a: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>j ",
				"2.4.30b: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>j<sup>2</sup>",
				"2.4.30c: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>1/j",
				"2.4.30d: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>1",
				"2.4.32a: Find the value of each of these sums \u2211<sub>j=0</sub> 1+(-1)<sup>j</sup>",
				"2.4.32b: Find the value of each of these sums \u2211<sub>j=0</sub> 3<sup>j</sup>-2<sup>j</sup>",
				"2.4.32c: Find the value of each of these sums \u2211<sub>j=0</sub> 2*3<sup>j</sup>+3*2<sup>j</sup>",
				"2.4.32d: Find the value of each of these sums \u2211<sub>j=0</sub> 2<sup>j+1</sup>-2<sup>j</sup>",
				"2.4.34a: Compute each of these double sums. \u2211<sub>i=1</sub><sup>3</sup>\u2211<sub>j=1</sub><sup>2</sup> (i-j)",
				"2.4.34b: Compute each of these double sums. \u2211<sub>i=0</sub><sup>3</sup>\u2211<sub>j=0</sub><sup>2</sup> (3i+2j)",
				"2.4.34c: Compute each of these double sums. \u2211<sub>i=1</sub><sup>3</sup>\u2211<sub>j=0</sub><sup>2</sup> j",
				"2.4.34d: Compute each of these double sums. \u2211<sub>i=0</sub><sup>2/sup>\u2211<sub>j=0</sub><sup>3</sup>  i<sup>2</sup>j<sup>3</sup>"
				
				/*
				/*
					Assignment 5
				/*
				"Given that the domain = codomain = {a,b,c,d}. " 
				+"Is the following function one-to-one? "
				+"f(a)=b, f(b) = a, f(c) = c, f(d) = d ",
				
				"Given that the domain=codomain = {a,b,c,d}. "
				+"Is the following function one-to-one? "
				+"f(a)=b, f(b) = b, f(c) = d, f(d) = c",
				
				"Given that the domain=codomain = {a,b,c,d}. "
				+"Is the following function one-to-one? "
				+"f(a)=d, f(b) = b, f(c) = c, f(d) = d",
				
				"Is the following function from Z to Z one-to one: "
				+"f(n) = n-1",
				
				"Is the following function from Z to Z one-to one: "
				+"f(n) = n\u00B3",
				
				"Is the following function from Z to Z one-to one: "
				+"f(n) = n\u00B2 + 1",
				
				"Is the following function from Z to Z one-to one: f(n) =  "
				+"f(n) = \u2308n/2\u2309",
				
				"Determine whether f: Z x Z \u2192 Z is onto if: "
				+"f(m,n) = m + n",
				
				"Determine whether f: Z x Z \u2192 Z is onto if: "
				+"f(m,n) = m\u00B2 + n\u00B2",
				
				"Determine whether f: Z x Z \u2192 Z is onto if: "
				+"f(m,n) = m",
				
				"Determine whether f: Z x Z \u2192 Z is onto if: "
				+"f(m,n) = |n|",
				
				"Determine whether f: Z x Z \u2192 Z is onto if: "
				+"f(m,n) = m - n",
				
				"Determine whether the following function from R to R is a bijection: "
				+"f(x) = -3x + 4",
				
				"Determine whether the following function from R to R is a bijection: "
				+"f(x) = -3x\u00B2 + 7",
				
				"Determine whether the following function from R to R is a bijection: "
				+"f(x) = (x + 1)/(x + 2)",
				
				"Determine whether the following function from R to R is a bijection: "
				+"f(x) = x\u00B5 + 1",
				
				"Find f \u2218 g where f(x) = x\u00B2 + 1 and g(x) = x + 2 are functions from R to R.",
				
				"Find g \u2218 f where f(x) = x\u00B2 + 1 and g(x) = x + 2 are functions from R to R.",
				
				"Draw a graph of the function: f(x) = 1-n\u00B2 ",
				
				"Draw a graph of the function: f(x) = \u230A2x\u230B",
				
				"Draw a graph of the function: "
				+"f(x) = \u2308x\u2309 + \u230Ax/2\u230B",
				
				"Let x be a real number, show that "
				+"\u230A3x\u230B = \u230Ax\u230B + \u230Ax + 1/3\u230B + \u230Ax + 2/3\u230B",
				*/		
				
				/*
					Assignment 4
				*/
				/*"List the members of the set: <br>"
				+"[x | x is a positive integer less than 4]",//1
				
				"Are the two sets equal: <br>"
				+"{{1}}, {1,{1}}?",//2
				
				"Are the following two subsets equal? <br>"
				+"{1,3,3,3,5,5,5,5,5,5}, {5,3,1}",//3
				
				"Is the following statement true or false: <br>"
				+"{x} is an element of the set {x}",//4
				
				"Is two an element of the following set: <br>"
				+"{2,{2}}",//5
				
				"Is two an element of the following set: <br>"
				+"{{2},{{2}}}",//6
				
				"Use a Venn diagram to illustrate the subset of odd integers " 
				+"in the set of all positive integers not exceeding 10.",//7
				
				"Let A = {a,d} and B = {x,y,z}. "
				+"Find A x B",//8
				
				"State if the following statement is true or false: <br>"
				+"{0} is an element of the set {0}",//9
				
				"State if the following statement is true or false: <br>"
				+"x is an element of the set {x}",//10
				
				"What is the powerset of the following set: <br>"
				+"{a}",//11
				
				"Let A = {a,b,c,d,e} and B = {a,b,c,d}. "
				+"Find A union B.",//12
				
				"Let A = {a,b,c,d,e} and B = {a,b,c,d}. "
				+"Find A intersect B.",//13
				
				"Let A = {a,b,c,d,e} and B = {a,b,c,d}. "
				+"Find A - B.",//14
				
				"Let A = {a,b,c,d,e} and B = {a,b,c,d}. "
				+"Find B - A .",//15
				
				"Prove the identity laws in table 1 (without those laws) "
				+"by showing that A union the null set is A. ",//16
				
				"Prove the identity laws in table 1 (without those laws) "
				+"by showing that A intersected with the universe set is A. ",//17
				
				"Using the laws, show that (A - B) - C is a subset of A - C.",//18
				
				"Let A, B, C be sets. "
				+"Show that (A - B) - C = (A - C) - (B - C)."//19*/
					
				/*
					Chapter 7 Problem 6 b, d, f
				*/
				/*"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(T \u2228 W) \u2283 A, (C \u2283 \u223C B), (A \u2283 C), \u223C \u223C B \u2215 \u2234 \u223C  (T \u2228 W)",
				
				"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(\u223C S \u2283 \u223C T), B \u2283 (X \u2228 Y), (\u223C T \u2283 B), \u223C S \u2215 \u2234 X \u2228 Y",
				
				"For the following argument(s), construct a proof of the conclusion from the premises, " 
				+ "using only the rules of M.P., M.T., and H.S. (Please be sure to justify every step): <br>" 
				+ "(A \u2219 B) \u2283 (C \u2228 D), (B \u2219 A) \u2283 (A \u2219 B), (C \u2228 D) \u2283 (D \u2228 C) \u2215 \u2234 (B \u2219 A) \u2283 (D \u2228 C)",*/
				/*
					Chapter 7 Problem 7 a
				*/
				/*
				"Construct proofs for the following, using only the rules for the conditional and conjunction. <br>"
				+ "(C \u2219 D) \u2283 \u223C F, (A \u2283 C) \u2219 (B \u2283 D), (A \u2219 B) \u2215 \u2234 \u223C F",
				*/
				/*
					Chapter 7 Problem 8 a, b, c, d, e, f, g, h, i, j, k, l, n
				*/
				/*
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
				*/
				/*
					Chapter 8 Problem 4 a, b, c
				*/
				/*
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "(A \u2228 B) \u2283 \u223C C, D \u2283 (C \u2228 C), (F \u2219 (E \u2219 D)) \u2215 \u2234 \u223C (A \u2228 B)", 
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "(B \u2219 A) \u2283 (Y \u2219 X), C \u2283 (A \u2219 B) \u2215 \u2234 C \u2283 (X \u2219 Y)",
				"Construct proofs for the following, "
				+ "using the 8 basic rules from Unit 7 plus D.N., Com., Assoc., and Dup. <br>"
				+ "\u223C (S \u2219 T) \u2283 W, W \u2283 \u223C (A \u2228 B), A \u2215 \u2234 S",
				*/
				/*
					Chapter 8 Problem 6 a, k, m, n, o, p
				*/
				/*
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
				*/
				/*
					Chapter 9 Problem 4 b, d, e
				*/
				/*
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "(\u223C A \u2228 \u223C B) \u2283 \u223C C \u2215 \u2234 C \u2283 A", 
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "(A \u2283 B) \u2283 C, A \u2283 \u223C (E \u2228 F), E \u2228 B \u2215 \u2234 A \u2283 C", 
				"Construct proofs for the following, "
				+ "using the rule of C.P. plus the rules from Units 7 & 8: <br>"
				+ "P \u2283 Q, (P \u2219 Q) \u2283 R, P \u2283 (R \u2283 S), (R \u2219 S) \u2283 T \u2215 \u2234 P \u2283 T", 
				*/
				/*
					Chapter 9 Problem 5 b, d, f
				*/
				/*
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "A \u2219 \u223C B \u2215 \u2234 \u223C (A \u2261 B)",
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "A \u2283 (C \u2219 D), B \u2283 \u223C (C \u2228 F) \u2215 \u2234 \u223C (A \u2219 B)",
				"Construct proofs for the following, "
				+ "using the rule of I.P. plus the rules from Units 7 & 8: <br>"
				+ "W \u2283 X, (W \u2283 Y) \u2283 (Z \u2228 X), \u223C Z \u2215 \u2234 X",
				*/
				/*
					Chapter 9 Problem 6 b
				*/
				/*
				"Construct proofs for the following, "
				+ "using any rules from Units 7-9: <br>"
				+ "A \u2283 (B \u2283 C), (C \u2219 D) \u2283 E, F \u2283 \u223C (D \u2283 E) \u2215 \u2234 A \u2283 (B \u2283 \u223C F)",
				*/
				/*
					Chapter 9 Problem 7 b, c, g, h, i, j
				*/
				/*
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
				*/
				};
		QuestionType[] questionType = new QuestionType[] {
				QuestionType.CHECK_BOX,
				QuestionType.FREE_RESP,
				QuestionType.MULT_CHOICE,
				QuestionType.SKETCH,
				QuestionType.SKETCH
		};
		for(int k = 0; k < 42/*22*//*19*//*39*/; k ++) {
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
