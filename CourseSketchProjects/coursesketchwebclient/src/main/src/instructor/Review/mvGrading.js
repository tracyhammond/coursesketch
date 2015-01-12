/*
 * this Multiview page goes off a single problem at a time and laods all student experiments of that
 * problem id.
 */
(function() {
    /*
     * a list of experiments to laod into the sketching pannels
     * gets all experiments that hold the current problem id and places them is
     * sketchList
     */
    function getSketches(callback) {
        CourseSketch.dataManager.getAllExperiments(getNav().getCurrentProblemId(), function(sketchList) {
             if (isUndefined(sketchList)) {
                if (element.isRunning()) {
                    element.finishWaiting();
                }
                return;
            }
            if (!isUndefined(callback)) {
                callback(sketchList);
            }
        });
    }

    /*
     * used to get list of experiments and then calls createMvSketch to create
     * all sketches on to the grade screen.
     */
    function createMvList() {
        getSketches(createMvSketch);
    }

    /*
     * creates a multiview sketch panel and attaches it to the grading area
     * this can be done dynamically
     */
    function createMvSketch(array) {
        for (var i = 0; i < array.length; i++) {
            var mvSketch = document.createElement('mv-sketch');
            document.querySelector(".sketches").appendChild(mvSketch);
            mvSketch.setUpdateList(getUpdateList(array, i).getList());
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
     * wipes the previous sketches, and laods the previous problem into sketchList
     * and then places them into the Multiview screen.
     */
    function previousProblem() {
        multiviewSketchDelete();
        createMvList();
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
                navigator.addCallback(function() {
                    multiviewSketchDelete();
                    createMvList();
                });
                navigator.reloadProblems();
            }
        });
    });
})();
