(function() {
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector("navigation-panel");
            var navigator = panel.getNavigator();
            var assignment = CourseSketch.dataManager.getState("currentAssignment");
            if (!isUndefined(assignment)) {
                navigator.setAssignmentId(assignment);
            }
            var problem = CourseSketch.dataManager.getState("currentProblemIndex");
            if (!isUndefined(problem)) {
                navigator.setPreferredIndex(problem);
            }
            CourseSketch.dataManager.clearStates();

            if (isUndefined(panel.dataset.callbackset)) {
                panel.dataset.callbackset = ""
                navigator.addCallback(loadProblem);
            }

            navigator.reloadProblems();
        });
    });

    function loadProblem(navigator) {
        var problemType = navigator.getProblemType();
        if (problemType === CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.SKETCH) {
            console.log("Loading sketch problem");
            loadSketch(navigator);
        } else if (problemType === CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.FREE_RESP) {
            console.log("Loading typing problem");
            loadSketch(navigator);
        }

    }

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
