/**
 * @class NavigationException
 * @extends BaseException
 *
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} cause - The cause of the exception.
 */
function NavigationException(message, cause) {
    this.name = 'NavigationException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

NavigationException.prototype = new BaseException();

/**
 * How this works is a polling system.
 *
 * When a problem/assignment/course in assignment view is changed all the parts are notify via a callback at which point they can poll
 * different parts of the system.
 * Callbacks are not guaranteed in any order.
 *
 * How navigation works.
 *
 * <ul>
 * <li>If you are navigating at subgroup part level:<ul>
 *     <li>Goto next:<ul>
 *         <li>If there is a next part navigate to it</li>
 *         <li>If there is not a next part then gotoNext at subgroup level</li></ul></li>
 *     <li>Goto previous:<ul>
 *         <li>If there is a previous part then navigate to it<li>
 *         <li>If there is not a previous part then gotoPrevious at subgroup level</li></ul></li></ul></li>
 * <li>If you are navigating at subgroup level:<ul>
 *      <li>Goto next:<ul>
 *         <li>If there is a next subgroup navigate to it</li>
 *         <li>If there is not a next subgroup:<ul>
 *              <li>If the stack is empty you reached the end!</li>
 *              <li>If the stack is not empty you pop up and navigate to the index specified on the list</li></ul></li></ul></li>
 *      <li>Goto previous:<ul>
 *         <li>If there is a previous subgroup navigate to it</li>
 *         <li>If there is not a previous group:<ul>
 *              <li>If the nested stack is empty you reached the beginning!</li>
 *              <li>If the nested stack is not empty you go in an and start at the end of that subgroup.</li></ul></li></ul></li></ul></li>
 * </ul>
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
     * @type {Array<UUID>}
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

    /**
     * The event constant for submitting a problem.
     *
     * @type {String}
     */
    this.SUBMIT_EVENT = 'submit';

    /**
     * The event constant for completing a problem.
     *
     * @type {String}
     */
    this.COMPLETED_PROBLEM_EVENT = 'completion';

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
        changeSubgroup(currentIndex, 0);
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
     * @returns {Number} the number of parts in the subgroup list.
     * @memberof AssignmentNavigator
     */
    this.getSubgroupPartSize = function getSubgroupSize() {
        return currentSubgroup.subgroups.length;
    };

    /**
     * @return {SrlAssignment} the current assignment stored in this navigator..
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getCurrentAssignment = function() {
        return currentAssignment;
    };

    /**
     * @returns {BankProblem | LectureSlide} The entire set of data that is in the bank problem or lecture slide.
     * @memberof AssignmentNavigator
     */
    this.getCurrentInfo = function getCurrentInfo() {
        return currentSubgroupPart;
    };

    /**
     * @return {QuestionType} the type of the base problem.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getProblemType = function() {
        var type = getCurrentInfo().questionType;
        return type;
    };

    /**
     * @return {ItemType} the type of current part.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getPartType = function() {
        return currentSubgroupPartHolder.itemType;
    };

    /**
     * @returns {Number} the current problem number in a human readable format.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getCurrentNumber = function() {
        if (isSubgroupNavigation) {
            return currentIndex + 1;
        }
        return currentSubgroupPartIndex + 1;
    };

    /**
     * @returns {Number} the current index of the subgroup.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getCurrentSubgroupIndex = function() {
        return currentIndex;
    };

    /**
     * @returns {Number} the current index of the subgroup.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.getCurrentPartIndex = function() {
        return currentSubgroupPartIndex;
    };

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
        dataLoaded = false;

        function setData(assignment) { // jscs:ignore jsDoc
            currentAssignment = assignment;
            currentAssignmentId = assignment.id;
            CourseSketch.dataManager.getAllProblemsFromAssignment(assignmentId, function(problems) {
                subgroupList = [];
                for (var i = 0; i < problems.length; i++) {
                    subgroupList.push(problems[i]);
                }
                if (!isUndefined(callback)) {
                    callback();
                }
                dataLoaded = true; // this one will take longer so we do this one second.
            });
        }
        CourseSketch.dataManager.getAssignment(assignmentId, setData, setData);
    }

    /**
     * Reloads the assignment from the id and assigns it to the currentAssignment.
     *
     * @instance
     * @memberof AssignmentNavigator
     */
    this.reloadAssignment = function() {
        loadAssignment(currentAssignmentId, function() {
            localScope.refresh();
        });
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
        changeSubgroup(index, 0);
    };

    /**
     * Attempts to change to the next problem or slide.
     *
     * @instance
     * @memberof AssignmentNavigator
     */
    this.gotoNext = function() {
        if (isSubgroupNavigation) {
            changeSubgroup(currentIndex + 1, 1);
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
            changeSubgroup(currentIndex - 1, -1);
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
     * @param {Number} subgroupIndex - The index of problem subgroup.
     * @param {Number} partIndex - The index of the subgroup part.
     * @param {Number} oldSubgroupIndex - The index of problem subgroup for navigating backwords.
     * @param {Number} oldPartIndex - The index of the subgroup part for navigating backwords.
     *
     * @returns {{group: Number, part: Number, backGroup: Number, backPart: Number}} An assignment location.
     */
    function createAssignmentLocation(subgroupIndex, partIndex, oldSubgroupIndex, oldPartIndex) {
        return {
            'group': subgroupIndex,
            'part': partIndex,
            'backGroup': oldSubgroupIndex,
            'backPart': oldPartIndex
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
        currentAssignmentId = nextAssignmentId;
        currentIndex = nextAssignmentLocation.group;
        currentSubgroupPartIndex = nextAssignmentLocation.part;
        loadAssignment(currentAssignmentId, function() {
            changeSubgroup(currentIndex, 0);
            gotoSubgroupPart(currentSubgroupPartIndex, 0);
            doneNavigating();
        });
    }

    /**
     * Navigates to the previously nested lecture that the user has navigated to.
     */
    function navigateFromNestedBackwards() {
        var nextAssignmentLocation = indicesStack.pop();
        var nextAssignmentId = assignmentIdStack.pop();
        while (nextAssignmentLocation.backGroup === -1 && indicesStack.length > 0) {
            nextAssignmentLocation = indicesStack.pop();
            nextAssignmentId = assignmentIdStack.pop();
        }
        currentAssignmentId = nextAssignmentId;
        currentIndex = nextAssignmentLocation.group;
        currentSubgroupPartIndex = nextAssignmentLocation.part;
        loadAssignment(assignmentId, function() {
            goToSubgroup(currentIndex);
            gotoSubgroupPart(currentSubgroupPartIndex);
            doneNavigating();
        });
    }

    /**
     * Loads a nested assignment.
     *
     * Pushes the startingAssignmentId onto the stack and pushes the next location onto the indices stack.
     *
     * @param {String} assignmentId - The id of the nested assignment.
     * @param {Number} direction - The direction of navigation.
     */
    function loadNestedAssignment(assignmentId, direction) {
        assignmentIdStack.push(currentAssignmentId);

        var oldSubgroupPartIndex = currentSubgroupPartIndex - 1;
        var oldSubgroupIndex = currentIndex;
        if (oldSubgroupPartIndex < 0) {
            oldSubgroupPartIndex = 0;
            oldSubgroupIndex -= 1;
        }

        if (currentSubgroupPartIndex + 1 >= currentSubgroup.subgroups.length) {
            currentSubgroupPartIndex = 0;
            currentIndex = 0;
        } else {
            currentSubgroupPartIndex += 1;
        }

        if (currentIndex >= subgroupList.length) {
            currentIndex = -1;
        }

        indicesStack.push(createAssignmentLocation(currentIndex, currentSubgroupPartIndex, oldSubgroupIndex, oldSubgroupPartIndex));
        loadAssignment(assignmentId, function() {
            if (direction < 0) {
                changeSubgroup(subgroupList.length - 1, direction);
            } else {
                changeSubgroup(0, direction);
            }
            doneNavigating();
        });
    }

    /**
     * Handles the navigation of nested assignments and non looping exceptions.
     *
     * @param {Number} index - the index we want to switch to.
     * @param {Number} direction - The direction of navigation.
     */
    function cantThinkOfFunctionName(index, direction) {
        if (index < 0 && assignmentIdStack.length > 0) {
            navigateFromNestedBackwards();
            return;
        }
        if (index >= subgroupList.length && assignmentIdStack.length > 0) {
            navigateFromNested();
            return;
        }
        if (index >= subgroupList.length) {
            isDone = true;
        }

        if (direction !== 0) {
            // TODO: change this to a navigation exception.
            throw new NavigationException('Index is not valid: [' + index + ' out of ' + getSubgroupListSize + ']');
        }
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
     * @param {Number} direction - The direction of navigation.
     * @instance
     * @access private
     * @memberof AssignmentNavigator
     */
    function changeSubgroup(index, direction) {
        if (index < 0 || index >= subgroupList.length && !isLoopable() && !isRandomNavigation()) {
            cantThinkOfFunctionName(index, direction);
            return;
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
        if (direction < 0) {
            gotoSubgroupPart(currentSubgroup.subgroups.length - 1, direction);
        } else if (direction === 0) {
            // The subgroup did not change
            gotoSubgroupPart(currentSubgroupPartIndex, direction);
        } else {
            gotoSubgroupPart(0, direction);
        }
    }

    /**
     * Loads the subpart based on the holder.
     *
     * @param {ProblemSlideHolder} subgroupPartHolder - The holder that contains the information for the assignment.
     * @param {Number} direction - The direction of navigation.
     */
    function loadSubgroupPart(subgroupPartHolder, direction) {
        var id = subgroupPartHolder.id;
        if (subgroupPartHolder.itemType === CourseSketch.prutil.ItemType.ASSIGNMENT) {
            loadNestedAssignment(id, direction);
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
     * @param {Number} direction - The direction of navigation.
     */
    function gotoSubgroupPart(index, direction) {
        var subgroupPartLength = currentSubgroup.subgroups.length;
        if (index >= subgroupPartLength || index < 0) {
            // TODO: change this to a navigation expection.
            throw new NavigationException('Index is not valid: [' + index + ' out of ' + subgroupPartLength + ']');
        }
        currentSubgroupPartIndex = index;
        loadSubgroupPart(currentSubgroup.subgroups[index], direction);
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
            changeSubgroup(currentIndex + 1, 1);
            return;
        }
        gotoSubgroupPart(currentSubgroupPartIndex + 1, 1);
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
            changeSubgroup(currentIndex - 1, -1);
            return;
        }
        gotoSubgroupPart(currentSubgroupPartIndex - 1, -1);
    }

    /********
     * EVENTS
     *******/

    /**
     * Add an event mapping for a specific callback.
     *
     * @param {*} key - The key of the event mapping.
     * @param {Function} funct - The function called when the event is called.
     * @instance
     * @memberof AssignmentNavigator
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
     *
     * @param {*} key - The key of the event mapping.
     * @param {Function} funct - The function that is being removed.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.removeEventMapping = function(key, funct) {
        if (isUndefined(eventMappingCallback[key])) {
            return;
        }
        removeObjectFromArray(eventMappingCallback[key], funct);
    };

    /**
     * Clears all mappings of event callbacks.
     *
     * @param {*} key - The key of the event mapping.
     * @instance
     * @memberof AssignmentNavigator
     */
    this.clearAllMappings = function(key) {
        eventMappingCallback[key] = undefined;
    };

    /**
     * Attempts to execute all of the events.
     *
     * The failure of one event should not affect the other events.
     * The order of events firing is not guaranteed.
     *
     * @param {*} key - The key of the event mapping.
     * @param {Array<*>} funcArgs - A list of arguments to call the function with.
     * @instance
     * @memberof AssignmentNavigator
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
}
