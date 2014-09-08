//Test Courses
var user_problems = {};
var user_assignments = {};
var user_courses = {};
var user_students = {};
addLoadEvent( function() {
	try{
	if (!SrlUser) {
		alert(SrlUser);
		try {
			parent.copyParentUtilityFunctions(this);
			parent.copyParentProtos(this);
		} catch (exception) {
			SrlUser = function() {
			};
			SrlCourse = function() {
			};
			SrlAssignment = function() {
			};
			SrlProblem = function() {
			};
		}
	}
	
	var course1 = new SrlCourse();
		course1.id = "Course_01";
		course1.name = "Physics";
		course1.description = "Physics is Phun";
		
	var course2 = new SrlCourse();
		course2.id = "Course_02";
		course2.name = "Chemistry";
		course2.description = "The course provides the mathematical foundations from discrete mathematics for analyzing computer algorithms," +
			" for both correctness and performance; introduction to models of computation, including finite state machines and Turing machines." +
			" At the end of the course, students will understand the basic principles of logic, proofs and sets." +
			" They will be able to apply results from discrete mathematics to analysis of algorithms." +
			" They will be able to produce proofs by induction and apply counting techniques." +
			" They will have a basic understanding of models of computation.";
	
	var course3 = new SrlCourse();
		course3.id = "Course_03";
		//empty course
	
	//Test Assignments
	var assignment1 = new SrlAssignment();
		assignment1.courseId = "Course_01";
		assignment1.id = "Assignment_001";
		assignment1.name = "Truss HW1";
		assignment1.type = SrlAssignment.AssignmentType.HOMEWORK;
		assignment1.other = "";
		assignment1.description = "This was a triumph";
		assignment1.links = "http://en.wikipedia.org/wiki/Moose";
		assignment1.latePolicy = SrlAssignment.LatePolicy.POLICY1;
		assignment1.gradeWeight = 15;
		assignment1.grade = 97;

		assignment1.accessDate = new SchoolBuilder.DateTime(2015, 9, 01, 1200,0,0, new Date().getTime());
		assignment1.dueDate = new SchoolBuilder.DateTime(2015, 11, 01, 1200, 0, 0, new Date().getTime());
		assignment1.closeDate = new SchoolBuilder.DateTime(2015, 11, 02, 2300, 0, 0, new Date().getTime());
		assignment1.state = new SchoolBuilder.State(true, false, false, false, false);
		assignment1.imageURL = "test/truss_thumb.png";

	var assignment2 = new SrlAssignment();
		assignment2.courseId = "Course_01";
		assignment2.id = "Assignment_002";
		assignment2.name = "Truss HW2";
		assignment2.description = "I'm making a note here, HUGE SUCCESS";
		assignment2.state = new SchoolBuilder.State(true, true, false, true, true);
		assignment2.grade = 38;
		
	var assignment3 = new SrlAssignment("Course_01", "Assignment_003");
		assignment3.name = "Truss Quiz 1";
		assignment3.description = "We do what we must, because we can";
		assignment3.type = SrlAssignment.AssignmentType.QUIZ;
		assignment3.state = new SchoolBuilder.State(true, true, false, true, false);
		assignment3.grade = 1010;
		
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
		
	var student1 = new SrlUser();
		student1.username = "Student_01";
		student1.courseList = [course1.id, course2.id];
		student1.firstName = "Hi";
		student1.lastName = "Hello";
		student1.password = "abcde";
	
	var student2 = new SrlUser();
		student2.username = "Student_02";
		student2.courseList = [course3.id];
		student2.password = "fghij";
		
	/* ===============================================================================================*/
	
	
		user_assignments[assignment1.id] = assignment1;
		user_assignments[assignment2.id] = assignment2;
		user_assignments[assignment3.id] = assignment3;
		user_assignments[assignment4.id] = assignment4;
		user_assignments[assignment5.id] = assignment5;
		user_assignments[assignment6.id] = assignment6;
	
	
		user_courses[course1.id] = course1;
		user_courses[course2.id] = course2;
		user_courses[course3.id] = course3;
	
		user_problems[problem1.id] = problem1;
		user_problems[problem2.id] = problem2;
		user_problems[problem3.id] = problem3;
		user_problems[problem4.id] = problem4;
	
		user_students[student1.username] = student1;
		user_students[student2.username] = student2;
	}catch(exception) {
		console.log(exception);
		console.error(exception.stack);
		alert(exception);
	}
});
/* ===============================================================================================*/

var studentCourses = new listMap();
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
	};

	this.getList = function(key) {
		return this.map[key];
	};

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
	
	 for (var i in user_courses)
	    { studentCourses.addObject(user_courses[i].courseId, user_courses[i]);}
	
	 for (var i in user_assignments)
		{ courseAssignments.addObject(user_assignments[i].courseId, user_assignments[i]);}
	 
	 for (var i in user_problems)
		 { assignmentProblems.addObject(user_problems[i].assignmentId, user_problems[i]);}
};