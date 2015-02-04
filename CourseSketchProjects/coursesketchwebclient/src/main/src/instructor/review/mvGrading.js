/*
 * this Multiview page goes off a single problem at a time and laods all student experiments of that
 * problem id.
 */
(function() {
    CourseSketch.multiViewPage.waitScreenManager = new WaitScreenManager();
    /*
     * a list of experiments to laod into the sketching pannels
     * gets all experiments that hold the current problem id and places them is
     * sketchList
     */
    function getSketches(callback, navigator) {
        CourseSketch.dataManager.getAllExperiments(getNav().getCurrentProblemId(), function(sketchList) {
             if (isUndefined(sketchList)) {
                if (element.isRunning()) {
                    element.finishWaiting();
                }
                return;
            }
            if (!isUndefined(callback)) {
                callback(sketchList, navigator);
            }
        });
    }

    /*
     * used to get list of experiments and then calls createMvSketch to create
     * all sketches on to the grade screen.
     */
    function createMvList(navigator) {
        getSketches(createMvSketch, navigator);
    }

    /*
     * creates a multiview sketch panel and attaches it to the grading area
     * this can be done dynamically
     */
    function createMvSketch(array, navigator) {
        for (var i = 0; i < array.length; i++) {
            var mvSketch = document.createElement('mv-sketch');
            document.querySelector(".sketches").appendChild(mvSketch);
            mvSketch.setUpdateList(getUpdateList(array, i).getList());
            mvSketch.setSketchClickedFunction(function() {
                console.log(navigator);
                CourseSketch.multiViewPage.loadProblem(navigator, this.getUpdateList());
            });
        }
    }

    /*
     * gets a specific set of sketch data to be used in the multiview sketch panel
     *
     *@param array
     *       {array<experiments>}
     *@param index
     *         {int}
     */
    function getUpdateList(array, index) {
        return array[index].getSubmission().getUpdateList();
    }

    /*
     * returns the navigation panel element to be used by other pages.
     */
    function getNav() {
        return document.querySelector("navigation-panel").getNavigator();
    };

    /*
     * deletes the sketch data in the sketch-area element
     */
    function multiviewSketchDelete() {
        var parent = document.getElementById("sketch-area");
        parent.innerHTML= '';
    }

    $(document).ready(function() {
        document.getElementById("dialogPanel").querySelector('button').onclick = function() {
            document.getElementById("dialogPanel").close();
        };
        CourseSketch.dataManager.waitForDatabase(function() {
            var navPanel = document.querySelector("navigation-panel")
            var navigator = getNav();
            var assignment = CourseSketch.dataManager.getState("currentAssignment");
            if (!isUndefined(assignment)) {
                navigator.setAssignmentId(assignment);
            }
            var problemIndex = CourseSketch.dataManager.getState("currentProblemIndex");
            if (!isUndefined(problemIndex)) {
                navigator.setPreferredIndex(parseInt(problemIndex));
            }
            CourseSketch.dataManager.clearStates();
            if (isUndefined(navPanel.dataset.callbackset)) {
                navPanel.dataset.callbackset = ""
                navigator.addCallback(function(navigator) {
                    multiviewSketchDelete();
                    createMvList(navigator);
                });
                navigator.reloadProblems();
            }
        });
    });

    /**
     * loads the problem, called every time a user navigates to a different problem.
     */
    CourseSketch.multiViewPage.loadProblem = function(navigator, submissionData) {
        document.getElementById("dialogPanel").show();
        var problemType = navigator.getProblemType();
        // todo: better way of removing elements
        var parentPanel = document.getElementById("problemPanel");
        console.log(parentPanel);
        var oldElement = parentPanel.querySelector(".sub-panel");
        if (oldElement instanceof Node) {
            parentPanel.removeChild(oldElement);
        }
        if (problemType === CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.SKETCH) {
            console.log("Loading sketch problem");
            CourseSketch.multiViewPage.loadSketch(submissionData);
        } else if (problemType === CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.FREE_RESP) {
            console.log("Loading typing problem");
            loadTyping(submissionData);
        }

        parentPanel.problemIndex = navigator.getCurrentNumber();
        parentPanel.setProblemType(problemType);
        parentPanel.refreshPanel();
        parentPanel.isStudent = false;
        parentPanel.isGrader = true;

        // THIS WILL BE DONE A TINY BIT LATER
        parentPanel.setWrapperFunction(function(submission) {
            var studentExperiment = CourseSketch.PROTOBUF_UTIL.SrlExperiment();
            navigator.setSubmissionInformation(studentExperiment, true);
            console.log("student experiment data set", studentExperiment);
            studentExperiment.submission = submission;
            return studentExperiment;
        });
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     */
    CourseSketch.multiViewPage.loadSketch = function(updateList) {
        var sketchSurface = document.createElement("sketch-surface");
        sketchSurface.className = "wide_rule sub-panel";
        sketchSurface.style.width="100%";
        sketchSurface.style.height="calc(100%)";
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.multiViewPage.addWaitOverlay();
        document.getElementById("percentBar").appendChild(element);
        element.startWaiting();
        var realWaiting = element.finishWaiting.bind(element);
        element.finishWaiting = function() {
            realWaiting();
            sketchSurface.refreshSketch();
            CourseSketch.multiViewPage.removeWaitOverlay();
            sketchSurface = undefined;
            element = undefined;
        };
        document.getElementById("problemPanel").appendChild(sketchSurface);
        // tell the surface not to create its own sketch.
        sketchSurface.dataset.existinglist = "";

        sketchSurface.refreshSketch();
        //console.log(updateList);
        sketchSurface.loadUpdateList(updateList, element);
        updateList = null;
        element = null;
        //console.log(submission);
    }

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    CourseSketch.multiViewPage.addWaitOverlay = function() {
        CourseSketch.multiViewPage.waitScreenManager.buildOverlay(document.querySelector("body"));
        CourseSketch.multiViewPage.waitScreenManager.buildWaitIcon(document.getElementById("overlay"));
        document.getElementById("overlay").querySelector(".waitingIcon").classList.add("centered");
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    CourseSketch.multiViewPage.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById("overlay")) && document.getElementById("overlay") != null) {
            document.querySelector("body").removeChild(document.getElementById("overlay"));
        }
    };
})();
