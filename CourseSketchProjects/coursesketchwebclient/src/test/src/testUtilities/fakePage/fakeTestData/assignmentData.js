(function() {

    CourseSketch.fakeAssignments = [];

    var assignment1 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
    var assignment2 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
    var assignment3 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
    var assignment4 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
    var assignment5 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
    var assignment6 = CourseSketch.PROTOBUF_UTIL.SrlAssignment();

    assignment1.courseId = "1";
    assignment1.id = "1";
    assignment1.name = "Test Assignment";
    assignment1.description = "Test description not the best description though...";
    assignment1.state = CourseSketch.PROTOBUF_UTIL.State(true, true, false, true, true);
    CourseSketch.fakeAssignments.push(assignment1);

    assignment2.courseId = "1";
    assignment2.id = "2";
    assignment2.name = "Test HW1";
    assignment2.type = CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().AssignmentType.HOMEWORK;
    assignment2.other = "";
    assignment2.description = "This was a triumph test";
    assignment2.links = "http://en.wikipedia.org/wiki/Moose";
    assignment2.latePolicy = CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().LatePolicy.POLICY1;
    assignment2.gradeWeight = 15;
    assignment2.grade = 97;

    //assignment2.accessDate = CourseSketch.PROTOBUF_UTIL.DateTime(2015, 9, 01, 1200,0,0, new Date().getTime());
    //assignment2.dueDate = CourseSketch.PROTOBUF_UTIL.DateTime(2015, 11, 01, 1200, 0, 0, new Date().getTime());
    //assignment2.closeDate = CourseSketch.PROTOBUF_UTIL.DateTime(2015, 11, 02, 2300, 0, 0, new Date().getTime());
    //assignment2.state = CourseSketch.PROTOBUF_UTIL.State(true, false, false, false, false);
    assignment2.imageURL = "test/truss_thumb.png";
    CourseSketch.fakeAssignments.push(assignment2);

    assignment3.courseId = "2";
    assignment3.id = "3";
    assignment3.name = "Quiz 101";
    assignment3.description = "We will gracefully guide you through a quiz because we are nice";
    assignment3.type = CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().AssignmentType.QUIZ;
    //assignment3.state = CourseSketch.PROTOBUF_UTIL.State(true, true, false, true, false);
    CourseSketch.fakeAssignments.push(assignment3);

    assignment4.courseId = "3";
    assignment4.id = "4";
    assignment4.name = "Quantum entanglement";
    assignment4.description = "tests you over your tanglement";
    assignment4.type = CourseSketch.PROTOBUF_UTIL.getSrlAssignmentClass().AssignmentType.HOMEWORK;
    //assignment4.state = CourseSketch.PROTOBUF_UTIL.State(true, true, false, true, false);
    CourseSketch.fakeAssignments.push(assignment4);

    assignment5.courseId = "3";
    assignment5.id = "5";
    assignment5.name = "Quantum spin states";
    assignment5.description = "guess which state the cat is in";
    CourseSketch.fakeAssignments.push(assignment5);

    assignment6.courseId = "4";
    assignment6.id = "6";
    assignment6.name = "weave identification";
    assignment6.description = "do you know your weaves?";
    CourseSketch.fakeAssignments.push(assignment6);

    assignment7.courseId = "5";
    assignment7.id = "7";
    assignment7.name = "underwater breathing";
    assignment7.description = "how long can you hold your breath?";
})();
