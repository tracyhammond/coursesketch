validateFirstRun(document.currentScript);

(function() {
    CourseSketch.studentExperiment.waitScreenManager = new WaitScreenManager();
    var questionTextPanel = undefined;
    var currentProblem = undefined;
    var problemRenderer = undefined;
    var originalMap = undefined;
    var waitingElement = undefined;
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector('navigation-panel');
            questionTextPanel = document.querySelector('problem-text-panel');
            var navigator = panel.getNavigator();
            var assignmentId = CourseSketch.dataManager.getState('currentAssignment');
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

            problemRenderer = new CourseSketch.ProblemRenderer(document.getElementById('problemPanel'));
            problemRenderer.setStartWaitingFunction(startWaiting);
            problemRenderer.setFinishWaitingFunction(finishWaiting);

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
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
     * Loads the problem, called every time a user navigates to a different problem.
     *
     * @param {AssignmentNavigator} navigator - The assignment navigator.
     */
    function loadProblem(navigator) {
        var bankProblem = navigator.getCurrentInfo();
        problemRenderer.reset();
        problemRenderer.setStartWaitingFunction(startWaiting);
        problemRenderer.setFinishWaitingFunction(finishWaiting);
        currentProblem = bankProblem;
        loadBankProblem(bankProblem, navigator);
    }

    /**
     * Loads a bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem to load.
     */
    function loadBankProblem(bankProblem, navigator) {
        questionTextPanel.setRapidProblemText(bankProblem.getQuestionText());

        problemRenderer.renderBankProblem(bankProblem, function() {
            CourseSketch.dataManager.getSubmission(navigator.getGroupId(), function(submission) {
                if (CourseSketch.isException(submission)) {
                    CourseSketch.clientException(submission);
                    return;
                }
                problemRenderer.renderSubmission(bankProblem, submission, function() {
                    setupSubmissionPanel(document.getElementById('problemPanel'), navigator, bankProblem.questionType);
                });
            });
        });
    }

    function setupSubmissionPanel(submissionPanel, navigator, questionType) {
        submissionPanel.problemIndex = navigator.getCurrentNumber();
        submissionPanel.setProblemType(questionType);
        submissionPanel.refreshPanel();
        submissionPanel.isStudent = true;
        submissionPanel.isGrader = false;

        submissionPanel.setWrapperFunction(function(submission) {
            var studentExperiment = CourseSketch.prutil.SrlExperiment();
            navigator.setSubmissionInformation(studentExperiment, true);
            console.log('student experiment data set', studentExperiment);
            studentExperiment.submission = submission;
            return studentExperiment;
        });
    }

    function startWaiting() {
        document.getElementById('percentBar').innerHTML = '';
        waitingElement = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.studentExperiment.addWaitOverlay();
        document.getElementById('percentBar').appendChild(waitingElement);
        waitingElement.startWaiting();
        var realWaiting = waitingElement.finishWaiting.bind(waitingElement);

        /**
         * Called when the sketch surface is done loading to remove the overlay.
         */
        waitingElement.finishWaiting = function() {
            realWaiting();
            CourseSketch.studentExperiment.removeWaitOverlay();
        };
    }

    function finishWaiting() {
        if (!isUndefined(waitingElement) && waitingElement.isRunning()) {
            waitingElement.finishWaiting();
            CourseSketch.studentExperiment.removeWaitOverlay();
        }
    }

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    CourseSketch.studentExperiment.addWaitOverlay = function() {
        CourseSketch.studentExperiment.waitScreenManager.buildOverlay(document.querySelector('body'));
        CourseSketch.studentExperiment.waitScreenManager.buildWaitIcon(document.getElementById('overlay'));
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    CourseSketch.studentExperiment.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById('overlay')) && document.getElementById('overlay') !== null) {
            document.querySelector('body').removeChild(document.getElementById('overlay'));
        }
    };
})();
