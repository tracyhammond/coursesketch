//Test Courses
var user_problems = {};
var user_assignments = {};
var user_courses = {};
try {
if(!SrlCourse) {
	parent.copyParentUtilityFunctions(this);
	parent.copyParentProtos(this);
}
var course1 = new SrlCourse("Course_01");
	course1.name = "Physics";
	course1.description = "Physics is Phun";
	
var course2 = new SrlCourse("Course_02");
	course2.name = "Chemistry";
	course2.description = "Chemistry makes things explode";

var course3 = new SrlCourse("Course_03");
	//empty course

//Test Assignments
var assignment1 = new SrlAssignment("Course_01", "Assignment_001");
	assignment1.name = "Truss HW1";
	assignment1.type = SrlAssignment.AssignmentType.HOMEWORK;
	assignment1.other = "";
	assignment1.description = "This was a triumph";
	assignment1.links = "http://en.wikipedia.org/wiki/Moose";
	assignment1.latePolicy = SrlAssignment.LatePolicy.POLICY1;
	assignment1.gradeWeight = 15;
	assignment1.grade = 97;
	
	assignment1.accessDate = new SchoolBuilder.DateTime(2015, 9, 01, 1200);
	assignment1.dueDate = new SchoolBuilder.DateTime(2015, 11, 01, 1200);
	assignment1.closeDate = new SchoolBuilder.DateTime(2015, 11, 02, 2300);
	assignment1.state = new SchoolBuilder.State(true, false, false, false, false);
	assignment1.imageURL = "test/truss_thumb.png";
	
var assignment2 = new SrlAssignment("Course_01", "Assignment_002");
	assignment2.name = "Truss HW2";
	assignment2.description = "I'm making a note here, HUGE SUCCESS";
	assignment2.state = new SchoolBuilder.State(true, true, false, true, true);
	
var assignment3 = new SrlAssignment("Course_01", "Assignment_003");
	assignment3.name = "Truss Quiz 1";
	assignment3.description = "We do what we must, because we can";
	assignment3.type = SrlAssignment.AssignmentType.QUIZ;
	assignment3.state = new SchoolBuilder.State(true, true, false, true, false);
	
var assignment4 = new SrlAssignment("Course_02", "Assignment_004");
	assignment4.name = "Molecular Diagram 1";
	assignment4.description = "Carbon atom drawings are a form of art, right?";
	assignment4.state = new SchoolBuilder.State(true, false, true, true, false);
	
var assignment5 = new SrlAssignment("Course_02", "Assignment_005");
	assignment5.name = "Chem Exam 1";
	assignment5.description = "This is only a test";
	
var assignment6 = new SrlAssignment("Course_02", "Assignment_006");
	//empty assignment

//Test Problems
var problem1 = new SrlProblem("Course_01", "Assignment_001", "Problem_001");
	problem1.name = "Truss Problem 1";
	problem1.description = "Description of Truss Problem 1"
		
var problem2 =  new SrlProblem("Course_01", "Assignment_001", "Problem_002", "Truss Problem 2", "Description of Truss Problem 2");
	//a different way to declare a message's first n items
	
var problem3 =  new SrlProblem("Course_01", "Assignment_002", "Problem_003", "Advanced Truss Problem 1", "Descirption of Advanced Truss Problem 1");

var problem4 = new SrlProblem("Course_01", "Assignment_001", "Problem_004");
	//empty problem
	
/* ===============================================================================================*/


	user_assignments[assignment1.id] = assignment1;
	user_assignments[assignment2.id] = assignment2;
	user_assignments[assignment3.id] = assignment3;
	user_assignments[assignment4.id] = assignment4;
	user_assignments[assignment5.id] = assignment5;
	user_assignments[assignment6.id] = assignment6;

/*
var upcomming_assignments = 
	[
	 	[
	 	 	'assignment',
	 	 	['Truss Assignment1','Truss_Link1','Assignment_Truss_1_ID'],
	 	 	'In this assignment we will draw a truss trussty truss.' +
	 	 	' This truss trussty truss will allow us to save sience.',
	 	 	'test/truss_thumb.png',
	 	 	['Phyics','PhysicsLink','Class_Physics_ID'],
	 	 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'completed'
	 	],
		[
		 	'assignment',
		 	['Truss Assignment2','Truss_Link2', 'Assignment_Truss_2_ID'],
		 	'In this second assignment we will make something cool. YAYYYYYY',
		 	'test/truss_thumb.png',
		 	['Phyics','PhysicsLink','Class_Physics_ID'],
		 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)'),
		 	'unaccessible'
		],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment1','Chemistry_Link1', 'Assignment_Chemistry_1_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'started'
 	 	],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment2','Chemistry_Link2', 'Assignment_Chemistry_2_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'closed'
	 	],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment3','Chemistry_Link3', 'Assignment_Chemistry_3_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'accessible'
	 	],
	 	[
	 	 	'assignment',
	 	 	['Class Assignment1','Class_Link3', 'Assignment_Class_1_ID'],
	 	 	'n this assignment we will do a course item.',
	 	 	'test/course_thumb.png',
	 	 	['Class 3','Class3Link','Class_Class_3_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'unaccessible'
 	 	],
	];
	*/


	user_courses[course1.id] = course1;
	user_courses[course2.id] = course2;
	user_courses[course3.id] = course3;

/*
var user_courses =
	[
	 	[
	 	 	'course',
		 	['Physics','PhysicsLink','Class_Physics_ID'],
		 	'Description for me course blah blah blabity blah',
	 	 	'test/truss_thumb.png'
	 	],
	 	[
	 	 	'course',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	'Description for the chemistry course!',
	 	 	'test/chemistry_thumb.png'
	 	],
	 	[
	 	 	'course',
	 	 	['Class 3','Class3Link','Class_Class_3_ID'],
	 	 	'Description for the 3rd course!',
	 	 	'test/no_image.png'
	 	 ],
	];
*/

	user_problems[problem1.id] = problem1;
	user_problems[problem2.id] = problem2;
	user_problems[problem3.id] = problem3;
	user_problems[problem4.id] = problem4;

/*
var questions =
	[
	 	[
	 	 	'problem',
	 	 	['TrussProblem1','TrussProblem1Link','Problem_Truss_1_ID'],
	 	 	'Question Text for problem one is blahbity blah blah',
	 	 	'test/truss_thumb.png'
	 	 ]
	];
*/
}catch(excetion) {
	
}
/* ===============================================================================================*/

var courseAssignments = new listMap();	
var assignmentProblems = new listMap();	
//initialize();
/**
 * This map will hold a map of courses to a list of assignments.
 */

function listMap() {
	this.map = {};
	this.addObject = function(key,object) {
		if(this.map[key]){
			this.map[key].push(object);
		} else {
			var array = [];
			array.push(object);
			this.map[key] = array;
		}
	}

	this.getList = function(key) {
		return this.map[key];
	}

}

// TODO change the keys to actually be unique

function initialize() {
//	courseAssignments.addObject('Class_Physics_ID',upcomming_assignments[0]);
//	courseAssignments.addObject('Class_Physics_ID',upcomming_assignments[1]);
//	courseAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[2]);
//	courseAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[3]);
//	courseAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[4]);
//	courseAssignments.addObject('Class_Class_3_ID',upcomming_assignments[5]);
//	assignmentProblems.addObject('Assignment_Truss_2_ID',questions[0]);
	
	
	 for (var i in user_assignments)
		{ courseAssignments.addObject(user_assignments[i].courseId, user_assignments[i]);}
	 
	 for (var i in user_problems)
		 { assignmentProblems.addObject(user_problems[i].assignmentId, user_problems[i]);}
}