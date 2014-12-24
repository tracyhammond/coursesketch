(function() {
    CourseSketch.studentExperiment.waitScreenManager = new WaitScreenManager();
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
                navigator.reloadProblems();
            }
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

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    CourseSketch.studentExperiment.addWaitOverlay = function() {
        CourseSketch.studentExperiment.waitScreenManager.buildOverlay(document.querySelector("body"));
        CourseSketch.studentExperiment.waitScreenManager.buildWaitIcon(document.getElementById("overlay"));
        document.getElementById("overlay").querySelector(".waitingIcon").classList.add("centered");
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    CourseSketch.studentExperiment.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById("overlay")) && document.getElementById("overlay") != null) {
            document.querySelector("body").removeChild(document.getElementById("overlay"));
        }
    };

    function loadSketch(navigator) {
        sketchSurface = document.createElement("sketch-surface");
        sketchSurface.style.width="100%";
        sketchSurface.style.height="calc(100% - 100px)";
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.studentExperiment.addWaitOverlay();
        document.getElementById("percentBar").appendChild(element);
        element.startWaiting();

        CourseSketch.dataManager.getSubmission(navigator.getCurrentProblemId(), function(submission) {
            if (isUndefined(submission)) {
                if (element.isRunning()) {
                    element.finishWaiting();
                    CourseSketch.studentExperiment.removeWaitOverlay();
                    document.getElementById("problemPanel").appendChild(sketchSurface);
                }
                return;
            }

            // add sketch surface at last possible moment!
            document.getElementById("problemPanel").appendChild(sketchSurface);
            console.log(submission);
            var updateList = submission.getUpdateList();
            //console.log(updateList);
            sketchSurface.loadUpdateList(updateList.getList(), element);
            updateList = null;
            element = null;
            //console.log(submission);
            CourseSketch.studentExperiment.removeWaitOverlay();
        });
    }
})();
