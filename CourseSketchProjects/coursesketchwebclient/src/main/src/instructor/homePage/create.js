validateFirstRun(document.currentScript);

(function() {
    var courseManagement = CourseSketch.courseManagement;
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
        var keyList = newValue.keys();
        var srlCourse = element.schoolItemData;
        console.log(srlCourse);
        for (var key of keyList) {
            console.log(key);
            srlCourse[key] = newValue.get(key);
        }
        console.log(srlCourse);
        CourseSketch.dataManager.updateCourse(srlCourse);
    };

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
    courseManagement.assignmentEndEdit = function(attributeChanged, oldValue, newValue, element) {
        var keyList = newValue.keys();
        var srlCourse = element.schoolItemData;
        console.log(srlCourse);
        for (var key of keyList) {
            console.log(key);
            srlCourse[key] = newValue.get(key);
        }
        console.log(srlCourse);
        CourseSketch.dataManager.updateAssignment(srlCourse);
    };

    courseManagement.commonShowCourses = courseManagement.showCourses;

    /**
     * Overwrote the old show courses to add some edit capabilities.
     */
    courseManagement.showCourses = function(courseList) {
        courseManagement.commonShowCourses(courseList);
        hideButton("assignment_button");
        hideButton("problem_button");
        var children = document.getElementById('class_list_column').querySelectorAll("school-item");
        for (var i = 0; i < children.length; i++) {
            var schoolItem = children[i];
            schoolItem.setEditCallback(courseManagement.courseEndEdit);
        }
    };

    courseManagement.commonShowAssignments = courseManagement.showAssignments;

    /**
     * Overwrote the old show courses to add some edit capabilities.
     */
    courseManagement.showAssignments = function(assignmentList) {
        showButton("assignment_button");
        hideButton("problem_button");
        courseManagement.commonShowAssignments(assignmentList);
        var children = document.getElementById('assignment_list_column').querySelectorAll("school-item");
        for (var i = 0; i < children.length; i++) {
            var schoolItem = children[i];
            schoolItem.setEditCallback(courseManagement.assignmentEndEdit);
        }
    };

    /**
     * Creates a new course with default values.
     * adds it to the database.
     */
    courseManagement.addNewCourse = function addNewCourse() {
        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        var courseColumn = document.getElementById('class_list_column');
        courseColumn.appendChild(waitingIcon);
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
        var alreadyInserted = false;
        CourseSketch.dataManager.getAllCourses(function(courseList) {
            // ensure that we only insert once.
            if (!alreadyInserted) {
                alreadyInserted = true;
            } else {
                return;
            }
            var localCourseList = courseList;
            if (courseList instanceof CourseSketch.DatabaseException) {
                // we are cool because we are adding a new one.
                localCourseList = [];
            }
            var oldId = undefined;
            CourseSketch.dataManager.insertCourse(course, function(insertedCourse) {
                console.log("inserting course", insertedCourse);
                oldId = insertedCourse.id;
                localCourseList.unshift(insertedCourse);
                courseManagement.showCourses(localCourseList);
            }, function(updatedCourse) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                var oldElement = courseColumn.querySelector('#' + cssEscapeId(oldId));
                oldElement.id = updatedCourse.id.trim();
                oldElement.schoolItemData = updatedCourse;
            });
        }); // end getAllCourses
    };

    /**
     * Creates a new assignment with default values.
     * and adds it to the database.
     */
    courseManagement.addNewAssignment = function addNewAssignment() {
        var courseId = document.querySelector("#class_list_column .selectedBox").id;
        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        document.getElementById('assignment_list_column').appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();
        // by instructors
        var assignment = CourseSketch.PROTOBUF_UTIL.SrlAssignment();
        assignment.name = "Insert name";
        assignment.courseId = courseId;
        alert(courseId);
        assignment.description = "Insert description";
        // course.accessDate = "mm/dd/yyyy";
        // course.closeDate = "mm/dd/yyyy";
        CourseSketch.dataManager.getAllAssignmentsFromCourse(courseId, function(assignmentList) {
            var localAssignmentList = assignmentList;
            if (assignmentList instanceof CourseSketch.DatabaseException) {
                // no assignments exist or something went wrong
                localAssignmentList = [];
            }
            var firstAssignment = undefined;
            CourseSketch.dataManager.insertAssignment(assignment, function(assignment) {
                firstAssignment = assignment;
                localAssignmentList.push(assignment);
                courseManagement.showAssignments(localAssignmentList);
            }, function(assignment) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }

                // replaces object with an updated id
                removeObjectFromList(localAssignmentList, firstAssignment);
                localAssignmentList.push(assignment);

                // updates the course too!
                CourseSketch.dataManager.getAllCourses(function(courseList) {
                    courseManagement.showCourses(courseList);
                    courseManagement.showAssignments(localAssignmentList);
                });
            });
        });
    };

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
})();
