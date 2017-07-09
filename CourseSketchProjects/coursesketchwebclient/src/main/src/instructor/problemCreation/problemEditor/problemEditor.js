validateFirstRun(document.currentScript);

(function() {
    CourseSketch.problemEditor.waitScreenManager = new WaitScreenManager();
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var bankProblem = CourseSketch.dataManager.getState('currentAssignment');
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
            }

            if (!isUndefined(assignmentId)) {
                navigator.resetNavigation(assignmentId, parseInt(problemIndex, 10));
            } else if (addCallback) {
                navigator.refresh();
            }
        });
    });
})();