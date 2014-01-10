/**
 * How this works is a polling system.
 *
 * when a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 */
function schoolNavigator(assignmentId, dataManagerR, loop) {
	var currentAssignmentId = assignmentId;
	var currentAssignment;
	var dataManager = dataManagerR;
	var problemList = [];
	var currentProblem;
	var callbackList = [];
	var currentIndex = 0;
	var navScope = this;
	var eventMappingCallback = {};
	this.goToProblem = function goToProblem(index) {
		changeProblem(index)
	}

	this.gotoNext = function() {
		changeProblem(currentIndex + 1);
	}

	this.gotoPrevious = function() {
		changeProblem(currentIndex - 1);
	}

	this.refresh = function() {
		changeProblem(currentIndex);
	}

	function getProblemInfo() {
		return currentProblem.problemInfo;
	}
	/**
	 * Scopes the index for the callbackList.
	 */
	function callBacker(scopedIndex) {
		setTimeout(function() {callbackList[scopedIndex](navScope);},20);
	}

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

	this.addCallback = function(callback) {
		callbackList.push(callback);
	}

	/**
	 * Returns true if there is a previous problem that can be navigated to.
	 */
	this.hasPrevious = function() {
		return loop || currentIndex > 0;
	}

	/**
	 * Returns true if there is a next problem that can be navigated to.
	 */
	this.hasNext = function() {
		return loop || currentIndex < problemList.length;
	}

	/**
	 * Returns the current problem number in a human readable format
	 */
	this.getCurrentNumber = function() {
		return currentIndex + 1;
	}

	/**
	 * Returns the current problem number in a human readable format
	 */
	this.getLength = function() {
		return problemList.length;
	}

	/**
	 * Returns the problem text of the current problem.
	 */
	this.getProblemText = function() {
		return getProblemInfo().questionText;
	}

	this.setSubmissionInformation = function(submission, isExperiment) {
		if (isExperiment) {
			console.log()
			submission.courseId = currentProblem.courseId;
			submission.assignmentId = currentProblem.assignmentId;
			submission.problemId = currentProblem.id;
			submission.submission.id = getProblemInfo().id;
		}
	}
	/**
	 * Returns the type of the base problem.
	 *
	 * This should be the first subProblem all subsequent subproblems are handled in different ways.
	 */
	this.getPoblemType = function() {
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
	}

	this.reloadProblems = function() {
		dataManager.getAllProblemsFromAssignment(assignmentId, function(problems) {
			for(var i = 0; i <problems.length; i++) {
				problemList.push(problems[i]);
			}
		});
		dataManager.getAssignment(assignmentId, function(assignment) {
			currentAssignment = assignment;
		});
	}
	this.reloadProblems();

	this.addEventMapping = function(key, funct) {
		if (isUndefined(eventMappingCallback[key])) {
			var list = [];
			list.push(funct);
			eventMappingCallback[key] = list;
		} else {
			eventMappingCallback[key].push(funct);
		}
	}

	this.removeEventMapping = function(key, funct) {
		if (isUndefined(eventMappingCallback[key])) {
			return;
		}
		removeObjectFromArray(eventMappingCallback[key], funct);
	}

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
	}

	this.SUBMIT_EVENT = "submit";
	this.COMPLETED_PROBLEM_EVENT = "completion";
}