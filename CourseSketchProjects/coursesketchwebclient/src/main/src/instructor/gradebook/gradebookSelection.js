validateFirstRun(document.currentScript);
(function() {
    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    var courseManagement = CourseSketch.courseManagement;
    courseManagement.gradebookMode = true;
	
	/**
     * Called when a user clicks on a course school item.
     * This loads the assignments from the database then calls 'showCourses' to display them.
     */
	courseManagement.courseClicked = function(course) {
	 		CourseSketch.dataManager.addState('gradebookCourseid', course.id);
		// change source to the problem page! and load problem
		if (CourseSketch.connection.isInstructor) {
			// solution editor page!
			CourseSketch.redirectContent('/src/instructor/review/multiviewGrading.html', 'Loading Gradebook');
		} else {
			CourseSketch.redirectContent('/src/student/experiment/experiment.html', 'Loading Grades');
		}  
	};	
})();
    