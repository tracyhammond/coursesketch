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
    var editProblemButton = undefined;

    var mutators = {};
    var actions = {};

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
            editProblemButton = document.querySelectorAll('#editProblem')[0];
            editProblemButton.onclick = actions.createSolution;
        });
    });

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
     * @param {AssignmentNavigator} assignmentNavigator - The assignment navigator.
     */
    function loadProblem(assignmentNavigator) {
        solutionMode = false;
        currentSubmission = undefined;
        var bankProblem = assignmentNavigator.getCurrentInfo();
        problemRenderer.reset();
        problemRenderer.setStartWaitingFunction(waiter.startWaiting);
        problemRenderer.setFinishWaitingFunction(waiter.finishWaiting);
        currentProblem = bankProblem;
        resetSubmissionPanel(submissionPanel, assignmentNavigator, bankProblem.questionType);
        problemRenderer.startWaiting();
        setTimeout(function() {
            loadBankProblem(bankProblem);
        }, 1100);
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
        }, true);
    }

    /**
     * Saves the data to the database.
     */
    function saveData() {
        originalMap = advancedEdit.getInput(currentProblem, editPanel, originalMap);
        if (isUndefined(currentProblem.problemDomain) || currentProblem.currentProblem  === null) {
            currentProblem.problemDomain = CourseSketch.prutil.DomainId();
        }
        currentProblem.problemDomain.questionType = currentProblem.questionType;
        problemRenderer.saveData(currentProblem, function() {
            CourseSketch.dataManager.updateBankProblem(currentProblem, function(argument) {
                console.log(argument);
            }, function(argument) {
                console.log(argument);
            });
        });
    }

    actions.createSolution = function() {
        if (solutionMode) {
            solutionMode = false;
            loadProblem(navigator);
            return;
        }
        problemRenderer.startWaiting();

        // save our data first
        saveData();

        solutionMode = true;
        setupSolutionSubmissionPanel(submissionPanel, navigator, currentProblem.questionType);

        // eslint-disable-next-line
        function loadSubmission() {
            setTimeout(function() {
                problemRenderer.renderSubmission(currentProblem, currentSubmission, function() {
                    submissionPanel.refreshPanel();
                    console.log('submission');
                }, true);
            }, 2100);
        }

        if (isUndefined(currentSubmission) && !isUndefined(currentProblem.solutionId) &&
            currentProblem.solutionId !== null) {
            CourseSketch.dataManager.getSolution([ currentProblem.id, currentProblem.solutionId ], function(solution) {
                currentSubmission = solution.submission;
                loadSubmission();
            });
        } else {
            loadSubmission();
        }

        // take the question and render it also set up the button and rename it
    };

    /**
     * Sets data in the submission panel.
     *
     * @param {SubmissionPanel} subPanel - The element that has submission data.
     * @param {AssignmentNavigator} assignmentNavigator - The navigator that is performing navigation.
     * @param {QuestionType} questionType - The type of question on this panel.
     */
    function resetSubmissionPanel(subPanel, assignmentNavigator, questionType) {
        subPanel.problemIndex = assignmentNavigator.getCurrentNumber();
        subPanel.setProblemType(questionType);
        subPanel.setWrapperFunction(undefined);
        subPanel.isStudent = false;
        $(subPanel).removeClass('studentMode');
        $(editPanel).removeClass('studentMode');
        $(editProblemButton).removeClass('studentMode');
    }

    /**
     * Sets data in the submission panel.
     *
     * @param {SubmissionPanel} subPanel - The element that has submission data.
     * @param {AssignmentNavigator} assignmentNavigator - The navigator that is performing navigation.
     * @param {QuestionType} questionType - The type of question on this panel.
     */
    function setupSolutionSubmissionPanel(subPanel, assignmentNavigator, questionType) {
        resetSubmissionPanel(subPanel, assignmentNavigator, questionType);

        subPanel.isGrader = false;
        $(subPanel).addClass('studentMode');
        $(editPanel).addClass('studentMode');
        $(editProblemButton).addClass('studentMode');
        subPanel.setWrapperFunction(function(submission) {
            var solution = CourseSketch.prutil.SrlSolution();
            assignmentNavigator.setSubmissionInformation(solution, false);
            console.log('student experiment data set', solution);
            solution.submission = submission;
            solution.questionType = currentProblem.questionType;
            solution.problemDomain = currentProblem.problemDomain;
            return solution;
        });
    }
})();
