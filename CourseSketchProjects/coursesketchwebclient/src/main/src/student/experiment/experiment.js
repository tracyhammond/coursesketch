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

    function loadSketch(navigator) {
        var sketchSurface = document.createElement("sketch-surface");
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        document.getElementById("percentBar").appendChild(element);
        element.startWaiting();

        CourseSketch.dataManager.getSubmission(navigator.getCurrentProblemId(), function(submission) {
            if (isUndefined(submission)) {
                if (element.isRunning()) {
                    element.finishWaiting();
                }
                return;
            }

            var updateList = ProtoUpdateCommandBuilder.SrlUpdateList.decode(submission.getUpdateList());
            //console.log(updateList);
            commandManager.setUpdateList(updateList.getList(), element);
            updateList = null;
            element = null;
            //console.log(submission);
        });
        document.getElementById("problemPanel").appendChild(sketchSurface);
    }
})();
