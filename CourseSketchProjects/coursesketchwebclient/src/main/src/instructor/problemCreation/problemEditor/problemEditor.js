validateFirstRun(document.currentScript);

(function() {
    CourseSketch.problemEditor.waitScreenManager = new WaitScreenManager();
    var advancedEdit = undefined;
    var editPanel = undefined;
    var questionTextPanel = undefined;
    var currentProblem = undefined;
    var problemRenderer = undefined;
    var originalMap = undefined;
    $(document).ready(function() {
        editPanel = document.getElementById('editPanel');
        questionTextPanel = document.querySelector('problem-text-panel');
        CourseSketch.dataManager.waitForDatabase(function() {
            advancedEdit = new CourseSketch.AdvanceEditPanel();
            var panel = document.querySelector('navigation-panel');
            var navigator = panel.getNavigator();
            var courseProblemId = CourseSketch.dataManager.getState('courseProblemId');
            var bankProblem = CourseSketch.dataManager.getState('bankProblem');
            var problemIndex = CourseSketch.dataManager.getState('partIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

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

            problemRenderer = new CourseSketch.ProblemRenderer(document.getElementById('problemPanel'));

            document.querySelectorAll('#saveButton')[0].onclick = saveData;
        });
    });

    var mutators = {};
    var actions = {};

    mutators.questionText = function(element) {
        element.oninput = function(e) {
            questionTextPanel.setRapidProblemText(element.value);
        }
    };

    mutators.questionType = function(element) {
        element.onchange = function() {
            problemRenderer.stashData(function() {
                currentProblem.questionType = advancedEdit.getDataFromElement(element, undefined, 'questionType', undefined);
                loadSpecificType(currentProblem);
            });
        };
    };

    actions.createSolution = function(question, buttonElement, optionalParams) {
        console.log(arguments);
    };

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
        currentProblem = bankProblem;
        loadBankProblem(bankProblem);
    }

    function loadBankProblem(bankProblem) {
        var problemType = bankProblem.getQuestionType();

        questionTextPanel.setProblemText(bankProblem.getQuestionText());
        console.log('a problem has been loaded with question text', bankProblem.getQuestionText());
        originalMap = advancedEdit.loadData(bankProblem, editPanel);

        loadSpecificType(bankProblem);
    }

    function loadSpecificType(bankProblem) {
        problemRenderer.renderBankProblem(bankProblem, function() {
            console.log(' rendering is finished');
        });
    }

    function saveData() {
        originalMap = advancedEdit.getInput(currentProblem, editPanel, originalMap);
        problemRenderer.saveData(currentProblem, function() {
           CourseSketch.dataManager.updateBankProblem(currentProblem, function(argument) {
               console.log(argument);
           }, function(argument) {
               console.log(argument);
           }) ;
        });
    }
})();
