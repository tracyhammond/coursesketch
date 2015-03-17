/**
 * How this works is a polling system.
 *
 * when a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 * @param {UUID} assignmentId the id that the problem is created with.
 * @param {Boolean} loop true if the problems should loop, false otherwise.
 * @param  {Number}preferredIndex The starting index to start problems at.
 * @class ProblemNavigator
 */
function ProblemNavigator(assignmentId, loop, preferredIndex) {
    var currentAssignmentId = assignmentId;
    var currentAssignment;
    var problemList = [];
    var currentProblem;
    var callbackList = [];
    var currentIndex = 0;
    var localScope = this;
    var eventMappingCallback = {};
    var dataLoaded = false;
    var uiLoaded = false;

    /**
     * @param {Number} index {Number} the problem that we want to switch to.
     * @instance
     * @memberof ProblemNavigator
     */
    this.goToProblem = function goToProblem(index) {
        changeProblem(index);
    };

    /**
     * Attempts to change to the next problem.
     * @instance
     * @memberof ProblemNavigator
     */
    this.gotoNext = function() {
        changeProblem(currentIndex + 1);
    };

    /**
     * Attempts to change to the previous problem.
     * @instance
     * @memberof ProblemNavigator
     */
    this.gotoPrevious = function() {
        changeProblem(currentIndex - 1);
    };

    // Sets the current index.
    if (!isUndefined(preferredIndex)) {
        try {
            currentIndex = parseInt(preferredIndex, 10);
        } catch (exception) {
            console.error('could not parse preferredIndex using 0 instead');
        }
    }

    /**
     * Changes the problem to the current index.
     * @instance
     * @memberof ProblemNavigator
     */
    this.refresh = function() {
        changeProblem(currentIndex);
    };

    /**
     * @return {Boolean} true if the data has been loaded.
     * @instance
     * @memberof ProblemNavigator
     */
    this.isDataLoaded = function() {
        return dataLoaded;
    };

    /**
     * @param {Boolean} value true if the ui has been loaded.
     * @instance
     * @memberof ProblemNavigator
     */
    this.setUiLoaded = function(value) {
        uiLoaded = value;
    };

    /**
     * @returns {SrlProblemBank} the information of the current problem.
     * @memberof ProblemNavigator
     */
    function getProblemInfo() {
        return currentProblem.problemInfo;
    }

    /**
     * Scopes the index for the callbackList.
     * this way the browser is not locked up by callbacks.
     * @instance
     * @access private
     * @memberof ProblemNavigator
     */
    function callBacker(scopedIndex) {
        setTimeout(function() {
            callbackList[scopedIndex](localScope);
        }, 20);
    }

    /**
     * Changes the problem to the given index.
     *
     * @param {Number} index the index we want to switch to.
     * If looping is set to false then if given an index out of bounds this function returns immediately.
     * Otherwise the index is set to either 0 or the end of the list depending on how it is out of bounds.
     * After changing the index all of the set callbacks are called.
     * Order of the callbacks is not guaranteed.
     * @instance
     * @access private
     * @memberof ProblemNavigator
     */
    function changeProblem(index) {
        if (index < 0 || index >= problemList.length && !loop) {
            return;
        } else if (loop) {
            if (index < 0) {
                index = problemList.length - 1;
            }
            if (index >= problemList.length) {
                index = 0;
            }
        }
        if ((index >= 0 && index < problemList.length)) {
            currentIndex = index;
            currentProblem = problemList[index];
            for (var i = 0; i < callbackList.length; i++) {
                callBacker(i);
            }
        }
    }

    /**
     * adds a callback that is called when changing problem index.
     * @instance
     * @memberof ProblemNavigator
     */
    this.addCallback = function(callback) {
        callbackList.push(callback);
    };

    /**
     * @returns {Boolean} true if there is a previous problem that can be navigated to.
     * @instance
     * @memberof ProblemNavigator
     */
    this.hasPrevious = function() {
        return loop || currentIndex > 0;
    };

    /**
     * @returns {Boolean} true if there is a next problem that can be navigated to.
     * @instance
     * @memberof ProblemNavigator
     */
    this.hasNext = function() {
        return loop || currentIndex < problemList.length;
    };

    /**
     * @returns {Number} the current problem number in a human readable format.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getCurrentNumber = function() {
        return currentIndex + 1;
    };

    /**
     * @returns {Number} the number of problems seen by this problem Navigator.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getLength = function() {
        return problemList.length;
    };

    /**
     * @returns {String} the problem text of the current problem.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getProblemText = function() {
        return getProblemInfo().questionText;
    };

    /**
     * sets the information about a specific submission.
     * @param {SrlExperiment | SrlSolution} submissionWrapper this is either an experiment or solution this is NOT a submission object.
     * @instance
     * @memberof ProblemNavigator
     */
    this.setSubmissionInformation = function(submissionWrapper, isExperiment) {
        if (isExperiment) {
            submissionWrapper.courseId = currentProblem.courseId;
            submissionWrapper.assignmentId = currentProblem.assignmentId;
            submissionWrapper.problemId = currentProblem.id;
        } else {
            submissionWrapper.problemBankId = getProblemInfo().id;
        }
    };

    /**
     * @returns {String} the Id of the current problem.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getCurrentProblemId = function() {
        return currentProblem.id;
    };

    /**
     * @return {String} the type of the base problem.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getProblemType = function() {
        var type = getProblemInfo().questionType;
        return type;
    };

    /**
     * Sets the new Id for the assignment, this does not refresh the navigator.
     * That can be done by calling {@link #reloadProblems}.
     * @param {String} currentAssignmentId The new assignmentid.
     * @instance
     * @memberof ProblemNavigator
     */
    this.setAssignmentId = function(currentAssignmentId) {
        assignmentId = currentAssignmentId;
    };

    /**
     * @param {Number} selectedIndex sets the preferred index to start the problem at.
     * This does not change what the current index is.
     * @instance
     * @memberof ProblemNavigator
     */
    this.setPreferredIndex = function(selectedIndex) {
        preferredIndex = selectedIndex;
    };

    /**
     * Reloads the assignment from the id and assigns it to the currentAssignment.
     * @instance
     * @memberof ProblemNavigator
     */
    this.reloadAssignment = function() {
        CourseSketch.dataManager.getAssignment(assignmentId, function(assignment) {
            currentAssignment = assignment;
        });
    };

    /**
     * @return {SrlAssignment} the current assignment.
     * @instance
     * @memberof ProblemNavigator
     */
    this.getCurrentAssignment = function() {
        return currentAssignment;
    };

    /**
     * Loads all of the problems given an assignment.
     * @instance
     * @memberof ProblemNavigator
     */
    this.reloadProblems = function() {
        currentIndex = preferredIndex;
        dataLoaded = false;
        var refresh = this.refresh;
        if (!isUndefined(assignmentId)) {
            CourseSketch.dataManager.getAllProblemsFromAssignment(assignmentId, function(problems) {
                problemList = [];
                for (var i = 0; i < problems.length; i++) {
                    problemList.push(problems[i]);
                }
                if (uiLoaded) {
                    refresh();
                }
                dataLoaded = true; // this one will take longer so we do this one second.
            });

            CourseSketch.dataManager.getAssignment(assignmentId, function(assignment) {
                currentAssignment = assignment;
            });
        }
    };
    this.reloadProblems();

    /**
     * Add an event mapping for a specific callback.
     * @instance
     * @memberof ProblemNavigator
     */
    this.addEventMapping = function(key, funct) {
        if (isUndefined(eventMappingCallback[key])) {
            var list = [];
            list.push(funct);
            eventMappingCallback[key] = list;
        } else {
            eventMappingCallback[key].push(funct);
        }
    };

    /**
     * Attempts to remove the event from the current event map.
     * @instance
     * @memberof ProblemNavigator
     */
    this.removeEventMapping = function(key, funct) {
        if (isUndefined(eventMappingCallback[key])) {
            return;
        }
        removeObjectFromArray(eventMappingCallback[key], funct);
    };

    /**
     * clears all mappings.
     * @instance
     * @memberof ProblemNavigator
     */
    this.clearAllMappings = function(key) {
        eventMappingCallback[key] = undefined;
    };

    /**
     * Attempts to execute all of the events.
     *
     * The failure of one event should not affect the other events.
     * The order of events firing is not guaranteed.
     * @instance
     * @memberof ProblemNavigator
     */
    this.executeEvent = function(key, funcArgs) {
        var list = eventMappingCallback[key];
        if (isUndefined(eventMappingCallback[key])) {
            return;
        }
        for (var i = 0; i < list.length; i++) {
            (function(funct, args) {
                setTimeout(function() {
                    funct(args);
                }, 10);
            })(list[i], funcArgs);
        }
    };

    this.SUBMIT_EVENT = 'submit';
    this.COMPLETED_PROBLEM_EVENT = 'completion';
}
