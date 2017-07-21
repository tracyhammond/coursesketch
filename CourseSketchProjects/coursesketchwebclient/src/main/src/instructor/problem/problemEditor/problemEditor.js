validateFirstRun(document.currentScript);

(function() {
    CourseSketch.problemEditor.waitScreenManager = new WaitScreenManager();
    var advancedEdit = undefined;
    var editPanel = undefined;
    var questionTextPanel = undefined;
    var currentProblem = undefined;
    var solutionMode = false;
    var problemRenderer = undefined;
    var originalMap = undefined;
    var navigator = undefined;
    var submissionPanel = undefined;
    var currentSubmission = undefined;
    var waiter = undefined;
    $(document).ready(function() {
        waiter = new DefaultWaiter(CourseSketch.problemEditor.waitScreenManager,
            document.getElementById('percentBar'));

        editPanel = document.getElementById('editPanel');
        questionTextPanel = document.querySelector('problem-text-panel');
        CourseSketch.dataManager.waitForDatabase(function() {
            advancedEdit = new CourseSketch.AdvanceEditPanel();
            var panel = document.querySelector('navigation-panel');
            navigator = panel.getNavigator();
            var courseProblemId = CourseSketch.dataManager.getState('courseProblemId');
            var bankProblem = CourseSketch.dataManager.getState('bankProblem');
            var problemIndex = CourseSketch.dataManager.getState('partIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);
            submissionPanel = document.getElementById('problemPanel');

            problemRenderer = new CourseSketch.ProblemRenderer(submissionPanel);
            problemRenderer.setStartWaitingFunction(waiter.startWaiting);
            problemRenderer.setFinishWaitingFunction(waiter.finishWaiting);

            if (!isUndefined(bankProblem)) {
                loadBankProblem(bankProblem);
            }

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
            }
            registerObservers();

            if (!isUndefined(courseProblemId)) {
                navigator.setSubgroupNavigation(false);
                navigator.setStayInThisProblem(true);
                navigator.resetNavigationForProblem(courseProblemId, parseInt(problemIndex, 10));
            } else if (addCallback) {
                navigator.refresh();
            }

            document.querySelectorAll('#saveButton')[0].onclick = saveData;
        });
    });

    var mutators = {};
    var actions = {};

    mutators.questionText = function(element) {
        element.oninput = function(e) {
            questionTextPanel.setRapidProblemText(element.value);
        };
    };

    mutators.questionType = function(element) {
        element.onchange = function() {
            problemRenderer.stashData(function() {
                currentProblem.questionType = advancedEdit.getDataFromElement(element, undefined, 'questionType', undefined);
                problemRenderer.renderBankProblem(currentProblem, function() {
                    submissionPanel.setProblemType(questionType);
                    submissionPanel.refreshPanel();
                    console.log(' rendering is finished');
                });
            });
        };
    };

    /**
     * Adds change listeners on a set of elements.
     */
    function registerObservers() {
        var elementList = editPanel.querySelectorAll('[data-mutator]');
        for (var i = 0; i < elementList.length; i++) {
            var element = elementList[i];
            var functionName = element.getAttribute('data-mutator');
            mutators[functionName](element);
        }
        advancedEdit.setActions(actions);
    }

    /**
     * Loads the problem, called every time a user navigates to a different problem.
     *
     * @param {AssignmentNavigator} navigator - The assignment navigator.
     */
    function loadProblem(navigator) {
        var bankProblem = navigator.getCurrentInfo();
        problemRenderer.reset();
        problemRenderer.setStartWaitingFunction(waiter.startWaiting);
        problemRenderer.setFinishWaitingFunction(waiter.finishWaiting);
        currentProblem = bankProblem;
        resetSubmissionPanel(submissionPanel, navigator, bankProblem.questionType);
        loadBankProblem(bankProblem);
    }

    /**
     * Loads a bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem to load.
     */
    function loadBankProblem(bankProblem) {
        questionTextPanel.setRapidProblemText(bankProblem.getQuestionText());
        originalMap = advancedEdit.loadData(bankProblem, editPanel);

        problemRenderer.renderBankProblem(bankProblem, function() {
            submissionPanel.refreshPanel();
            console.log(' rendering is finished');
        });
    }

    /**
     * Saves the data to the database.
     */
    function saveData() {
        originalMap = advancedEdit.getInput(currentProblem, editPanel, originalMap);
        problemRenderer.saveData(currentProblem, function() {
            CourseSketch.dataManager.updateBankProblem(currentProblem, function(argument) {
                console.log(argument);
            }, function(argument) {
                console.log(argument);
            });
        });
    }

    actions.createSolution = function(question, buttonElement, optionalParams) {
        if (solutionMode) {
            loadProblem(navigator);
            return;
        }
        setupSolutionSubmissionPanel(submissionPanel, navigator, currentProblem.questionType);
        problemRenderer.renderSubmission(currentProblem, currentSubmission, function() {
            submissionPanel.refreshPanel();
            console.log('submission');
        });
        // take the question and render it also set up the button and rename it
    };

    /**
     * Sets data in the submission panel.
     *
     * @param {SubmissionPanel} submissionPanel - The element that has submission data.
     * @param {AssignmentNavigator} navigator - The navigator that is performing navigation.
     * @param {QuestionType} questionType - The type of question on this panel.
     */
    function resetSubmissionPanel(submissionPanel, navigator, questionType) {
        submissionPanel.problemIndex = navigator.getCurrentNumber();
        submissionPanel.setProblemType(questionType);
        submissionPanel.setWrapperFunction(undefined);
        submissionPanel.isStudent = false;
    }

    /**
     * Sets data in the submission panel.
     *
     * @param {SubmissionPanel} submissionPanel - The element that has submission data.
     * @param {AssignmentNavigator} navigator - The navigator that is performing navigation.
     * @param {QuestionType} questionType - The type of question on this panel.
     */
    function setupSolutionSubmissionPanel(submissionPanel, navigator, questionType) {
        resetSubmissionPanel(submissionPanel, navigator, questionType);

        submissionPanel.isGrader = false;
        submissionPanel.setWrapperFunction(function(submission) {
            var studentExperiment = CourseSketch.prutil.SrlSolution();
            navigator.setSubmissionInformation(studentExperiment, true);
            console.log('student experiment data set', studentExperiment);
            studentExperiment.submission = submission;
            return studentExperiment;
        });
    }
})();
