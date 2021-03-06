validateFirstRun(document.currentScript);
(function() {
    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    var courseManagement = CourseSketch.courseManagement;
    courseManagement.gradebookMode = true;

    /**
     * This loads the assignments from the database then calls 'showCourses' to display them.
     *
     * Called when a user clicks on a course school item.
     *
     * @param {SrlCourse} course - The name of the course.
     */
    courseManagement.courseClicked = function(course) {
        CourseSketch.dataManager.addState('gradebookCourseid', course.id);
        if (CourseSketch.connection.isInstructor) {
            CourseSketch.redirectContent('/src/instructor/gradebook/instructorGradebook.html', 'Loading Gradebook');
        } else {
            CourseSketch.redirectContent('/src/student/gradebook/studentGradebook.html', 'Loading Grades');
        }
    };
})();
