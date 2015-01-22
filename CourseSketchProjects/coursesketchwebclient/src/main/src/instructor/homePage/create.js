(function(courseManagement) {
    /**
     * Function to be called when a lecture has finished editing.
     *
     * @param attributeChanged
     *            the name of the protobuf attribute that changed
     * @param oldValue
     *            the attribute's old value
     * @param newValue
     *            the attribute's new value
     * @param element
     *            protobuf element that has been edited
     */
    courseManagement.courseEndEdit = function(attributeChanged, oldValue, newValue, element) {
            element[attributeChanged] = newValue;
            CourseSketch.dataManager.updateCourse(element.schoolItemData);
    };

    courseManagement.commonShowCourses = courseManagement.showCourses;

    /**
     * Overwrote the old show courses to add some edit capabilities.
     */
    courseManagement.showCourses = function(courseList) {
        courseManagement.commonShowCourses(courseList);
        var children = document.getElementById('class_list_column').children;
        if (children.length <= 0) {
            hideButton("assignment_button")
        } else {
            showButton("assignment_button");
        }
        for (var i = 0; i < children.length; i++) {
            var schoolItem = children[i];
            if (schoolItem.nodeName == "SCHOOL-ITEM") {
                schoolItem.setEditCallback(courseManagement.courseEndEdit);
            }
        }
    };

    /**
     * Creates a new course with default values.
     * adds it to the database.
     */
    courseManagement.addNewCourse = function addNewCourse() {
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
                courseManagement.showCourses(localCourseList);
            }, function(course) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                courseManagement.showCourses(localCourseList);
            });
        });
    }


    /**
     * sets an element (should be a button) with the given id to be visible.
     */
    function showButton(id) {
        var element = document.getElementById(id);
        if (element) {
            element.style.display = "block";
        }
    }

     /**
     * sets an element (should be a button) with the given id to be invisible.
     */
    function hideButton(id) {
        var element = document.getElementById(id);
        if (element) {
            element.style.display = "none";
        }
    }
})(CourseSketch.courseManagement);
