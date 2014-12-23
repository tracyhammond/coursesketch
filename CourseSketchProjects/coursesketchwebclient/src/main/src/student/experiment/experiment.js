(function() {
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var navigator = document.querySelector("navigation-panel").getNavigator();
            var assignment = CourseSketch.dataManager.getState("currentAssignment");
            if (!isUndefined(assignment)) {
                navigator.setAssignmentId(assignment);
            }
            var problem = CourseSketch.dataManager.getState("currentProblemIndex");
            if (!isUndefined(problem)) {
                navigator.setPreferredIndex(problem);
            }
            CourseSketch.dataManager.clearStates();
            navigator.reloadProblems();
        });
    });

})();
