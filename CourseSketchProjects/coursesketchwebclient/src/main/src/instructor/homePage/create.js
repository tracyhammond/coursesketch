(function(localScope) {
    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    /*
     * Creates a new course.
     */
    localScope.addNewCourse = function addNewCourse() { // Functionality to
                                                        // allow for adding of
                                                        // courses
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
            var firstCourse = undefined;
            CourseSketch.dataManager.insertCourse(course, function(course) {
                firstCourse = course;
                courseList.push(course);
                localScope.showCourses(courseList);
            }, function(course) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                localScope.showCourses(courseList);
            });
        });
    }
})(CourseSketch.courseManagement);
