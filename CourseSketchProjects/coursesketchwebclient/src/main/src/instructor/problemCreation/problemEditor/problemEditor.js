validateFirstRun(document.currentScript);

(function() {
    CourseSketch.problemEditor.waitScreenManager = new WaitScreenManager();
    var advancedEdit = undefined;
    var editPanel = undefined;
    var questionTextPanel = undefined;
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
        });
    });

    var mutators = {};
    var actions = {};

    mutators.questionText = function(element) {
        element.oninput = function(e) {
            questionTextPanel.setProblemText(element.value);
        }
    };

    mutators.questionType = function(element) {
        console.log(element);
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
        loadBankProblem(bankProblem);
    }

    function loadBankProblem(bankProblem) {
        var problemType = bankProblem.getQuestionType();

        questionTextPanel.setProblemText(bankProblem.getQuestionText());
        console.log('a problem has been loaded with question text', bankProblem.getQuestionText());
        advancedEdit.loadData(bankProblem, editPanel)
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {AssignmentNavigator} navigator - The assignment navigator.
     */
    function loadSketch(bankProblem) {
        var sketchSurface = document.createElement('sketch-surface');
        sketchSurface.className = 'sub-panel submittable';
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = '100%';
        sketchSurface.setErrorListener(function(exception) {
            console.log(exception);
            alert(exception);
        });
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.problemEditor.addWaitOverlay();
        document.getElementById('percentBar').appendChild(element);
        element.startWaiting();
        var realWaiting = element.finishWaiting.bind(element);

        /**
         * Called when the sketch surface is done loading to remove the overlay.
         */
        element.finishWaiting = function() {
            realWaiting();
            sketchSurface.refreshSketch();
            CourseSketch.studentExperiment.removeWaitOverlay();
            sketchSurface = undefined;
            element = undefined;
        };

        if (isUndefined(bankProblem.specialQuestionData) || isUndefined(bankProblem.specialQuestionData.sketchArea)) {
            document.getElementById('problemPanel').appendChild(sketchSurface);
            return;
        }

        var sketchArea = bankProblem.specialQuestionData.sketchArea);

        if (isUndefined(bankProblem.specialQuestionData)) {
            document.getElementById('problemPanel').appendChild(sketchSurface);
            return;
        }

        // tell the surface not to create its own sketch.
        sketchSurface.dataset.existinglist = '';

        // add after attributes are set.

        sketchSurface.refreshSketch();

        // adding here because of issues
        document.getElementById('problemPanel').appendChild(sketchSurface);
    }
})();
