validateFirstRun(document.currentScript);

/**
 * @namespace multiViewPage
 * This Multiview page goes off a single problem at a time and loads all student experiments of that problem id.
 *
 */
(function() {
    CourseSketch.multiViewPage.waitScreenManager = new WaitScreenManager();

    $(document).ready(function() {
        CourseSketch.multiViewPage.renderer = new CourseSketch.ProblemRenderer(document.getElementById('problemPanel'));
        CourseSketch.multiViewPage.renderer.setStartWaitingFunction(CourseSketch.multiViewPage.addWaitOverlay);
        CourseSketch.multiViewPage.renderer.setFinishWaitingFunction(CourseSketch.multiViewPage.removeWaitOverlay);
        /**
         * Closes the dialog panel.
         */
        document.getElementById('dialogPanel').querySelector('button').onclick = function() {
            document.getElementById('dialogPanel').close();
        };
        CourseSketch.dataManager.waitForDatabase(function() {
            var navPanel = document.querySelector('navigation-panel');
            var navigator = getNav();
            var assignmentId = CourseSketch.dataManager.getState('currentAssignment');
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            var addCallback = isUndefined(navPanel.dataset.callbackset);

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                navPanel.dataset.callbackset = '';
                navigator.addCallback(function(navigatorFromCallback) {
                    multiviewSketchDelete();
                    createMvList(navigatorFromCallback);
                });
            }

            navigator.setSubgroupNavigation(false);
            if (!isUndefined(assignmentId)) {
                navigator.resetNavigation(assignmentId, parseInt(problemIndex, 10));
            } else if (addCallback) {
                navigator.refresh();
            }
        });
    });

    /**
     * Gets all experiments that hold the current problem id and places them is sketchList.
     *
     * @param {Function} callback - called when all of the sketches are loaded.
     * @param {AssignmentNavigator} navigator - The navigator used to navigate the assignment.
     * @memberof multiViewPage
     */
    function getSubmissions(callback, navigator) {
        console.log(navigator.getSubmissionIdentifier());
        CourseSketch.dataManager.getAllExperiments(navigator.getSubmissionIdentifier(), function(submission) {
            console.log(submission);
            if (isException(submission)) {
                CourseSketch.clientException(submission);
                return;
            }
            if (isUndefined(submission)) {
                alert('This problem has no student submissions.');
                return;
            }
            if (!isUndefined(callback)) {
                callback(submission, navigator);
            }
        });
    }

    /**
     * Used to get list of experiments and then calls createMvSketch to create all sketches on to the grade screen.
     *
     * @param {AssignmentNavigator} navigator - The navigator used to navigate the assignment.
     * @memberof multiViewPage
     */
    function createMvList(navigator) {
        getSubmissions(createMvSketch, navigator);
    }

    /**
     * Creates a multiview sketch panel and attaches it to the grading area this can be done dynamically.
     *
     * @param {Array<SrlExperiment>} submissions - An array of sketches that the MvPanel creates.
     * @param {AssignmentNavigator} navigator - The navigator used to navigate the assignment.
     * @memberof multiViewPage
     */
    function createMvSketch(submissions, navigator) {
        var questionType = navigator.getCurrentInfo().questionType;
        for (var i = 0; i < submissions.length; i++) {
            var submissionPanel = document.createElement('mv-sketch');
            document.querySelector('.submissions').appendChild(submissionPanel);
            submissionPanel.setUserId(submissions[i].userId);
            submissionPanel.setSubmission(questionType, submissions[i].getSubmission());
            submissionPanel.setSubmissionClickedFunction(function() {
                CourseSketch.multiViewPage.loadProblem(navigator, this.getSubmission());
            });

            var protoGrade = CourseSketch.prutil.ProtoGrade();
            protoGrade.userId = submissions[i].userId;
            submissionPanel.courseId = protoGrade.courseId = submissions[i].courseId;
            submissionPanel.assignmentId = protoGrade.assignmentId = submissions[i].assignmentId;
            submissionPanel.problemId = protoGrade.problemId = submissions[i].problemId;
            console.log('before I get the grade ', protoGrade);

            (function(submissionPanel) {
                // Only one of the callbacks will be called right now...
                CourseSketch.dataManager.getGrade(protoGrade, function(dbGrade) {
                    console.log('LOADING GRADE FROM SERVER', dbGrade);
                    submissionPanel.setGrade(dbGrade.currentGrade);
                    var history = dbGrade.gradeHistory;
                    if (!isUndefined(history)) {
                        submissionPanel.setComment(history[history.length - 1].comment);
                    }
                });
            })(submissionPanel);
        }
    }

    /**
     * Returns the navigation panel element to be used by other pages.
     *
     * @memberof multiViewPage
     * @returns {AssignmentNavigator} The navigator of this page.
     */
    function getNav() {
        return document.querySelector('navigation-panel').getNavigator();
    }

    /**
     * Deletes the sketch data in the sketch-area element.
     *
     * @memberof multiViewPage
     */
    function multiviewSketchDelete() {
        var parent = document.getElementById('submission-area');
        parent.innerHTML = '';
    }

    /**
     * Loads the problem, called every time a user navigates to a different problem.
     *
     * @param {AssignmentNavigator} navigator - The navigator used to navigate the assignment.
     * @param {SrlSubmission} submissionData - the data that was submitted.
     * @memberof multiViewPage
     */
    CourseSketch.multiViewPage.loadProblem = function(navigator, submissionData) {
        document.getElementById('dialogPanel').show();
        var problemInfo = navigator.getCurrentInfo();
        var bankProblem = CourseSketch.prutil.SrlBankProblem();
        bankProblem.questionType = problemInfo.questionType;
        CourseSketch.multiViewPage.renderer.renderSubmission(bankProblem, submissionData, function() {
            console.log('submission Loaded');
            var parentPanel = document.getElementById('problemPanel');
            parentPanel.problemIndex = navigator.getCurrentNumber();
            parentPanel.setProblemType(problemInfo.questionType);
            parentPanel.refreshPanel();
            parentPanel.isStudent = false;
            parentPanel.isGrader = true;

            // THIS WILL BE DONE A TINY BIT LATER
            parentPanel.setWrapperFunction(function(submission) {
                var studentExperiment = CourseSketch.prutil.SrlExperiment();
                navigator.setSubmissionInformation(studentExperiment, true);
                studentExperiment.submission = submission;
                return studentExperiment;
            });
        });
    };

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     *
     * @memberof multiViewPage
     */
    CourseSketch.multiViewPage.addWaitOverlay = function() {
        CourseSketch.multiViewPage.waitScreenManager.buildOverlay(document.querySelector('body'));
        CourseSketch.multiViewPage.waitScreenManager.buildWaitIcon(document.getElementById('overlay'));
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     *
     * @memberof multiViewPage
     */
    CourseSketch.multiViewPage.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById('overlay')) && document.getElementById('overlay') !== null) {
            document.querySelector('body').removeChild(document.getElementById('overlay'));
        }
    };
})();
