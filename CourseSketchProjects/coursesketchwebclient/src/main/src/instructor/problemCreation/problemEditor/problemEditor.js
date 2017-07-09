validateFirstRun(document.currentScript);

(function() {
    CourseSketch.problemEditor.waitScreenManager = new WaitScreenManager();
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector('navigation-panel');
            var navigator = panel.getNavigator();
            var courseProblemId = CourseSketch.dataManager.getState('courseProblemId');
            var bankProblem = CourseSketch.dataManager.getState('bankProblem');
            var problemIndex = CourseSketch.dataManager.getState('partIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

            var questionText = document.querySelector('problem-text-panel');
            if (!isUndefined(bankProblem)) {
                questionText.setProblemText(bankProblem.getQuestionText());
            }

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
            }

            if (!isUndefined(courseProblemId)) {
                navigator.setSubgroupNavigation(false);
                navigator.setStayInThisProblem(true);
                navigator.resetNavigationForProblem(courseProblemId, parseInt(problemIndex, 10));
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
        var problemType = bankProblem.getQuestionType();

        var questionText = document.querySelector('problem-text-panel');
        questionText.setProblemText(bankProblem.getQuestionText());
        console.log('a problem has been loaded with question text', bankProblem.getQuestionText());
    }
})();
