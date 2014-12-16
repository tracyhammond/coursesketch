package local.data;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlBankProblem.QuestionType;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.SrlProblem;

public class LocalAddProblems {
	public static void testProblems(String courseId, String assignmentId, String mastId) {
		String[] name = new String[]{"Problem1", "Problem2", "Problem3", "Problem4", "Problem5", "Problem6", "Problem7", "Problem8", "Problem9", "Problem10", "Problem11", "Problem12", "Problem13", "Problem14", "Problem15", "Problem16",
		"Problem17", "Problem18", "Problem19", "Problem20", "Problem21", "Problem22", "Problem23", "Problem24", "Problem25", "Problem26", "Problem27", "Problem28", "Problem29", "Problem30", "Problem31", "Problem32", "Problem33", "Problem34",
		"Problem35", "Problem36", "Problem37", "Problem38", "Problem39", "Problem40", "Problem41" /*, "Problem42", "Problem43", "Problem44", "Problem45", "Problem46", "Problem47", "Problem48", "Problem49", "Problem50", "Problem51", "Problem52"*/};
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
					Assignment 8
				*/

					"5.1.4a: Let P(n) be the statement that 1 <sup>3</sup> + 2<sup>3</sup> + ... + n <sup>3</sup> = (n(n+1)/2) <sup>2</sup> for the positive integer n. Fill in the corresponding steps: (Basis)",

					"5.1.4b: Let P(n) be the statement that 1 <sup>3</sup> + 2<sup>3</sup> + ... + n <sup>3</sup> = (n(n+1)/2) <sup>2</sup> for the positive integer n. Fill in the corresponding steps: (Want to Show) ",

					"5.1.4c: Let P(n) be the statement that 1 <sup>3</sup> + 2<sup>3</sup> + ... + n <sup>3</sup> = (n(n+1)/2) <sup>2</sup> for the positive integer n. Fill in the corresponding steps: (Assume)",

					"5.1.4d: Let P(n) be the statement that 1 <sup>3</sup> +2 <sup>3</sup> + ... + n <sup>3</sup> = (n(n+1)/2) <sup>2</sup> for the positive integer n. Fill in the corresponding steps: (Induction)",


					"5.1.6a: Prove that 1*1!+2*2!+...+n*n!= (n+1)!-1 whenever n is a positive integer. Fill in the corresponding steps: (Basis) ",

					"5.1.6b: Prove that 1*1!+2*2!+...+n*n!= (n+1)!-1 whenever n is a positive integer. Fill in the corresponding steps: (Want to Show) ",

					"5.1.6c: Prove that 1*1!+2*2!+...+n*n!= (n+1)!-1 whenever n is a positive integer. Fill in the corresponding steps: (Assume)",

					"5.1.6d: Prove that 1*1!+2*2!+...+n*n!= (n+1)!-1 whenever n is a positive integer. Fill in the corresponding steps: (Induction)",


					"5.1.8a: Prove that 2-2*7+2*7 <sup>2</sup> -…+2(-7) <sup>n</sup> = (1-(-7) <sup>n+1</sup> )/4 whenever n is a nonnegative integer. Fill in the corresponding steps: (Basis) ",

					"5.1.8b: Prove that 2-2*7+2*7 <sup>2</sup> -…+2(-7) <sup>n</sup> = (1-(-7) <sup>n+1</sup> )/4 whenever n is a nonnegative integer. Fill in the corresponding steps: (Want to Show) ",

					"5.1.8c: Prove that 2-2*7+2*7 <sup>2</sup> -…+2(-7) <sup>n</sup> = (1-(-7) <sup>n+1</sup> )/4 whenever n is a nonnegative integer. Fill in the corresponding steps: (Assume)",

					"5.1.8d: Prove that 2-2*7+2*7 <sup>2</sup> -…+2(-7) <sup>n</sup> = (1-(-7) <sup>n+1</sup> )/4 whenever n is a nonnegative integer. Fill in the corresponding steps: (Induction)",


					"5.1.10a: Find the formula for 1/(1*2)+1/(2*3)+...+1/(n(n+1)) then fill  prove the formula by filling in the following steps: (Find the Formula) ",

					"5.1.10b: Find the formula for 1/(1*2)+1/(2*3)+...+1/(n(n+1)) then fill  prove the formula by filling in the following steps: (Basis) ",

					"5.1.10c: Find the formula for 1/(1*2)+1/(2*3)+...+1/(n(n+1)) then fill  prove the formula by filling in the following steps: (Want to Show) ",

					"5.1.10d: Find the formula for 1/(1*2)+1/(2*3)+...+1/(n(n+1)) then fill  prove the formula by filling in the following steps: (Assume)",

					"5.1.10e: Find the formula for 1/(1*2)+1/(2*3)+...+1/(n(n+1)) then fill  prove the formula by filling in the following steps: (Induction)",


					"5.1.12a: Prove that &Sigma; <sub>j=0</sub> <sup>n</sup> (-1/2) <sup>j</sup> = (2 <sup>n+1</sup> +(-1) <sup>n</sup> )/(3*2 <sup>n</sup> ) whenever n is a nonnegative integer by filling in the following steps: (Basis) ",

					"5.1.12b: Prove that &Sigma; <sub>j=0</sub> <sup>n</sup> (-1/2) <sup>j</sup> = (2 <sup>n+1</sup> +(-1) <sup>n</sup> )/(3*2 <sup>n</sup> ) whenever n is a nonnegative integer by filling in the following steps: (Want to Show) ",

					"5.1.12c: Prove that &Sigma; <sub>j=0</sub> <sup>n</sup> (-1/2) <sup>j</sup> = (2 <sup>n+1</sup> +(-1) <sup>n</sup> )/(3*2 <sup>n</sup> ) whenever n is a nonnegative integer by filling in the following steps: (Assume)",

					"5.1.12d: Prove that &Sigma; <sub>j=0</sub> <sup>n</sup> (-1/2) <sup>j</sup> = (2 <sup>n+1</sup> +(-1) <sup>n</sup> )/(3*2 <sup>n</sup> ) whenever n is a nonnegative integer by filling in the following steps: (Induction)",


					"5.1.14a: Prove that for every positive integer n. &Sigma; <sup>n</sup> <sub>(k=1)</sub> k2 <sup>k</sup> = (n-1) 2 <sup>n+1</sup> +2 by filling in the following steps: (Basis) ",

					"5.1.14b: Prove that for every positive integer n. &Sigma; <sup>n</sup> <sub>(k=1)</sub> k2 <sup>k</sup> = (n-1) 2 <sup>n+1</sup> +2 by filling in the following steps: (Want to Show) ",

					"5.1.14c: Prove that for every positive integer n. &Sigma; <sup>n</sup> <sub>(k=1)</sub> k2 <sup>k</sup> = (n-1) 2 <sup>n+1</sup> +2 by filling in the following steps: (Assume)",

					"5.1.14d: Prove that for every positive integer n. &Sigma; <sup>n</sup> <sub>(k=1)</sub> k2 <sup>k</sup> = (n-1) 2 <sup>n+1</sup> +2 by filling in the following steps: (Induction)",


					"5.1.16a: Prove that for every positive integer n, 1*2*3+2*3*4+…+n(n+1)(n+2) = (n(n+1)(n+2)(n+3))/4, by filling in the following steps: (Basis) ",

					"5.1.16b: Prove that for every positive integer n, 1*2*3+2*3*4+…+n(n+1)(n+2) = (n(n+1)(n+2)(n+3))/4, by filling in the following steps: (Want to Show) ",

					"5.1.16c: Prove that for every positive integer n, 1*2*3+2*3*4+…+n(n+1)(n+2) = (n(n+1)(n+2)(n+3))/4, by filling in the following steps: (Assume)",

					"5.1.16d: Prove that for every positive integer n, 1*2*3+2*3*4+…+n(n+1)(n+2) =(n(n+1)(n+2)(n+3))/4, by filling in the following steps: (Induction)",


					"5.1.20a: Prove that 3 <sup>n</sup> <n! if n is an integer greater then 6, n>6, by filling in the following steps: (Basis) ",

					"5.1.20b: Prove that 3 <sup>n</sup> <n! if n is an integer greater then 6, n>6, by filling in the following steps: (Want to Show) ",

					"5.1.20c: Prove that 3 <sup>n</sup> <n! if n is an integer greater then 6, n>6, by filling in the following steps: (Assume)",

					"5.1.20d: Prove that 3 <sup>n</sup> <n! if n is an integer greater then 6, n>6, by filling in the following steps: (Induction)",


					"5.1.32a: Prove that 3 divides n <sup>3</sup> +2n whenever n is a positive integer by filling in the following steps: (Basis) ",

					"5.1.32b: Prove that 3 divides n <sup>3</sup> +2n whenever n is a positive integer by filling in the following steps: (Want to Show) ",

					"5.1.32c: Prove that 3 divides n <sup>3</sup> +2n whenever n is a positive integer by filling in the following steps: (Assume)",

					"5.1.32d: Prove that 3 divides n <sup>3</sup> +2n whenever n is a positive integer by filling in the following steps: (Induction)",


					"5.1.36a: Prove that 21 divides 4 <sup>n+1</sup> +5 <sup>2n-1</sup> whenever n is a positive integer by filling in the following steps: (Basis) ",

					"5.1.36b: Prove that 21 divides 4 <sup>n+1</sup> +5 <sup>2n-1</sup> whenever n is a positive integer by filling in the following steps: (Want to Show) ",

					"5.1.36c: Prove that 21 divides 4 <sup>n+1</sup> +5 <sup>2n-1</sup> whenever n is a positive integer by filling in the following steps: (Assume)",

					"5.1.36d: Prove that 21 divides 4 <sup>n+1</sup> +5 <sup>2n-1</sup> whenever n is a positive integer by filling in the following steps: (Induction)",


				/*
					Assignment 7
				*/
				/*
					"2.5.2a: Determine whether each of these sets is finite, countably infinite, or uncountable. For those that are countably infinite, exhibit a one to one correspondence between the set of positive integers and that set. The integers greater than 10. ",
					"2.2.5b: Determine whether each of these sets is finite, countably infinite, or uncountable. For those that are countably infinite, exhibit a one to one correspondence between the set of positive integers and that set. the odd negative integers. ",
					"2.5.2c: Determine whether each of these sets is finite, countably infinite, or uncountable. For those that are countably infinite, exhibit a one to one correspondence between the set of positive integers and that set. the integers with absolute value less than 1,000,000. ",
					"2.5.4a: Determine whether each of these sets is countable. For those that are countably infinite, exhibit a one to one correspondence between a set of positive integers and that set. integers not divisible by 3. ",
					"2.5.4b: Determine whether each of these sets is countable. For those that are countably infinite, exhibit a one to one correspondence between a set of positive integers and that set. integers divisible by 5 but not by 7. ",
					"2.5.4c: Determine whether each of these sets is countable. For those that are countably infinite, exhibit a one to one correspondence between a set of positive integers and that set. the real numbers with decimal representation consisting of all 1’s. ",
					"2.5.6: Suppose the the Hilbert’s Grand hotel is fully occupied, but the hotel closes all the even numbered rooms for maintenance .show that all guests can remain in the hotel. ",
					"2.6.8: Show that a countably infinite number of guests arriving at the Hilbert’s fully occupied Grand Hotel can be given rooms without evicting current guests. ",
					"2.5.22: Suppose that A is a countable set. Show that the set B is also countable if there is an onto function f from A to B. ",
					"2.5.28: Show that the set Z\u207A X Z\u207A is countable. ",
					"2.6.2a: Find A+B. A={1,0,4;-1,2,2;0,-2,-3} B={-1,3,5;2,2,-3;2,-3,0} ",
					"2.6.2b: Find A+B A={-1,0,5,6;-4,-3,5,-2} B={-3,9,-3,4;0,-2,-1,2} ",
					"2.6.4a: Find the product AB A={1,0,1;0,-1,-1;-1,1,0}  B={0,1,-1;1,-1,0;-1,0,1} ",
					"2.6.4b: Find the product AB A={1,-3,0;1,2,2;2,1,-1} B={1,-1,2,3;-1,0,3,-1;-3,-2,0,2} ",
					"2.6.4c: Find the Product AB A={0-1;72;-4-3} B={4,-1,2,3,0;-2,0,3,4,1} ",
					"2.6.6: Find the A matrix such that {1,3,2;2,1,1;4,0,3}A={7,1,3;1,0,3;-1,-3,7} ",
					"2.6.10a: let A be a 3x4 matrix and B be a 4x5 matrix and C be a 4x4 matrix. Determine which of the following products are defined and find the size of those that are defined. AB ",
					"2.6.10b: let A be a 3x4 matrix and B be a 4x5 matrix and C be a 4x4 matrix. Determine which of the following products are defined and find the size of those that are defined. BA ",
					"2.6.10c: let A be a 3x4 matrix and B be a 4x5 matrix and C be a 4x4 matrix. Determine which of the following products are defined and find the size of those that are defined. AC ",
					"2.6.20a: Let A={-1,2;1,3}. Find A inverse.  ",
					"2.6.20b: Let A={-1,2;1,3}. Find A cubed.  ",
					"2.6.20c: Let A={-1,2;1,3} Find A inverse cubed (A inverse) cubed.  ",
					"2.6.26a: Let A={1,1;0,1} and B={0,1;1,0}. Find A \u2228 B. ",
					"2.6.26b: Let A={1,1;0,1} and B={0,1;1,0}. Find A \u2227 B. ",
					"2.6.26c: Let A={1,1;0,1} and B={0,1;1,0}. Find A \u2299 B. ",
					"3.1.4: Describe an algorithm that takes as input a lists of n integers and produces as output the largest difference obtained by subtracting an integer in the list from the one following it. ",
					"3.1.16: Describe an algorithm for finding the smallest integer in a finite sequence of natural order. ",
					"3.1.18: Describe an algorithm that locates the last occurrence of the smallest element in a finite list of integers, where the integers in the list are not necessarily distinct. ",
					"3.1.30: Devise an algorithm that finds modes. Recall that a list of integers is nondecreasing if  each term of the list is at least as large as the preceding term. ",
					"3.1.34: Use bubble sort to sort  6,2,3,1,5,4 showing the lists obtained at each step. ",
					"3.1.38: Use insertion sort to sort 6,2,3,1,5,4 showing the lists obtained at each step. ",
					"3.1: Use selection sort to sort 6,2,3,1,5,4 showing the lists obtained at each step. ",
					"3.1.52a: Use the greedy algorithm to make change using quarters, dimes, nickels, and pennies for: .87 ",
					"3.1.52b: Use the greedy algorithm to make change using quarters, dimes, nickels, and pennies for: .49 ",
					"3.1.52c: Use the greedy algorithm to make change using quarters, dimes, nickels, and pennies for: .99 ",
					"3.1.52d: Use the greedy algorithm to make change using quarters, dimes, nickels, and pennies for: .33 ",
					"3.2.2a: Determine whether  each of these  functions is O(x^2) 17x+11 ",
					"3.2.2b: Determine whether  each of these  functions is O(x^2) x^2+1000 ",
					"3.2.2c: Determine whether  each of these  functions is O(x^2) xlogx ",
					"3.2.2d: Determine whether  each of these  functions is O(x^2) x^4/2 ",
					"3.2.2e: Determine whether  each of these  functions is O(x^2) 2^x ",
					"3.2.2f: Determine whether  each of these  functions is O(x^2) Floor(x)*Ceiling(x) ",
					"3.2.8a: Find the  least integer n such that f(x) is O(x^n) for each of these functions: 2X^3 + X^2logX ",
					"3.2.8b: Find the  least integer n such that f(x) is O(x^n) for each of these functions: 23X^3 + (logX)^4 ",
					"3.2.8c: Find the  least integer n such that f(x) is O(x^n) for each of these functions: 2(x^4 + X^2+1)/(X^4 + 1) ",
					"3.2.8d: Find the  least integer n such that f(x) is O(x^n) for each of these functions: 2(X^3 + 5logX)/(X^4 + 1) ",
					"3.2.14a: Determine whether  X^3 is O(g(x)) for each of these functions:  X^2 ",
					"3.2.14b: Determine whether  X^3 is O(g(x)) for each of these functions:  X^3 ",
					"3.2.14c: Determine whether  X^3 is O(g(x)) for each of these functions:  X^2 + X^3 ",
					"3.2.14d: Determine whether  X^3 is O(g(x)) for each of these functions:  X^2 + X^4 ",
					"3.2.14e: Determine whether  X^3 is O(g(x)) for each of these functions:  3^X ",
					"3.2.14f: Determine whether  X^3 is O(g(x)) for each of these functions:  (X^3)/2 "
				*/

				/*
					Assignment 6
				*/
				/*"2.4.2a: What is the term a\u2088 of the sequence {a\u207F} if a\u207F = 2n-1?",
				"2.4.2b: What is the term a\u2088 of the sequence {a\u207F} if a\u207F = 7?",
				"2.4.2c: What is the term a\u2088 of the sequence {a\u207F} if a\u207F = 1+(-1)\u207F?",
				"2.4.2d: What is the term a\u2088 of the sequence {a\u207F} if a\u207F = -(-2)\u207F?",
				"2.4.4a: What are the terms a\u2080,a\u2081,a\u2082,and a\u2083 of the sequence {a\u207F} where a\u207F = (-2)\u207F?",
				"2.4.4b: What are the terms a\u2080,a\u2081,a\u2082,and a\u2083 of the sequence {a\u207F} where a\u207F = 3?",
				"2.4.4c: What are the terms a\u2080,a\u2081,a\u2082,and a\u2083 of the sequence {a\u207F} where a\u207F = 7+4\u207F?",
				"2.4.4d: What are the terms a\u2080,a\u2081,a\u2082,and a\u2083 of the sequence {a\u207F} where a\u207F = 2\u207F+(-2)\u207F?",
				"2.4.10a: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. a\u207F=-2, a\u2099\u208B\u2081a\u2080=-1",
				"2.4.10b: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. a\u2099\u208B\u2081-a\u2099\u208B\u2082, a\u2080=2, a\u2081=-1",
				"2.4.10c: Find the first six terms of the sequence defined by each of these recurrence relations and initial conditions. 3a\u2099\u208B\u2081\u00B2, a\u2080=1",
				"2.4.12a: Show that the sequence {a\u207F} is a solution of the recurrence relation a\u207F=-3a\u2099\u208B\u2081+4a\u2099\u208B\u2082 if a\u207F = 0",
				"2.4.12b: Show that the sequence {a\u207F} is a solution of the recurrence relation a\u207F=-3a\u2099\u208B\u2081+4a\u2099\u208B\u2082 if a\u207F = 1",
				"2.4.12c: Show that the sequence {a\u207F} is a solution of the recurrence relation a\u207F=-3a\u2099\u208B\u2081+4a\u2099\u208B\u2082 if a\u207F = (-4)\u207F",
				"2.4.12d: Show that the sequence {a\u207F} is a solution of the recurrence relation a\u207F=-3a\u2099\u208B\u2081+4a\u2099\u208B\u2082 if a\u207F = 2(-4)\u207F+3",
				"2.4.16a: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = -a\u2099\u208B\u2081, a\u2080=5",
				"2.4.16b: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = a\u2099\u208B\u2081+2, a\u2080=3",
				"2.4.16c: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = a\u2099\u208B\u2081-n, a\u2080=4",
				"2.4.16d: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = 2a\u2099\u208B\u2081-3, a\u2080=-1",
				"2.4.16e: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = (n+1)a\u2099\u208B\u2081, a\u2080=2",
				"2.4.16f: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F = 2na\u2099\u208B\u2081, a\u2080=3",
				"2.4.16g: Find the solution to each of these recurrence relations with the given initial conditions: a\u207F =-a\u2099\u208B\u2081+n-1, a\u2080=7",
				"2.4.26a: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 3,6,11,18,27,38,51,66,83,102,...",
				"2.4.26b: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.7,11,15,19,23,27,31,35,39,43,...",
				"2.4.26c: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,10,11,100,101,110,111,1000,1001,1010,1011,...",
				"2.4.26d: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,2,2,2,3,3,3,3,3,5,5,5,5,5,...",
				"2.4.26e: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.0,2,8,26,80,242,728,2186,6560,19682,...",
				"2.4.26f: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence. 1,3,15,105,945,10395,135135,2027025,34459425,...",
				"2.4.26g: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.1,0,0,1,1,1,0,0,0,0,1,1,1,1,1,...",
				"2.4.26h: For each of these lists of integers, provide a simple formula or rule that generates the terms of an integer sequence that begins with the given list. Assuming that your formula or  rule is correct, determine the next three terms of the sequence.2,4,16,256,65536,4294967296,...",
				"2.4.30a: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>j ",
				"2.4.30b: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>j\u00B2",
				"2.4.30c: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>1/j",
				"2.4.30d: What are the values of these sums, where S = {1,3,5,7}? \u2211<sub>j \u220A S</sub>1",
				"2.4.32a: Find the value of each of these sums \u2211\u2C7C\u208C\u2080 1+(-1)<sup>j</sup>",
				"2.4.32b: Find the value of each of these sums \u2211\u2C7C\u208C\u2080 3<sup>j</sup>-2<sup>j</sup>",
				"2.4.32c: Find the value of each of these sums \u2211\u2C7C\u208C\u2080 2*3<sup>j</sup>+3*2<sup>j</sup>",
				"2.4.32d: Find the value of each of these sums \u2211\u2C7C\u208C\u2080 2<sup>j+1</sup>-2<sup>j</sup>",
				"2.4.34a: Compute each of these double sums. \u2211<sub>i=1</sub><sup>3</sup>\u2211\u2C7C\u208C\u2081\u00B2 (i-j)",
				"2.4.34b: Compute each of these double sums. \u2211<sub>i=0</sub><sup>3</sup>\u2211\u2C7C\u208C\u2080\u00B2 (3i+2j)",
				"2.4.34c: Compute each of these double sums. \u2211<sub>i=1</sub><sup>3</sup>\u2211\u2C7C\u208C\u2080\u00B2 j",
				"2.4.34d: Compute each of these double sums. \u2211<sub>i=0</sub><sup>2/sup>\u2211\u2C7C\u208C\u2080<sup>3</sup>  i\u00B2j<sup>3</sup>"
				*/
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
		for(int k = 0; k < 41 /*52*//*42*//*22*//*19*//*39*/; k ++) {
			SrlBankProblem.Builder bankBuilder = SrlBankProblem.newBuilder();
			bankBuilder.setQuestionText(questionText[k]);
			SrlPermission.Builder permissions2 = SrlPermission.newBuilder();
			permissions2.addUserPermission(courseId);
			bankBuilder.setAccessPermission(permissions2.build());
			bankBuilder.setQuestionType(QuestionType.SKETCH);
			String resultantId = null;
			try {
				resultantId = MongoInstitution.getInstance().insertBankProblem(mastId, bankBuilder.buildPartial()); // "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332"
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
