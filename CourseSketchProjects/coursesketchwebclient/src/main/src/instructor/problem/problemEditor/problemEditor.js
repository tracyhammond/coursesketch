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

            problemRenderer = new CourseSketch.ProblemRenderer(document.getElementById('problemPanel'));

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
                loadSpecificType(currentProblem);
            });
        };
    };

    actions.createSolution = function(question, buttonElement, optionalParams) {
        console.log(arguments);
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
        currentProblem = bankProblem;
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
            }) ;
        });
    }
})();
