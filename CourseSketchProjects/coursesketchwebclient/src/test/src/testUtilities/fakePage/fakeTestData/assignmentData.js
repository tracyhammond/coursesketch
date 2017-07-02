validateFirstRun(document.currentScript);
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

    assignment1.courseId = '1';
    assignment1.id = '0';
    assignment1.name = 'Test Assignment';
    assignment1.description = 'Test description not the best description though...';
    assignment1.state = CourseSketch.prutil.State();
    assignment1.state.published = true;
    assignment1.state.accessible = true;// late
    assignment1.state.pastDue = true;
    assignment1.state.started = false;
    assignment1.state.completed = false;
    assignment1.state.graded = true;
    assignment1.problemGroups = ['10', '20'];
    CourseSketch.fakeAssignments.push(assignment1);

    assignment2.courseId = '1';
    assignment2.id = '100';
    assignment2.name = 'Test HW1';
    // Graded is used by some tests so do not change this value!
    assignment2.assignmentType = CourseSketch.prutil.AssignmentType.GRADED;
    assignment2.other = '';
    assignment2.description = 'This was a triumph test';
    assignment2.links = 'http://en.wikipedia.org/wiki/Moose';
    //assignment2.latePolicy = CourseSketch.prutil.getSrlAssignmentClass().LatePolicy.POLICY1;
    assignment2.gradeWeight = '15';
    assignment2.grade = 97;
    assignment2.state = CourseSketch.prutil.State();
    assignment2.state.published = true;
    assignment2.state.accessible = true;//started
    assignment2.state.pastDue = false;
    assignment2.state.started = true;
    assignment2.state.completed = false;
    assignment2.state.graded = false;
    assignment2.problemGroups = ['30', '40'];

    //assignment2.accessDate = CourseSketch.prutil.DateTime(2015, 9, 01, 1200,0,0, new Date().getTime());
    //assignment2.dueDate = CourseSketch.prutil.DateTime(2015, 11, 01, 1200, 0, 0, new Date().getTime());
    //assignment2.closeDate = CourseSketch.prutil.DateTime(2015, 11, 02, 2300, 0, 0, new Date().getTime());
    //assignment2.state = CourseSketch.prutil.State(true, false, false, false, false);
    assignment2.imageURL = 'test/truss_thumb.png';
    CourseSketch.fakeAssignments.push(assignment2);

    assignment3.courseId = '2';
    assignment3.id = '200';
    assignment3.name = 'Quiz 101';
    assignment3.description = 'We will gracefully guide you through a quiz because we are nice';
    assignment3.assignmentType = CourseSketch.prutil.AssignmentType.PRACTICE;
    assignment3.state = CourseSketch.prutil.State();
    assignment3.state.published = true;
    assignment3.state.accessible = false;
    assignment3.state.pastDue = false;
    assignment3.state.started = false;
    assignment3.state.completed = false;
    assignment3.state.graded = false;
    assignment3.problemGroups = ['50', '60'];
    CourseSketch.fakeAssignments.push(assignment3);

    assignment4.courseId = '3';
    assignment4.id = '300';
    assignment4.name = 'Quantum entanglement';
    assignment4.description = 'tests you over your tanglement';
    assignment4.assignmentType = CourseSketch.prutil.AssignmentType.GRADED;
    assignment4.state = CourseSketch.prutil.State();
    assignment4.state.published = true; //closed
    assignment4.state.accessible = false;
    assignment4.state.pastDue = true;
    assignment4.state.started = false;
    assignment4.state.completed = false;
    assignment4.state.graded = false;
    assignment4.problemGroups = ['70'];
    CourseSketch.fakeAssignments.push(assignment4);

    assignment5.courseId = '3';
    assignment5.id = '400';
    assignment5.name = 'Quantum spin states';
    assignment5.description = 'guess which state the cat is in';
    assignment5.state = CourseSketch.prutil.State();
    assignment5.state.published = true; //completed
    assignment5.state.accessible = true;
    assignment5.state.pastDue = false;
    assignment5.state.started = true;
    assignment5.state.completed = true;
    assignment5.state.graded = false;
    assignment5.problemGroups = ['80'];
    CourseSketch.fakeAssignments.push(assignment5);

    assignment6.courseId = '4';
    assignment6.id = '500';
    assignment6.name = 'weave identification';
    assignment6.description = 'do you know your weaves?';
    assignment6.state = CourseSketch.prutil.State();
    assignment6.state.published = false; //not published
    assignment6.state.accessible = false;
    assignment6.state.pastDue = false;
    assignment6.state.started = false;
    assignment6.state.completed = false;
    assignment6.state.graded = false;
    assignment6.problemGroups = ['90'];
    CourseSketch.fakeAssignments.push(assignment6);

    assignment7.courseId = '5';
    assignment7.id = '600';
    assignment7.name = 'underwater breathing';
    assignment7.description = 'how long can you hold your breath?';
    assignment7.problemGroups = ['100'];
    CourseSketch.fakeAssignments.push(assignment7);

    assignment8.courseId = '6';
    assignment8.id = '700';
    assignment8.name = 'style quiz';
    assignment8.description = 'do you know how to code??';
    assignment8.problemGroups = ['110', '120'];
    CourseSketch.fakeAssignments.push(assignment8);

    assignment9.courseId = '6';
    assignment9.id = '800';
    assignment9.name = 'make course sketch';
    assignment9.description = 'not a big assignment, I just want you to program a MOOC using sketch recognition. You have one week.';
    assignment9.problemGroups = ['130', '140'];
    assignment9.navigationType = CourseSketch.prutil.NavigationType.LOOPING;
    CourseSketch.fakeAssignments.push(assignment9);

    assignment10.courseId = '5';
    assignment10.id = '900';
    assignment10.name = 'random problem loading';
    assignment10.assignmentType = CourseSketch.prutil.AssignmentType.FLASHCARD;
    assignment10.navigationType = CourseSketch.prutil.NavigationType.RANDOM;
    assignment10.problemGroups = ['10', '20', '30', '40', '50', '60', '70', '80', '140'];
    CourseSketch.fakeAssignments.push(assignment10);

    var baseAssignmentId = 10000; // 10,000
    for (var i = 0; i < 4; i++) {
        var assignmentLoop = CourseSketch.prutil.SrlAssignment();
        assignmentLoop.courseId = '8';
        var assignmentIdNumber = baseAssignmentId + i * 100;
        assignmentLoop.id = '' + assignmentIdNumber;
        assignmentLoop.name = 'homework' + i;
        assignmentLoop.description = 'This is a homework that contains 10 problems!';
        assignmentLoop.problemGroups = [];
        for (var j = 0; j < 4; j++) {
            assignmentLoop.problemGroups.push('' + (assignmentIdNumber + j * 10));
        }
        CourseSketch.fakeAssignments.push(assignmentLoop);
    }
})();
