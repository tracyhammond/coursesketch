CourseSketch.courseManagement.waitingIcon = (function() {
    var manage = new WaitScreenManager();
    manage.waitIconText = "loading data";
    return manage.setWaitType(manage.TYPE_WAITING_ICON).build();
})();

(function(document) {

    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    var localScope = CourseSketch.courseManagement;

    /**
     * Polls for all updates to the user and then shows the courses.
     *
     * This will wait till the database is ready before it polls for updates and
     * shows the courses.
     */
    CourseSketch.courseManagement.initializeCourseManagment = function(localDocument) {
        var doc = document;
        if (!isUndefined(localDocument)) {
            doc = localDocument;
        }
        if (!doc.querySelector('#class_list_column')) {
            return false;
        }
        doc.querySelector('#class_list_column').appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();

        var loadCourses = function(courseList) {
            if (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting();
            }
            localScope.showCourses(courseList);
        };
        if (CourseSketch.dataManager.isDatabaseReady()) {
            CourseSketch.dataManager.pollUpdates(function() {
                CourseSketch.dataManager.getAllCourses(loadCourses);
            });
        } else {
            var intervalVar = setInterval(function() {
                if (CourseSketch.dataManager.isDatabaseReady()) {
                    clearInterval(intervalVar);
                    CourseSketch.dataManager.pollUpdates(function() {
                        CourseSketch.dataManager.getAllCourses(loadCourses);
                    });
                }
            }, 100);
        }
    };

    /**
     * Given a list of {@link SrlCourse} a bunch of school items are built then
     * added to the clss_list_column div.
     */
    localScope.showCourses = function showCourses(courseList, localDocument) {
        var doc = document;
        if (!isUndefined(localDocument)) {
            doc = localDocument;
        }

        var builder = new SchoolItemBuilder();
        builder.setList(courseList);
        if (CourseSketch.connection.isInstructor) {
            builder.setInstructorCard(true);
        }
        builder.showImage = false;
        builder.setBoxClickFunction(function(course) {
            courseClickerFunction(course, doc)
        });
        builder.build(doc.querySelector('#class_list_column'));
        clearLists(2, localDocument);
    };

    /**
     * Called when a user clicks on a course school item. This then creates the
     * list of assignments for that course and then displays them.
     */
    function courseClickerFunction(course, doc) {
        clearLists(2, doc);

        // note that query selector does not work on ids that start with a number.
        changeSelection(doc.getElementById(course.id), courseSelectionManager);
        assignmentSelectionManager.clearAllSelectedItems();
        problemSelectionManager.clearAllSelectedItems();

        // waiting icon
        doc.querySelector('#assignment_list_column').appendChild(waitingIcon);
        waitingIcon.startWaiting();

        function buildSchoolList(assignmentList) {
            console.log(assignmentList);
            var builder = new SchoolItemBuilder();
            builder.setEmptyListMessage('There are no assignments for this course!');
            if (assignmentList == "NONEXISTANT_VALUE") {
                if (!(course.getState().accessible)) {
                    builder
                            .setEmptyListMessage('This course is currently not available. Please contact the instructor to let you view the assignments');
                }
                assignmentList = [];
            }

            builder.setList(assignmentList);
            builder.showImage = false;

            builder.setBoxClickFunction(function(assignment) {
                assignmentClickerFunction(assignment, doc);
            });
            builder.build(doc.querySelector('#assignment_list_column'));
            doc.querySelector('#assignment_list_column').appendChild(waitingIcon); // because it was probably removed
            if (CourseSketch.dataManager.getState("isInstructor")) {
                try {
                    replaceEditContent('html/instructor/course_management_frames/edit_course.html');
                } catch (exception) {

                }
                showButton('assignment_button');
            }
        }
        // we can make this faster because we have the list of assignments
        CourseSketch.dataManager.getAssignments(course.assignmentList, buildSchoolList, function(assignmentList) {
            buildSchoolList(assignmentList);
            if (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting(); // stops the
                // waiting icon
            }
        });
    }

    function assignmentClickerFunction(assignment) {
        // clears the problems
        changeSelection(assignment.id, assignmentSelectionManager);
        problemSelectionManager.clearAllSelectedItems();
        clearLists(1);

        // waiting icon
        document.getElementById('problem_list_column').appendChild(waitingIcon);
        waitingIcon.startWaiting();
        CourseSketch.dataManager
                .getCourseProblems(
                        assignment.problemList,
                        function(problemList) {
                            var builder = new SchoolItemBuilder();
                            builder.setEmptyListMessage('There are no problems for this assignment!');
                            if (problemList == "NONEXISTANT_VALUE") {
                                problemList = [];
                                if (!assignment.getState().accessible) {
                                    builder
                                            .setEmptyListMessage('This assignment is currently not avialable. Please contact the instructor to let you view the problems');
                                }
                            }
                            for (var i = 0; i < problemList.length; i++) {
                                var q = problemList[i].description;
                                if (isUndefined(q) || q == "") {
                                    var prob = problemList[i];
                                    if (!isUndefined(prob.problemInfo)) {
                                        var text = prob.getProblemInfo().getQuestionText();
                                        problemList[i].setDescription(text);
                                    } else {
                                        problemList[i].setDescription("No Description or question text");
                                    }
                                }
                            }
                            builder.setList(problemList);
                            builder.showImage = false;
                            builder.setBoxClickFunction(problemClickerFunction);
                            if (waitingIcon.isRunning()) {
                                waitingIcon.finishWaiting(); // stops the
                                // waiting icon
                            }
                            builder.build('problem_list_column');
                            if (CourseSketch.dataManager.getState("isInstructor")) {
                                try {
                                    replaceEditContent('html/instructor/course_managment_frames/edit_assignment.html');
                                } catch (exception) {

                                }
                                showButton('problem_button');
                            }
                        });
    }

    function problemClickerFunction(problem) {
        var id = problem.id;
        if (problemSelectionManager.isItemSelected(id)) {
            var element = document.getElementById(id);
            var itemNumber = element.dataset.item_number;
            CourseSketch.dataManager.addState("CURRENT_QUESTION_INDEX", itemNumber);
            CourseSketch.dataManager.addState("CURRENT_ASSIGNMENT", problem.assignmentId);
            CourseSketch.dataManager.addState("CURRENT_QUESTION", id);
            // change source to the problem page! and load problem
            if (CourseSketch.dataManager.getState("isInstructor")) {
                // solution editor page!
                CourseSketch.redirectContent("html/instructor/instructorproblemlayout.html", "");
            } else {
                CourseSketch.redirectContent("html/student/problemlayout.html", "Starting Problem");
            }
        } else {
            var element = document.getElementById(id);
            var myOpenTip = new Opentip(element, {
                target : element,
                tipJoint : "bottom"
            });
            myOpenTip.prepareToShow(); // Shows the tooltip after the given
            // delays. This could get interrupted

            if (CourseSketch.dataManager.getState("isInstructor")) {
                myOpenTip.setContent("Click again to edit the solution"); // Updates
                // Opentips
                // content
            } else {
                myOpenTip.setContent("Click again to open up a problem"); // Updates
                // Opentips
                // content
            }

            var pastToolTip = problemSelectionManager['currentToolTip'];
            if (pastToolTip) {
                pastToolTip.deactivate();
            }
            problemSelectionManager['currentToolTip'] = myOpenTip;
            changeSelection(id, problemSelectionManager);
        }

        if (CourseSketch.dataManager.getState("isInstructor")) {
            try {
                replaceEditContent('html/instructor/course_managment_frames/edit_problem.html');
            } catch (exception) {

            }
            showButton('problem_button');
        }
    }

    function showButton(id) {
        var element = document.getElementById(id);
        if (element) {
            element.style.display = "block";
        }
    }

    function hideButton(element) {
        if (element) {
            element.style.display = "none";
        }
    }

    function clearLists(number, localDocument) {
        var doc = document;
        if (!isUndefined(localDocument)) {
            doc = localDocument;
        }
        var builder = new SchoolItemBuilder();

        if (number > 0) {
            hideButton(doc.querySelector('#problem_button'));
            builder.setEmptyListMessage('Please select an assignment to see the list of problems.');
            builder.build(doc.querySelector('#problem_list_column'));
        }

        if (number > 1) {
            hideButton(doc.querySelector('#assignment_button'));
            builder.setEmptyListMessage('Please select a course to see the list of assignments.');
            builder.build(doc.querySelector('#assignment_list_column'));
        }
    }

    function changeSelection(id, selectionManager) {
        selectionManager.clearAllSelectedItems();
        selectionManager.addSelectedItem(id);
    }

    function manageHeight() {
        var iframe = document.getElementById('edit_frame_id');
        var innerDoc = iframe.contentDocument || iframe.contentWindow.document;
        // Gets the visual height.
        if (innerDoc) {
            var iFrameElement = innerDoc.getElementById('iframeBody') || innerDoc.getElementsByTagName('body')[0];
            if (!iFrameElement) {
                return;
            }
            var height = iFrameElement.scrollHeight;
            iframe.height = height;
        }
    }

    /**
     * Given the source this will create an iframe that will manage its own
     * height. TODO: make this more general.
     */
    function replaceEditContent(src) {

        function onload(event) {
            var toReplace = document.getElementById('editable_unit');
            removeAllChildren(toReplace);
            var link = event.srcElement;
            var content = link.import.querySelector("#iframeBody");
            if (src && content) {
                toReplace.appendChild(content.cloneNode(true));
            } else {
                toReplace.innerHTML = '<h2 style = "text-align:center">Nothing is selected yet</h2>'
                        + '<h2 style = "text-align:center">Click an item to edit</h2>';
            }
        }

        function onerror(event) {
            var toReplace = document.getElementById('editable_unit');
            removeAllChildren(toReplace);
            toReplace.innerHTML = '<h2 style = "text-align:center">Nothing is selected yet</h2>'
                    + '<h2 style = "text-align:center">Click an item to edit</h2>';
        }

        try {
            loader.replaceFile(false, src, "html", onload, onerror, 'editable_import', 'editable_import');
        } catch (exception) {
            loader.loadFile(src, "html", onload, onerror, 'editable_import');
        }
    }

    var courseSelectionManager = new clickSelectionManager();
    var assignmentSelectionManager = new clickSelectionManager();
    var problemSelectionManager = new clickSelectionManager();
})(document.currentScript.ownerDocument);
