/**
 * How this works is a polling system.
 *
 * When a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 *
 * How navigation works.
 *
 * If you are navigating at subgroup part level:
 * <ul>
 *     <li>Goto next:<ul>
 *         <li>If there is a next part navigate to it</li>
 *         <li>If there is not a next part then gotoNext at subgroup level</li></ul></li>
 *     <li>Goto previous:
 *         If there is a previous part then navigate to it
 *         If there is not a previous part then gotoPrevious at subgroup level</li></ul></li>
 * <li>If you are navigating at subgroup level:<ul>
 *      <li>Goto next:<ul>
 *         <li>If there is a next subgroup navigate to it
 *         <li>If there is not a next subgroup:<ul>
 *              <li>If the stack is empty you reached the end!
 *              <li>If the stack is not empty you pop up and navigate to the index specified on the list</li></ul></li><
 *      <li>Goto previous:
 *         <li>If there is a previous subgroup navigate to it
 *         <li>If there is not a previos usgroup:
 *              <li>If the visisted stack is empty you reached the beginning!
 *              <li>If the visited stack is not empty you go in an and start at the end of that subgroup.
 *
 * A subgroup part can be an assignment which would be a nested assignment.
 *
 * @param {UUID} startingAssignmentId - The id that the problem is created with.
 * @param {Number} preferredIndex - The starting index to start problems at.
 * @param {Boolean} navigateAtSubgroupLevel - If True then the navigation happens at the subgroup level and not the subgroupPart level.
 *                      Not setting this gives it a value of false.
 * @class AssignmentNavigator
 */
function AssignmentNavigator(startingAssignmentId, preferredIndex, navigateAtSubgroupLevel) {

    /**
     * Controls weather navigation happens at the subgroup level (SrlProblem) or the subgroup part level (SrlBankProblem, SrlSlide).
     * @type {Boolean}
     */
    var isSubgroupNavigation = navigateAtSubgroupLevel;

    /**
     * The current id of the assignment that is being navigated.
     * @type {UUID}
     */
    var currentAssignmentId = startingAssignmentId;

    /**
     * The current assignment object that is being navigated.
     * @type {SrlAssignment}
     */
    var currentAssignment;

    /**
     * The set of subgroups that belong to the assignment.
     *
     * These are referenced separately for ease of use.
     * @type {Array}
     */
    var subgroupList = [];

    /**
     * Current slide group or problem.
     * @type {SrlProblem}
     */
    var currentSubgroup;

    /**
     * Current slide or problem
     * @type {SrlBankProblem | LectureSlide}
     */
    var currentSubgroupPart;

    /**
     * Holder for current subgroup part
     * @type {ProblemSlideHolder}
     */
    var currentSubgroupPartHolder = undefined;

    /**
     * A list of callbacks that are called when the navigation changes problem part / problem or slide.
     * @type {Array}
     */
    var callbackList = [];

    /**
     * The current index In the assignment where the navigator is.
     * @type {Number}
     */
    var currentIndex = 0;

    /**
     * The current index in the problem or slide set where the navigator is.
     * @type {Number}
     */
    var currentSubgroupPartIndex = 0;

    /**
     * Used for scoping.
     * @type {AssignmentNavigator}
     */
    var localScope = this;

    /**
     * A map of events that are used for the navigator.
     *
     * These can be specific to a certain problem or to multiple problems.
     * @type {Map<Event, Function>}
     */
    var eventMappingCallback = {};

    /**
     * True once the data assignment has been loaded.
     * @type {Boolean}
     */
    var dataLoaded = false;

    /**
     * True if the ui that uses the information for the problem or slide has been loaded.
     * @type {Boolean}
     */
    var uiLoaded = false;

    /**
     * The stack of assignments you are currently in.
     *
     * Every level
     *
     * This is processed like a stack.
     * @type {Array<String>}
     */
    var assignmentIdStack = [];

    /**
     * The stack of indexes that the assignments navigate to once an nested assignment has been completed.
     *
     * Each item contains an AssignmentLocation which is the following format:
     * {
     *      part: Number,
     *      group: Number
     * }
     * @type {Array<{group: Number, part: Number}>}
     */
    var indicesStack = [];

    /**
     * The stack of assignments have been visited.
     * @type {Array}
     */
    var visitedAssignmentIdStack = [];

    /**
     * True once the top level assignment has been navigated to the last index.
     *
     * (true even if looping is on).
     * @type {Boolean}
     */
    var isDone = false;

    // some initialization
    (function() {
        // Sets the current index.
        if (!isUndefined(preferredIndex)) {
            try {
                currentIndex = parseInt(preferredIndex, 10);
            } catch (exception) {
                console.error('could not parse preferredIndex using 0 instead');
            }
        }
        // Sets the navigation level
        if (isUndefined(navigateAtSubgroupLevel)) {
            isSubgroupNavigation = false;
        }
    })();

    /**
     * @return {Number} assignment type of the current problem.
     */
    this.getAssignmentType = function getAssignmentType() {
        return currentAssignment.assignmentType;
    };

    /**
     * @return {Number} assignment type of the current problem.
     */
    this.getNavigationType = function getNavigationType() {
        return currentAssignment.navigationType;
    };

    /**
     * Changes the problem to the current index.
     *
     * @instance
     * @memberof AssignmentNavigator
     */
    this.refresh = function() {
        changeSubgroup(currentIndex);
    };

    /**
     * @return {Boolean} true if the problems have been loaded into memory.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.isDataLoaded = function() {
        return dataLoaded;
    };

    /**
     * Sets the knowledge of if the navigator ui has been loaded.
     *
     * @param {Boolean} value - True if the ui has been loaded.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.setUiLoaded = function(value) {
        uiLoaded = value;
    };

    /**
     * @returns {Number} the number of subgroups in the list.
     * @memberof AssignmentNavigator
     */
    this.getSubgroupSize = function getSubgroupSize() {
        return subgroupList.length;
    };

    /**
     * @returns {BankProblem | LectureSlide} The entire set of data that is in the bank problem or lecture slide.
     * @memberof AssignmentNavigator
     */
    function getCurrentInfo() {
        return currentSubgroupPart;
    }

    /**
     * Scopes the index for the callbackList.
     *
     * This way the browser is not locked up by callbacks.
     *
     * @param {Number} scopedIndex - The index from the callbackList to call.
     * @instance
     * @access private
     * @memberof AssignmentNavigator
     */
    function callBacker(scopedIndex) {
        setTimeout(function() {
            callbackList[scopedIndex](localScope);
        }, 20);
    }

    /**
     * Returns {Boolean} True if navigation is random.
     */
    function isRandomNavigation() {
        var assignmentType = currentAssignment.assignmentType;
        var navigationType = currentAssignment.navigationType;
        return navigationType === CourseSketch.prutil.NavigationType.RANDOM ||
            (navigationType === CourseSketch.prutil.NavigationType.DEFAULT && assignmentType === CourseSketch.prutil.AssignmentType.FLASHCARD);
    }

    /**
     * @return {Boolean} True if the assignment is able to be looped.
     */
    function isLoopable() {
        return currentAssignment.navigationType === CourseSketch.prutil.ItemType.LOOPING;
    }

    /**
     * Loads an assignment given the assignmentId and sets it as the new current assignment.
     *
     * This also sets the subgroupList.
     * Calls the callback after the local data is set.
     *
     * @param {UUID} assignmentId - The id of the asignment to load.
     * @param {Function} [callback] - A callback function called when the assignment is loaded.
     */
    function loadAssignment(assignmentId, callback) {

        function setData(assignment) { // jscs:ignore jsDoc
            currentAssignment = assignment;
            subgroupList = currentAssignment.problemGroups;
            currentAssignmentId = assignment.id;
            if (!isUndefined(callback)) {
                callback();
            }
        }
        CourseSketch.dataManager.getAssignment(assignmentId, setData, setData);
    }

    /**
     * Reloads the assignment from the id and assigns it to the currentAssignment.
     *
     * @instance
     * @memberof ProblemNavigator
     */
    this.reloadAssignment = function() {
        loadAssignment(currentAssignmentId);
    };

    /*******************************
     * START OF NAVIGATION LOGIC
     ******************************/

    /**
     * Changes the index to point at this new problem.
     *
     * @param {Number} index - The problem that we want to switch to.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.goToSubgroup = function goToSubgroup(index) {
        changeSubgroup(index);
    };

    /**
     * Attempts to change to the next problem or slide.
     *
     * @instance
     * @memberof AssignmentNavigator
     */
    this.gotoNext = function() {
        if (isSubgroupNavigation) {
            changeSubgroup(currentIndex + 1);
        } else {
            goToNextSubgroupPart();
        }
    };

    /**
     * Attempts to change to the previous problem or slide.
     *
     * @instance
     * @memberof AssignmentNavigator
     */
    this.gotoPrevious = function() {
        if (isSubgroupNavigation) {
            changeSubgroup(currentIndex - 1);
        } else {
            goToPreviousSubgroupPart();
        }
    };

    /**
     * Called when navigation is finished.
     *
     * Currently just calls the callbacks.
     */
    function doneNavigating() {
        for (var i = 0; i < callbackList.length; i++) {
            callBacker(i);
        }
    }

    /**
     * Creates an assignment location.
     *
     * @param {Number} subGroupIndex - The index of problem subgroup.
     * @param {Number} partIndex - The index of the subgroup part.
     *
     * @returns {{group: Number, part: Number}} An assignment location.
     */
    function createAssignmentLocation(subGroupIndex, partIndex) {
        return {
            'group': subGroupIndex,
            'part': partIndex
        };
    }

    /**
     * Navigate from an nested assignment to the one that contains it.
     */
    function navigateFromNested() {
        var nextAssignmentLocation = indicesStack.pop();
        var nextAssignmentId = assignmentIdStack.pop();
        while (nextAssignmentLocation.group === -1 && indicesStack.length > 0) {
            nextAssignmentLocation = indicesStack.pop();
            nextAssignmentId = assignmentIdStack.pop();
        }
    }

    /**
     *
     * Navigates to the previously nested lecture that the user has navigated to.
     */
    function navigateToPreviousNested() {
    }

    /**
     * Loads a nested assignment.
     *
     * Pushes the startingAssignmentId onto the stack and pushes the next location onto the indices stack.
     *
     * @param {String} assignmentId - The id of the nested assignment.
     */
    function loadNestedAssignment(assignmentId) {
        assignmentIdStack.push(currentAssignmentId);
        if (currentSubgroupPartIndex + 1 >= currentSubgroup.subgroups.length) {
            currentSubgroupPartIndex = 0;
            currentIndex = 0;
        } else {
            currentSubgroupPartIndex += 1;
        }

        if (currentIndex >= subgroupList.length) {
            currentIndex = -1;
        }

        indicesStack.push(createAssignmentLocation(currentIndex, currentSubgroupPartIndex));
        loadAssignment(assignmentId, function() {
            goToSubgroup(0);
            gotoSubgroupPart(0);
            doneNavigating();
        });
    }

    /**
     * Changes the problem to the given index.
     *
     * If looping is set to false then if given an index out of bounds this function returns immediately.
     * Otherwise the index is set to either 0 or the end of the list depending on how it is out of bounds.
     * After changing the index all of the set callbacks are called.
     * Order of the callbacks is not guaranteed.
     *
     * @param {Number} index - the index we want to switch to.
     * @instance
     * @access private
     * @memberof AssignmentNavigator
     */
    function changeSubgroup(index) {
        if (index < 0 || index >= subgroupList.length && !isLoopable() && !isRandomNavigation()) {
            if (index < 0 && visitedAssignmentIdStack.length > 0) {
                navigateToPreviousNested();
                return;
            }
            if (index >= subgroupList.length && assignmentIdStack.length > 0) {
                navigateFromNested();
                return;
            }
            if (index >= subgroupList.length) {
                isDone = true;
            }

            // TODO: change this to a navigation expection.
            throw new CourseSketch.BaseException('Index is not valid: ' + index + ' out of ' + getSubgroupListSize);
        }
        if (isRandomNavigation()) {
            //Pull problems at random for Game
            var numberOfQuestions = getSubgroupListSize();
            var randomNumber = Math.random();

            index = randomNumber % numberOfQuestions;
        } else if (isLoopable()) {
            if (index < 0) {
                index = problemList.length - 1;
            }
            if (index >= problemList.length) {
                index = 0;
            }
        }
        currentIndex = index;
        currentSubgroup = subgroupList[index];
        gotoSubgroupPart(0);
    }

    /**
     * Loads the subpart based on the holder.
     *
     * @param {ProblemSlideHolder} subgroupPartHolder - The holder that contains the information for the assignment.
     */
    function loadSubgroupPart(subgroupPartHolder) {
        var id = subgroupPartHolder.id;
        if (subgroupPartHolder.itemType === CourseSketch.prutil.ItemType.ASSIGNMENT) {
            loadNestedAssignment(id);
            return;
        }

        currentSubgroupPartHolder = subgroupPartHolder;

        if (subgroupPartHolder.itemType === CourseSketch.prutil.ItemType.BANK_PROBLEM) {
            if (isUndefined(subgroupPartHolder.problem)) {
                CourseSketch.dataManager.getBankProblem(subgroupPartHolder.id, function(bankProblem) {
                    currentSubgroup = bankProblem;
                    currentSubgroupPartHolder.problem = bankProblem;
                    doneNavigating();
                });
            } else {
                doneNavigating();
            }
        } else if (subgroupPartHolder.itemType === CourseSketch.prutil.ItemType.SLIDE) {
            if (isUndefined(subgroupPartHolder.slide)) {
                CourseSketch.dataManager.getLectureSlide(subgroupPartHolder.id, function(lectureSlide) {
                    currentSubgroup = lectureSlide;
                    currentSubgroupPartHolder.slide = bankProblem;
                    doneNavigating();
                });
            } else {
                doneNavigating();
            }
        }
    }

    /**
     * Navigates to a specific subgroup part.
     *
     * Assumptions that index is a valid index.
     *
     * @param {Number} index - The index of the next subgroup part.
     */
    function gotoSubgroupPart(index) {
        var subgroupPartLength = currentSubgroup.subgroups.length;
        if (index >= subgroupPartLength || index < 0) {
            // TODO: change this to a navigation expection.
            throw new CourseSketch.BaseException('Index is not valid: ' + index);
        }
        currentSubgroupPartIndex = index;
        loadSubgroupPart(currentSubgroup.subgroups[index]);
    }

    /**
     * Goes to the next slide or problem part.
     *
     * Navigation rules are at the top of this file!
     *
     * @memberof AssignmentNavigator
     */
    function goToNextSubgroupPart() {
        var subgroupPartLength = currentSubgroup.subgroups.length;
        if (currentSubgroupPartIndex >= subgroupPartLength) {
            currentSubgroupPartIndex = 0;
            changeSubgroup(currentIndex + 1);
            return;
        }
        gotoSubgroupPart(currentSubgroupPartIndex + 1);
    }

    /**
     * Goes to the next slide.
     *
     * Navigation rules are at the top of this file!
     *
     * @memberof AssignmentNavigator
     */
    function goToPreviousSubgroupPart() {
        var subgroupPartLength = currentSubgroup.subgroups.length;
        if (currentSubgroupPartIndex >= subgroupPartLength) {
            currentSubgroupPartIndex = 0;
            changeSubgroup(currentIndex - 1);
            return;
        }
        gotoSubgroupPart(currentSubgroupPartIndex - 1);
    }
}
