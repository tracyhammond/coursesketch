/**
 * Created by david on 7/18/17.
 */
(function() {
    var studentList = ["student1", "stud2", "gilbert", "gabby", "student5", "student6",
        "matt", "stud8", "stud9", "stud10"];

    var idToNameMap = new Map();
    for (var index = 0; index < studentList.length; index++) {
        idToNameMap.set(studentList[index], studentList[index].toUpperCase());
    }

    var studentRoster = idToNameMap;
    studentRoster.set('noGradeStudent', 'NOGRADESTUDENT');
    studentRoster.set('noGrade2', 'NOGRADE2');
    studentRoster.set('failure', 'FAILURE');

    CourseSketch.fakeRoster = studentRoster;
    var courseIndex = 7;
    var courseId = CourseSketch.fakeCourses[courseIndex].id;

    var fakeGradeList = [];
    var grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[0];
    grade.assignmentId = CourseSketch.fakeAssignments[0].id;
    grade.currentGrade = 70;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[1];
    grade.assignmentId = CourseSketch.fakeAssignments[0].id;
    grade.currentGrade = 99;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[3];
    grade.assignmentId = CourseSketch.fakeAssignments[0].id;
    grade.currentGrade = 105;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[0];
    grade.assignmentId = CourseSketch.fakeAssignments[1].id;
    grade.currentGrade = 80;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[1];
    grade.assignmentId = CourseSketch.fakeAssignments[1].id;
    grade.currentGrade = 50;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[2];
    grade.assignmentId = CourseSketch.fakeAssignments[1].id;
    grade.currentGrade = 98;
    fakeGradeList.push(grade);

    grade = CourseSketch.prutil.ProtoGrade();
    grade.courseId = courseId;
    grade.userId = studentList[1];
    grade.assignmentId = CourseSketch.fakeAssignments[2].id;
    grade.currentGrade = 95;
    fakeGradeList.push(grade);

    var assignmentList = CourseSketch.fakeCourses[courseIndex].assignmentList;
    for (var i = 4; i < studentList.length; i++) {
        for (var j = 3; j < assignmentList.length; j++) {
            grade = CourseSketch.prutil.ProtoGrade();
            grade.courseId = courseId;
            grade.userId = studentList[i];
            grade.assignmentId = assignmentList[j];
            grade.currentGrade = Math.random() * 100;
            grade.gradeHistory = [];
            var history = Math.random() * 5;
            for (var historyIndex = 0; historyIndex < history; historyIndex++) {
                var gradeHistory = CourseSketch.prutil.GradeHistory();
                gradeHistory.gradeValue = Math.random() * 100;
                gradeHistory.comment = 'gradeComment' + historyIndex + 'for student ' + grade.userId;
                gradeHistory.whoChanged = studentList[(j * historyIndex) % studentList.length];
                grade.gradeHistory.push(gradeHistory);
            }
            fakeGradeList.push(grade);
        }
    }

    CourseSketch.fakeGradeList = fakeGradeList;
})();