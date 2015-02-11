validateFirstRun(document.currentScript);

CourseSketch.courseManagement.waitingIcon = (function() {
    var manage = new WaitScreenManager();
    manage.waitIconText = "loading data";
    return manage.setWaitType(manage.TYPE_WAITING_ICON).build();
})();
(function() {

    var waitingIcon = CourseSketch.courseManagement.waitingIcon;
    var courseManagement = CourseSketch.courseManagement;

    /**
     * Polls for all updates to the user and then shows the courses.
     *
     * This will wait till the database is ready before it polls for updates and
     * shows the courses.
     */
    CourseSketch.courseManagement.initializeCourseManagment = function() {
        if (!document.querySelector('#class_list_column')) {
            return false;
        }
        document.querySelector('#class_list_column').appendChild(waitingIcon);
        CourseSketch.courseManagement.waitingIcon.startWaiting();

        var loadCourses = function(courseList) {
            if (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting();
            }
            courseManagement.showCourses(courseList);
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
    courseManagement.showCourses = function showCourses(courseList) {
        var builder = new SchoolItemBuilder();
        if (CourseSketch.connection.isInstructor === true) {
            builder.setInstructorCard(true);
        }
        builder.showImage = false;
        builder.setBoxClickFunction(function(course) {
            courseManagement.courseClicked(course);
        });

        if (courseList instanceof CourseSketch.DatabaseException || courseList.length == 0) {
            if (CourseSketch.connection.isInstructor) {
                builder.setEmptyListMessage('Please Create a new course to get started!');
            } else {
                builder.setEmptyListMessage('Please add a new course to get started');
            }
            courseList = [];
        }

        builder.setList(courseList);
        builder.build(document.querySelector('#class_list_column'));
        setNotSelectedMessage(2);
    };

    /**
     * Called when a user clicks on a course school item.
     * This loads the assignments from the database then calls "showAssignments" to display them.
     */
    courseManagement.courseClicked = function(course) {
        var classColumn = document.querySelector('#class_list_column');
        setNotSelectedMessage(2);

        // note that query selector does not work on ids that start with a number.
        changeSelection(classColumn.querySelector(cssEscapeId(course.id)), courseSelectionManager);
        assignmentSelectionManager.clearAllSelectedItems();
        problemSelectionManager.clearAllSelectedItems();

        // waiting icon
        document.querySelector('#assignment_list_column').appendChild(waitingIcon);
        waitingIcon.startWaiting();

        // we can make this faster because we have the list of assignments
        CourseSketch.dataManager.getAssignments(course.assignmentList, function(assignmentList) {
            courseManagement.showAssignments(assignmentList, course);
        }, function(assignmentList) {
            courseManagement.showAssignments(assignmentList, course);
            if (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting(); // stops the
                // waiting icon
            }
        });
    }

    /**
     * Called to show a specific set of assignments with the given list.
     */
    courseManagement.showAssignments = function(assignmentList, course) {
        console.log(assignmentList);
        var builder = new SchoolItemBuilder();
        if (CourseSketch.connection.isInstructor === true) {
            builder.setInstructorCard(true);
        }
        builder.setEmptyListMessage('There are no assignments for this course!');
        if (assignmentList instanceof CourseSketch.DatabaseException) {
            if (!isUndefined(course) && course.getState() != null &&!(course.getState().accessible)) {
                builder.setEmptyListMessage('This course is currently not available. Please contact the instructor to let you view the assignments');
            }
            assignmentList = [];
        }

        builder.setList(assignmentList);
        builder.showImage = false;

        builder.setBoxClickFunction(function(assignment) {
            courseManagement.assignmentClicked(assignment);
        });
        builder.build(document.querySelector('#assignment_list_column'));
        document.querySelector('#assignment_list_column').appendChild(waitingIcon); // because it was probably removed
    }


    /**
     * Called when an assignment is clicked.
     */
    courseManagement.assignmentClicked = function(assignment) {
        var assignmentColumn = document.querySelector('#assignment_list_column');
        changeSelection(assignmentColumn.querySelector(cssEscapeId(assignment.id)), assignmentSelectionManager);
        problemSelectionManager.clearAllSelectedItems();

        // waiting icon
        document.getElementById('problem_list_column').appendChild(waitingIcon);
        waitingIcon.startWaiting();
        CourseSketch.dataManager.getCourseProblems(assignment.problemList,function(problemList) {
            courseManagement.showProblems(problemList, assignment);
        }, function(problemList) {
            courseManagement.showProblems(problemList, assignment);
            if (waitingIcon.isRunning()) {
                waitingIcon.finishWaiting(); // stops the
                // waiting icon
            }
        });
    }

    /**
     * Displays the list of problems for the user to pick from.
     * @param problemList The list of problems that are wanting to be showed
     * @param assignment (optional) The assignment that created this problem list
     */
    courseManagement.showProblems = function(problemList, assignment) {
        var builder = new SchoolItemBuilder();
        if (CourseSketch.connection.isInstructor === true) {
            builder.setInstructorCard(true);
        }
        builder.setEmptyListMessage('There are no problems for this assignment!');
        if (problemList instanceof CourseSketch.DatabaseException) {
            problemList = [];
            if (!isUndefined(assignment) && assignment.getState() != null && !assignment.getState().accessible) {
                builder.setEmptyListMessage('This assignment is currently not available. '
                    + 'Please contact the instructor to let you view the problems');
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
        builder.setBoxClickFunction(courseManagement.problemClicked);
        builder.build('problem_list_column');
    };

    /**
     * Called when a problem is displayed.
     */
    courseManagement.problemClicked = function(problem) {
        var problemColumn = document.querySelector('#problem_list_column');
        var clickedElement = problemColumn.querySelector(cssEscapeId(problem.id));

        if (problemSelectionManager.isItemSelected(clickedElement)) {
            var itemNumber = clickedElement.dataset.item_number;
            CourseSketch.dataManager.addState("currentProblemIndex", itemNumber);
            CourseSketch.dataManager.addState("currentAssignment", problem.assignmentId);
            CourseSketch.dataManager.addState("CURRENT_QUESTION", problem.id);
            // change source to the problem page! and load problem
            if (CourseSketch.connection.isInstructor) {
                // solution editor page!
                CourseSketch.redirectContent("/src/instructor/review/multiviewGrading.html", "Grading problems!");
            } else {
                CourseSketch.redirectContent("/src/student/experiment/experiment.html", "Starting Problem");
            }
        } else {
            // TODO: find a more lightweight popup library
            /*
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
            */
            // note that queryselector is not allowed on these types of ids
            changeSelection(clickedElement, problemSelectionManager);
        }
    }

    function setNotSelectedMessage(number) {
        var builder = new SchoolItemBuilder();

        if (number > 0) {
            builder.setEmptyListMessage('Please select an assignment to see the list of problems.');
            builder.build(document.querySelector('#problem_list_column'));
        }

        if (number > 1) {
            builder.setEmptyListMessage('Please select a course to see the list of assignments.');
            builder.build(document.querySelector('#assignment_list_column'));
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
})();
