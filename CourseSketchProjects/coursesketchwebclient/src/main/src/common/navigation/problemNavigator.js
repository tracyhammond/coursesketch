/**
 * How this works is a polling system.
 *
 * when a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 * @param assignmentId {UUID} the id that the problem is created with.
 * @param loop {Boolean} true if the problems should loop, false otherwise.
 * @param preferredIndex {Number} The starting index to start problems at.
 */
function ProblemNavigator(assignmentId, loop, preferredIndex) {
	var currentAssignmentId = assignmentId;
	var currentAssignment;
	var problemList = [];
	var currentProblem;
	var callbackList = [];
	var currentIndex = 0;
	var navScope = this;
	var eventMappingCallback = {};
	var dataLoaded = false;
	var uiLoaded = false;

	/**
	 * @param index {Number} the problem that we want to switch to.
	 */
	this.goToProblem = function goToProblem(index) {
		changeProblem(index)
	};

	/**
	 * @param index {Number} attempts to change to the next problem.
	 */
	this.gotoNext = function() {
		changeProblem(currentIndex + 1);
	};

    /**
	 * @param index {Number} attempts to change to the previous problem.
	 */
	this.gotoPrevious = function() {
		changeProblem(currentIndex - 1);
	};

	// sets the current index.
	if (!isUndefined(preferredIndex)) {
		currentIndex = preferredIndex;
	}

	/**
	 * changes the problem to the current index.
	 */
	this.refresh = function() {
		changeProblem(currentIndex);
	};

	/**
	 * @return {Boolean} true if the data has been loaded.
	 */
	this.isDataLoaded = function() {
		return dataLoaded;
	};

	/**
	 * @param {Boolean} true if the ui has been loaded.
	 */
	this.setUiLoaded = function(value) {
		uiLoaded = value;
	};

	/**
	 * Returns the information of the current problem.
	 */
	function getProblemInfo() {
		return currentProblem.problemInfo;
	}

	/**
	 * Scopes the index for the callbackList.
	 * this way the browser is not locked up by callbacks.
	 */
	function callBacker(scopedIndex) {
		setTimeout(function() {callbackList[scopedIndex](navScope);},20);
	}

	/**
	 * Changes the problem to the given index.
	 *
	 * @param index {Number} the index we want to switch to.
	 * If looping is set to false then if given an index out of bounds this function returns immediately.
	 * Otherwise the index is set to either 0 or the end of the list depending on how it is out of bounds.
	 * After changing the index all of the set callbacks are called.
	 * Order of the callbacks is not guaranteed.
	 */
	function changeProblem(index) {
		if (index < 0 || index >= problemList.length && !loop) {
			return;
		} else if(loop) {
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
	 */
	this.addCallback = function(callback) {
		callbackList.push(callback);
	};

	/**
	 * Returns true if there is a previous problem that can be navigated to.
	 */
	this.hasPrevious = function() {
		return loop || currentIndex > 0;
	};

	/**
	 * Returns true if there is a next problem that can be navigated to.
	 */
	this.hasNext = function() {
		return loop || currentIndex < problemList.length;
	};

	/**
	 * Returns the current problem number in a human readable format.
	 */
	this.getCurrentNumber = function() {
		return currentIndex + 1;
	};

	/**
	 * Returns the number of problems seen by this problem Navigator.
	 */
	this.getLength = function() {
		return problemList.length;
	};

	/**
	 * Returns the problem text of the current problem.
	 */
	this.getProblemText = function() {
		return getProblemInfo().questionText;
	};

	/**
	 * sets the information about a specific submission.
	 */
	this.setSubmissionInformation = function(submission, isExperiment) {
		if (isExperiment) {
			submission.courseId = currentProblem.courseId;
			submission.assignmentId = currentProblem.assignmentId;
			submission.problemId = currentProblem.id;
		}
	};

	/**
	 * Returns the Id of the current problem.
	 */
	this.getCurrentProblemId = function() {
		return currentProblem.id;
	};

	/**
	 * Returns the type of the base problem.
	 */
	this.getProblemType = function() {
		var type = getProblemInfo().questionType;
		if (type == 1) {
			return "SKETCH";
		}
		if (type == 2) {
			return "MULT_CHOICE";
		}
		if (type == 3) {
			return "FREE_RESP";
		}
		if (type == 4) {
			return "CHECK_BOX";
		}
	};

	/**
	 * Loads all of the problems given an assignment.
	 */
	this.reloadProblems = function() {
		dataLoaded = false;
		dataManager.getAllProblemsFromAssignment(assignmentId, function(problems) {
			for (var i = 0; i <problems.length; i++) {
				problemList.push(problems[i]);
			}
			if (uiLoaded) {
				refresh();
			}
			dataLoaded = true; // this one will take longer so we do this one second.
		});

		dataManager.getAssignment(assignmentId, function(assignment) {
			currentAssignment = assignment;
		});
	};
	this.reloadProblems();

	/**
	 * Add an event mapping for a specific callback.
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
	 */
	this.removeEventMapping = function(key, funct) {
		if (isUndefined(eventMappingCallback[key])) {
			return;
		}
		removeObjectFromArray(eventMappingCallback[key], funct);
	};

	this.clearAllMappings = function(key) {
		eventMappingCallback[key] = undefined;
	};

	/**
	 * Attempts to execute all of the events.
	 *
	 * The failure of one event should not affect the other events.
	 * The order of events firing is not guaranteed.
	 */
	this.executeEvent = function(key, arguments) {
		var list = eventMappingCallback[key];
		if (isUndefined(eventMappingCallback[key])) {
			return;
		}
		for (var i = 0; i <list.length; i++) {
			(function(funct, args) {
				setTimeout(function() {
					funct(args);
				}, 10);
			})(list[i], arguments);
		}
	};

	this.SUBMIT_EVENT = "submit";
	this.COMPLETED_PROBLEM_EVENT = "completion";
}
