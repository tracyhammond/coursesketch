(function(localScope) {
    /*
     * Creates a new course.
     * adds it to the database.
     *
     */
    localScope.addNewCourse = function addNewCourse() {
        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        document.getElementById('class_list_column').appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();
        // by instructors
        var course = CourseSketch.PROTOBUF_UTIL.SrlCourse();
        // course.id = "Course_01";
        course.name = "Insert name";
        course.description = "Insert description";
        // course.semester = "Should be in format: '_F13' (_F = Fall, Sp =
        // Spring, Su = Summer) ";
        // course.accessDate = "mm/dd/yyyy";
        // course.closeDate = "mm/dd/yyyy";
        CourseSketch.dataManager.getAllCourses(function(courseList) {
            var localCourseList = courseList;
            if (courseList instanceof CourseSketch.DatabaseException) {
                // we are cool because we are adding a new one.
                localCourseList = [];
            }
            var firstCourse = undefined;
            CourseSketch.dataManager.insertCourse(course, function(course) {
                firstCourse = course;
                localCourseList.push(course);
                localScope.showCourses(localCourseList);
            }, function(course) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                localScope.showCourses(localCourseList);
            });
        });
    }
})(CourseSketch.courseManagement);
