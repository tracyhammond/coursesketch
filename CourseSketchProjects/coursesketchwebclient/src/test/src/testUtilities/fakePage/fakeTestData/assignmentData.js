(function() {

    CourseSketch.fakeAssignments = [];

    var assignment1 = CourseSketch.prutil.SrlAssignment();
    var assignment2 = CourseSketch.prutil.SrlAssignment();
    var assignment3 = CourseSketch.prutil.SrlAssignment();
    var assignment4 = CourseSketch.prutil.SrlAssignment();
    var assignment5 = CourseSketch.prutil.SrlAssignment();
    var assignment6 = CourseSketch.prutil.SrlAssignment();
    var assignment7 = CourseSketch.prutil.SrlAssignment();
    var assignment8 = CourseSketch.prutil.SrlAssignment();
    var assignment9 = CourseSketch.prutil.SrlAssignment();
    var assignment10 = CourseSketch.prutil.SrlAssignment();

    assignment1.courseId = "1";
    assignment1.id = "1";
    assignment1.name = "Test Assignment";
    assignment1.description = "Test description not the best description though...";
    assignment1.state = CourseSketch.prutil.State();
    assignment1.state.published = true;
    assignment1.state.accessible = true;// late
    assignment1.state.pastDue = true;
    assignment1.state.started = false;
    assignment1.state.completed = false;
    assignment1.state.graded = true;
    assignment1.problemList = ["1", "2"];
    CourseSketch.fakeAssignments.push(assignment1);

    assignment2.courseId = "1";
    assignment2.id = "2";
    assignment2.name = "Test HW1";
    assignment2.type = CourseSketch.prutil.getSrlAssignmentClass().AssignmentType.HOMEWORK;
    assignment2.other = "";
    assignment2.description = "This was a triumph test";
    assignment2.links = "http://en.wikipedia.org/wiki/Moose";
    //assignment2.latePolicy = CourseSketch.prutil.getSrlAssignmentClass().LatePolicy.POLICY1;
    assignment2.gradeWeight = "15";
    assignment2.grade = 97;
    assignment2.state = CourseSketch.prutil.State();
    assignment2.state.published = true;
    assignment2.state.accessible = true;//started
    assignment2.state.pastDue = false;
    assignment2.state.started = true;
    assignment2.state.completed = false;
    assignment2.state.graded = false;
    assignment2.problemList = ["3", "4"];

    //assignment2.accessDate = CourseSketch.prutil.DateTime(2015, 9, 01, 1200,0,0, new Date().getTime());
    //assignment2.dueDate = CourseSketch.prutil.DateTime(2015, 11, 01, 1200, 0, 0, new Date().getTime());
    //assignment2.closeDate = CourseSketch.prutil.DateTime(2015, 11, 02, 2300, 0, 0, new Date().getTime());
    //assignment2.state = CourseSketch.prutil.State(true, false, false, false, false);
    assignment2.imageURL = "test/truss_thumb.png";
    CourseSketch.fakeAssignments.push(assignment2);

    assignment3.courseId = "2";
    assignment3.id = "3";
    assignment3.name = "Quiz 101";
    assignment3.description = "We will gracefully guide you through a quiz because we are nice";
    assignment3.type = CourseSketch.prutil.getSrlAssignmentClass().AssignmentType.QUIZ;
    assignment3.state = CourseSketch.prutil.State();
    assignment3.state.published = true;
    assignment3.state.accessible = false;
    assignment3.state.pastDue = false;
    assignment3.state.started = false;
    assignment3.state.completed = false;
    assignment3.state.graded = false;
    assignment3.problemList = ["5", "6"];
    CourseSketch.fakeAssignments.push(assignment3);

    assignment4.courseId = "3";
    assignment4.id = "4";
    assignment4.name = "Quantum entanglement";
    assignment4.description = "tests you over your tanglement";
    assignment4.type = CourseSketch.prutil.getSrlAssignmentClass().AssignmentType.HOMEWORK;
    assignment4.state = CourseSketch.prutil.State();
    assignment4.state.published = true; //closed
    assignment4.state.accessible = false;
    assignment4.state.pastDue = true;
    assignment4.state.started = false;
    assignment4.state.completed = false;
    assignment4.state.graded = false;
    assignment4.problemList = ["7"];
    CourseSketch.fakeAssignments.push(assignment4);

    assignment5.courseId = "3";
    assignment5.id = "5";
    assignment5.name = "Quantum spin states";
    assignment5.description = "guess which state the cat is in";
    assignment5.state = CourseSketch.prutil.State();
    assignment5.state.published = true; //completed
    assignment5.state.accessible = true;
    assignment5.state.pastDue = false;
    assignment5.state.started = true;
    assignment5.state.completed = true;
    assignment5.state.graded = false;
    assignment5.problemList = ["8"];
    CourseSketch.fakeAssignments.push(assignment5);

    assignment6.courseId = "4";
    assignment6.id = "6";
    assignment6.name = "weave identification";
    assignment6.description = "do you know your weaves?";
    assignment6.state = CourseSketch.prutil.State();
    assignment6.state.published = false; //not published
    assignment6.state.accessible = false;
    assignment6.state.pastDue = false;
    assignment6.state.started = false;
    assignment6.state.completed = false;
    assignment6.state.graded = false;
    assignment6.problemList = ["9"];
    CourseSketch.fakeAssignments.push(assignment6);

    assignment7.courseId = "5";
    assignment7.id = "7";
    assignment7.name = "underwater breathing";
    assignment7.description = "how long can you hold your breath?";
    assignment7.problemList = ["10"];
    CourseSketch.fakeAssignments.push(assignment7);

    assignment8.courseId = "6";
    assignment8.id = "8";
    assignment8.name = "style quiz";
    assignment8.description = "do you know how to code??";
    assignment8.problemList = ["11", "12"];
    CourseSketch.fakeAssignments.push(assignment8);

    assignment9.courseId = "6";
    assignment9.id = "9";
    assignment9.name = "make course sketch";
    assignment9.description = "not a big assignment, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    assignment9.problemList = ["13", "14"];
    CourseSketch.fakeAssignments.push(assignment9);

    assignment10.courseId = "5";
    assignment10.id = "10";
    assignment10.name = "random problem loading";
    assignment10.type = CourseSketch.prutil.getSrlAssignmentClass().AssignmentType.GAME;
    assignment10.problemList = ["1", "2", "3", "4", "5", "6", "7", "8", "14"];
    CourseSketch.fakeAssignments.push(assignment10);

	for (var i = 0; i < 10; i++) {
	    var assignmentLoop = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
		assignmentLoop.courseId = '8';
		assignmentLoop.id = '' + (11 + i);
		assignmentLoop.name = 'homework' + i;
		assignmentLoop.description = 'This is a homework that contains 10 problems!';
		assignmentLoop.problemList = [];
		for (var j = 0; j < 10; j++) {
			assignmentLoop.problemList.push('' + (11 + i * 10 + j));
		}
		CourseSketch.fakeAssignments.push(assignmentLoop);
	}
})();
