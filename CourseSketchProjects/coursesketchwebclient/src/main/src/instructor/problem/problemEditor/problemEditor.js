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
            problemRenderer.setStartWaitingFunction(startWaiting);
            problemRenderer.setFinishWaitingFunction(finishWaiting);

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
                    console.log(' rendering is finished');
                });
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
        problemRenderer.setStartWaitingFunction(startWaiting);
        problemRenderer.setFinishWaitingFunction(finishWaiting);
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

    /**
     * Starts a waiting screen.
     */
    function startWaiting() {
        document.getElementById('percentBar').innerHTML = '';
        waitingElement = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.problemEditor.addWaitOverlay();
        document.getElementById('percentBar').appendChild(waitingElement);
        waitingElement.startWaiting();
        var realWaiting = waitingElement.finishWaiting.bind(waitingElement);

        /**
         * Called when the sketch surface is done loading to remove the overlay.
         */
        waitingElement.finishWaiting = function() {
            realWaiting();
            CourseSketch.problemEditor.removeWaitOverlay();
        };
    }

    /**
     * Ends a waiting screen.
     */
    function finishWaiting() {
        if (!isUndefined(waitingElement) && waitingElement.isRunning()) {
            waitingElement.finishWaiting();
            CourseSketch.problemEditor.removeWaitOverlay();
        }
    }

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    CourseSketch.problemEditor.addWaitOverlay = function() {
        CourseSketch.problemEditor.waitScreenManager.buildOverlay(document.querySelector('body'));
        CourseSketch.problemEditor.waitScreenManager.buildWaitIcon(document.getElementById('overlay'));
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    CourseSketch.problemEditor.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById('overlay')) && document.getElementById('overlay') !== null) {
            document.querySelector('body').removeChild(document.getElementById('overlay'));
        }
    };
})();
