/**
 * How this works is a polling system.
 *
 * when a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 */
function schoolNavigator(assignmentId, loop) {
	var problemList = [];
	problemList.push("hi");
	problemList.push("ho");
	var currentProblem;
	var callbackList = [];
	var currentIndex = 0;
	var navScope = this;
	this.goToProblem = function goToProblem(index) {
		changeProblem(index)
	}

	this.gotoNext = function() {
		changeProblem(currentIndex + 1);
	}

	this.gotoPrevious = function() {
		changeProblem(currentIndex - 1);
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
}