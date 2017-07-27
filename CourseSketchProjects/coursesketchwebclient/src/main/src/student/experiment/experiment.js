validateFirstRun(document.currentScript);

(function() {
    CourseSketch.studentExperiment.waitScreenManager = new WaitScreenManager();
    var questionTextPanel = undefined;
    var currentProblem = undefined;
    var problemRenderer = undefined;
    var feedbackRenderer = undefined;
    var waiter = undefined;
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector('navigation-panel');
            questionTextPanel = document.querySelector('problem-text-panel');
            var navigator = panel.getNavigator();
            var assignmentId = CourseSketch.dataManager.getState('currentAssignment');
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

            waiter = new DefaultWaiter(CourseSketch.studentExperiment.waitScreenManager,
                document.getElementById('percentBar'));

            var problemPanel = document.getElementById('problemPanel');
            problemRenderer = new CourseSketch.ProblemRenderer(problemPanel);
            problemRenderer.setStartWaitingFunction(waiter.startWaiting);
            problemRenderer.setFinishWaitingFunction(waiter.finishWaiting);
            feedbackRenderer = new CourseSketch.FeedbackRenderer(problemPanel, undefined, document.getElementById('feedbackBackground'));
            problemPanel.setOnSavedListener(submissionSaved, submissionErrored);

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
        feedbackRenderer.reset();
        problemRenderer.reset();
        problemRenderer.setIsStudentProblem(true);
        feedbackRenderer.setStartWaitingFunction(waiter.startWaiting);
        feedbackRenderer.setFinishWaitingFunction(waiter.finishWaiting);
        problemRenderer.setStartWaitingFunction(waiter.startWaiting);
        problemRenderer.setFinishWaitingFunction(waiter.finishWaiting);
        currentProblem = bankProblem;
        loadBankProblem(bankProblem, navigator);
    }

    /**
     * Loads a bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem to load.
     * @param {AssignmentNavigator} navigator - The navigator that is performing navigation.
     */
    function loadBankProblem(bankProblem, navigator) {
        questionTextPanel.setRapidProblemText(bankProblem.getQuestionText());

        problemRenderer.startWaiting();
        CourseSketch.dataManager.getExperiment(navigator.getSubmissionIdentifier(), function(submission) {
            if (CourseSketch.isException(submission)) {
                CourseSketch.clientException(submission);
                // Do not return we still need to set up the submission to look at feedback
            }
            problemRenderer.renderSubmission(bankProblem, submission, function() {
                setupSubmissionPanel(document.getElementById('problemPanel'), navigator, bankProblem.questionType);
                // FUTURE: Render the last feedback for this problem.
                // feedbackRenderer.renderFeedback()
            }, true);
        });
    }

    /**
     * Sets data in the submission panel.
     *
     * @param {SubmissionPanel} submissionPanel - The element that has submission data.
     * @param {AssignmentNavigator} navigator - The navigator that is performing navigation.
     * @param {QuestionType} questionType - The type of question on this panel.
     */
    function setupSubmissionPanel(submissionPanel, navigator, questionType) {
        submissionPanel.problemIndex = navigator.getCurrentNumber();
        submissionPanel.setProblemType(questionType);
        submissionPanel.refreshPanel();
        submissionPanel.isStudent = true;
        submissionPanel.isGrader = false;

        submissionPanel.setWrapperFunction(function(submission) {
            startFeedbackRenderer(submission);

            var studentExperiment = CourseSketch.prutil.SrlExperiment();
            navigator.setSubmissionInformation(studentExperiment, true);
            console.log('student experiment data set', studentExperiment);
            studentExperiment.submission = submission;
            if (!isUndefined(currentProblem.solutionId) && currentProblem.solutionId !== null) {
                studentExperiment.solutionId = currentProblem.solutionId;
            }
            return studentExperiment;
        });
    }

    /**
     * sets up the feedback renderer to listen for feedback.
     *
     * @param {SrlSubmission} submission - The submission the user just submitted.
     */
    function startFeedbackRenderer(submission) {
        try {
            feedbackRenderer.listenToFeedback(currentProblem, submission, true, function() {
                console.log('feedback rendered');
            });

            // prevent students from being stupid while waiting for feedback
            // Only wait 3 seconds max
            feedbackRenderer.startWaiting();
            setTimeout(function() {
                feedbackRenderer.finishWaiting();
            }, 3000);
        } catch (exception) {
            console.log(exception);
        }
    }

    /**
     * Called when the submission is successfully saved.
     *
     * @param {Request} request - The request response from the server.
     */
    function submissionSaved(request) {
        Materialize.toast('Submission Was saved successfully', 4000);
    }

    /**
     * Called when the submission fails to save.
     *
     * @param {BaseException} exception - The exception that occurred.
     */
    function submissionErrored(exception) {
        console.log(exception);
        Materialize.toast(exception.getMessage(), 4000);
    }
})();
