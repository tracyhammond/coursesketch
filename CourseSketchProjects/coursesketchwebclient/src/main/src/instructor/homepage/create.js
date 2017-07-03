validateFirstRun(document.currentScript);

(function() {
    var courseManagement = CourseSketch.courseManagement;
    /**
     * Function to be called when a lecture has finished editing.
     *
     * @param {String} attributeChanged
     *            The name of the protobuf attribute that changed
     * @param {String|Number|Object} oldValue
     *            The attribute's old value
     * @param {String|Number|Object} newValue
     *            The attribute's new value
     * @param {Element} element
     *            Protobuf element that has been edited
     */
    courseManagement.courseEndEdit = function(attributeChanged, oldValue, newValue, element) {
        var keyList = newValue.keys();
        var srlCourse = element.schoolItemData;
        console.log(srlCourse);
        newValue.forEach(function(value, key, mapObj) {
            console.log(key);
            srlCourse[key] = value;
        });
        console.log(srlCourse);
        CourseSketch.dataManager.updateCourse(srlCourse);
    };

    /**
     * Creates a new course with default values.
     *
     * Adds it to the database.
     */
    courseManagement.addNewCourse = function addNewCourse() {
        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        var courseColumn = document.getElementById('class_list_column');
        courseColumn.appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();
        // by instructors
        var course = CourseSketch.prutil.SrlCourse();
        // course.id = "Course_01";
        course.name = 'Insert name';
        course.description = 'Insert description';
        // course.semester = 'Should be in format: '_F13' (_F = Fall, Sp =
        // Spring, Su = Summer) ';
        // course.accessDate = 'mm/dd/yyyy';
        // course.closeDate = 'mm/dd/yyyy';
        var isInserting = false;
        CourseSketch.dataManager.getAllCourses(function(courseList) {
            // ensure that we only insert once.
            if (!isInserting) {
                isInserting = true;
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
                console.log('inserting course', insertedCourse);
                oldId = insertedCourse.id;
                localCourseList.unshift(insertedCourse);
                courseManagement.showCourses(localCourseList);
            }, function(updatedCourse) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                var oldElement = courseColumn.querySelector(cssEscapeId(oldId));
                oldElement.id = updatedCourse.id;
                oldElement.schoolItemData = updatedCourse;
            });
        }); // end getAllCourses
    };

    /**
     * Function to be called when a lecture has finished editing.
     *
     * @param {String} attributeChanged
     *            the name of the protobuf attribute that changed
     * @param {String|Number|Object} oldValue
     *            the attribute's old value
     * @param {String|Number|Object} newValue
     *            the attribute's new value
     * @param {Element} element
     *            protobuf element that has been edited
     */
    courseManagement.assignmentEndEdit = function(attributeChanged, oldValue, newValue, element) {
        var assignment = element.schoolItemData;
        newValue.forEach(function(value, key, mapObj) {
            console.log(key);
            assignment[key] = value;
        });
        CourseSketch.dataManager.updateAssignment(assignment);
    };

    /**
     * Creates a new assignment with default values.
     * And adds it to the database.
     */
    courseManagement.addNewAssignment = function addNewAssignment() {
        var courseId = document.querySelector('#class_list_column .selectedBox').id;
        var assignmentColumn = document.getElementById('assignment_list_column');

        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        assignmentColumn.appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();

        // by instructors
        var assignment = CourseSketch.prutil.SrlAssignment();
        assignment.name = 'Insert name';
        assignment.courseId = courseId;
        assignment.description = 'Insert description';
        // course.accessDate = 'mm/dd/yyyy';
        // course.closeDate = 'mm/dd/yyyy';
        var isInserting = false;
        CourseSketch.dataManager.getAllAssignmentsFromCourse(courseId, function(assignmentList) {
            // ensure that we only insert once.
            if (!isInserting) {
                isInserting = true;
            } else {
                return;
            }
            var localAssignmentList = assignmentList;
            if (assignmentList instanceof CourseSketch.DatabaseException) {
                // no assignments exist or something went wrong
                localAssignmentList = [];
            }
            var oldId = undefined;
            CourseSketch.dataManager.insertAssignment(assignment, function(insertedAssignment) {
                oldId = insertedAssignment.id;
                localAssignmentList.unshift(insertedAssignment);
                courseManagement.showAssignments(localAssignmentList);
            }, function(updateAssignment) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                var oldElement = assignmentColumn.querySelector(cssEscapeId(oldId));
                oldElement.id = updateAssignment.id.trim();
                oldElement.schoolItemData = updateAssignment;

                // updates the course too! (basically the assignment list)
                CourseSketch.dataManager.getCourse(courseId, function(course) {
                    if (isUndefined(course) || course instanceof CourseSketch.DatabaseException) {
                        throw new Error('Course is not defined while trying to add assignment.');
                    }
                    document.getElementById('class_list_column').querySelector(cssEscapeId(courseId)).schoolItemData = course;
                });
            });
        });
    };

    /**
     * Function to be called when a problem has finished editing.
     *
     * @param {String} attributeChanged
     *            the name of the protobuf attribute that changed
     * @param {String|Number|Object} oldValue
     *            the attribute's old value
     * @param {String|Number|Object} newValue
     *            the attribute's new value
     * @param {Element} element
     *            protobuf element that has been edited
     */
    courseManagement.problemEndEdit = function(attributeChanged, oldValue, newValue, element) {
        var problem = element.schoolItemData;
        newValue.forEach(function(value, key) {
            if (key === 'description') {
                var bankProblem = problem.getProblemInfo();
                bankProblem.questionText = value;
                problem.setProblemInfo(bankProblem);
                CourseSketch.dataManager.updateBankProblem(bankProblem);
            } else {
                problem[key] = value;
            }
        });
        CourseSketch.dataManager.updateCourseProblem(problem);
    };

    /**
     * Lets the instructor choose an existing problem.
     */
    courseManagement.chooseExistingProblem = function() {
        var courseId = document.querySelector('#class_list_column .selectedBox').id;
        var assignmentId = document.querySelector('#assignment_list_column .selectedBox').id;
        var problemSelection = document.createElement('problem-selection');

        problemSelection.setAcceptedCallback(function(selectedProblems) {
            document.body.removeChild(problemSelection);
            for (var i = 0; i < selectedProblems.length; i++) {
                courseManagement.addNewCourseProblem(selectedProblems[i]);
            }
        });

        problemSelection.setCanceledCallback(function() {
            document.body.removeChild(problemSelection);
        });

        document.body.appendChild(problemSelection);
        problemSelection.loadProblems(courseId, assignmentId, 0);
    };

    /**
     * Creates a new bank problem and course problem with default values and adds it to the database.
     *
     * Displays the problem after it is added.
     *
     * @param {String|Undefined} existingBankProblem - If loading an existing bank problem then the value is the Id. Otherwise it is undefined.
     */
    courseManagement.addNewCourseProblem = function addNewCourseProblem(existingBankProblem) {
        var courseId = document.querySelector('#class_list_column .selectedBox').id;
        var assignmentId = document.querySelector('#assignment_list_column .selectedBox').id;
        var problemColumn = document.getElementById('problem_list_column');

        var waitingIcon = CourseSketch.courseManagement.waitingIcon;
        problemColumn.appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();


        var courseProblem = CourseSketch.prutil.SrlProblem();
        courseProblem.courseId = courseId;
        courseProblem.name = 'Insert Problem Name';
        courseProblem.assignmentId = assignmentId;
        courseProblem.description = '';

        if (isUndefined(existingBankProblem)) {
            var bankProblem = CourseSketch.prutil.SrlBankProblem();
            bankProblem.questionText = prompt('Please enter the question text', 'Default Question Text');
            var permissions = CourseSketch.prutil.SrlPermission();
            permissions.userPermission = [ courseId ];
            bankProblem.accessPermission = permissions;
            courseProblem.setProblemInfo(bankProblem);
        } else {
            courseProblem.setProblemBankId(existingBankProblem);
        }
        var isInserting = false;
        CourseSketch.dataManager.getAllProblemsFromAssignment(assignmentId, function(problemList) {
            // ensure that we only insert once.
            if (!isInserting) {
                isInserting = true;
            } else {
                return;
            }
            var localProblemList = problemList;
            if (problemList instanceof CourseSketch.DatabaseException) {
                // no problems exist or something went wrong
                localProblemList = [];
            }
            var oldId = undefined;
            CourseSketch.dataManager.insertCourseProblem(courseProblem, function(insertedProblem) {
                oldId = insertedProblem.id;
                localProblemList.unshift(insertedProblem);
                courseManagement.showProblems(localProblemList);
            }, function(updateProblem) {
                if (waitingIcon.isRunning()) {
                    waitingIcon.finishWaiting();
                }
                var oldElement = problemColumn.querySelector(cssEscapeId(oldId));
                oldElement.id = updateProblem.id.trim();
                oldElement.schoolItemData = updateProblem;

                // updates the course too! (basically the problem list)
                CourseSketch.dataManager.getAssignment(assignmentId, function(assignment) {
                    if (isUndefined(assignment) || assignment instanceof CourseSketch.DatabaseException) {
                        throw new Error('Course is not defined while trying to add problem.');
                    }
                    document.getElementById('assignment_list_column').querySelector(cssEscapeId(assignmentId)).schoolItemData = assignment;
                });
            });
        });
    };

    courseManagement.courseSelectionManager.addClickSelectionListener({
        removed: function(element) {
            hideButton('assignment_button');
            hideButton('problem_button');
            destroyAdvancedEditCard();
        },
        selected: function(element) {
            showButton('assignment_button');
            createAdvancedEditCard(element, courseManagement.courseEndEdit);
        }
    });

    courseManagement.assignmentSelectionManager.addClickSelectionListener({
        removed: function(element) {
            hideButton('problem_button');
            destroyAdvancedEditCard();
        },
        selected: function(element) {
            showButton('problem_button');
            destroyAdvancedEditCard();
            createAdvancedEditCard(element, courseManagement.assignmentEndEdit);
        }
    });

    courseManagement.problemSelectionManager.addClickSelectionListener({
        removed: function(element) {
            destroyAdvancedEditCard();
        },
        selected: function(element) {
            destroyAdvancedEditCard();
            createAdvancedEditCard(element, courseManagement.problemEndEdit);
        }
    });

    /**
     * Sets an element (should be a button) with the given id to be visible.
     *
     * @param {String} id - The id of the button that should be shown.
     */
    function showButton(id) {
        var element = document.getElementById(id);
        if (element) {
            element.style.display = 'block';
        }
    }

    /**
     * Sets an element (should be a button) with the given id to be invisible.
     *
     * @param {String} id - The id of the button that should be shown.
     */
    function hideButton(id) {
        var element = document.getElementById(id);
        if (element) {
            element.style.display = 'none';
        }
    }

    function destroyAdvancedEditCard() {
        $(document.querySelectorAll('#advancedEditHolder')[0]).html('');
    }

    function createAdvancedEditCard(element, saveCallback) {
        /*
        try {
            document.querySelectorAll('#advancedEditHolder')[0].close();
        } catch(adfsg) {

        }
        */
        var childElement = element.createAdvanceEditPanel(element, document.querySelectorAll('#advancedEditHolder')[0], saveCallback);
        $(document.querySelectorAll('#advancedEditHolder')[0]).modal({
            dismissible: true, // Modal can be dismissed by clicking outside of the modal
            opacity: 0.5, // Opacity of modal background
            inDuration: 300, // Transition in duration
            outDuration: 200, // Transition out duration
            startingTop: '4%', // Starting top style attribute
            endingTop: '10%' // Ending top style attribute
        }
        );
        childElement.style.display = '';
       // document.querySelectorAll('#advancedEditHolder')[0].open = true;
    }
})();
