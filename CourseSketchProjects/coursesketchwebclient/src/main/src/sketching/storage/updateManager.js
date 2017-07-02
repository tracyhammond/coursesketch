/**
 * @class UpdateException
 * @extends BaseException
 *
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} cause - The cause of the exception.
 */
function UpdateException(message, cause) {
    this.name = 'UpdateException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

UpdateException.prototype = new BaseException();

/**
 * @class UndoRedoException
 * @extends UpdateException
 */
function UndoRedoException(message, cause) {
    this.name = 'UndoRedoException';
    this.setMessage(message);
    this.setCause(cause);
}
UndoRedoException.prototype = new UpdateException();

/**
 * The update manager manages the lists of actions that have occurred for a
 * sketch, or multiple sketches.
 * Goals: The update manager can be used for multiple sketches (using the switch
 * sketch command)
 *
 * @param {SketchManager} sketchManager - The manager that the sketch deals with.
 * @param {Function} onError - A method that is called when an error occurs.
 * @class UpdateManager
 */
function UpdateManager(sketchManager, onError) {

    /**
     * The id of the current sketch that is being used by the update list (this may switch multiple times)
     */
    var currentSketchId;

    /**
     * Holds a state of updates (for undoing and redoing).
     * Note that this always points after the last executed update.
     * If there have been no undo's or redo's or the list is currently not in undo redo mode this will be the same as the updateList length
     * after execution of the update.
     */
    var currentUpdateIndex = 0;

    /**
     * Holds the pointer to the end of the list (only really used with markers).
     *
     * @type {Number}
     */
    var currentEndingIndex = 0;

    /**
     * If a marker is executed with an index larger than what we have then we
     * wait.
     */
    var skippingMarkerMode = false;
    var amountToSkip = 0;
    var localScope = this;

    /**
     * Updates to the current index on every submission.
     *
     * @type {Number}
     */
    var lastSubmissionPointer = 0;

    /**
     * Records the last type of update.
     * 0 = normal update
     * -1 = undo
     * 1 = redo
     * 2 =
     *
     * @type {Number}
     */
    var lastUpdateType = 0;

    /**
     * Updates to the current index on every save and every submission.
     *
     * @type {Number}
     */
    var lastSavePointer = 0;

    /**
     * A list of plugins whose methods are called when addUpdate is called.
     *
     * @type {List<Plugin>}
     */
    var plugins = [];

    /**
     * Holds the list of updates that are waiting to be executed locally
     *
     * This list should almost always be near empty. there is also an
     * executionLock and a boolean for the queue being empty
     */
    var queuedLocalUpdates = [];
    var executionLock = false;

    var inRedoUndoMode = false;
    var netCount = 0; // This is equal to undo - redo

    /**
     * Holds the entire list of updates
     */
    var updateList = [];

    /**
     * Adds an update to the updateList.
     *
     * Adds an update to the queue that then executes them as quick as possible.  This will not freeze up the browser.
     *
     * @param {SrlUpdate} update - The update that is being added to this specific
     *            update manager.
     * @param {String} [pluginId] - The id of the plugin that added the update.
     */
    this.addUpdate = function(update, pluginId) {
        // TODO: find a better way to manage cleaning updates if possible.
        var cleanedUpdate = cleanUpdate(update);
        cleanedUpdate.sketchManager = sketchManager;
        cleanedUpdate.pluginId = pluginId;
        queuedLocalUpdates.push(cleanedUpdate);
        emptyLocalQueue();
    };

    /**
     * Adds an update to the updateList.
     *
     * Adds the update in a synchronous manner, if the local queue is not empty this will empty it then add the update list.
     * Throws an exception if the queue is currently locked.  Please do not use often.
     *
     * @param {SrlUpdate} update - The update that is being added to this specific
     *            update manager.
     * @param {String} [pluginId] - The id of the plugin that added the update.
     */
    this.addSynchronousUpdate = function(update, pluginId) {
        var cleanedUpdate = cleanUpdate(update);
        cleanedUpdate.sketchManager = sketchManager;
        cleanedUpdate.pluginId = pluginId;
        queuedLocalUpdates.push(cleanedUpdate);
        emptyLocalQueueSynchronously();
    };

    /**
     * Adds a plugin that is called after each update is added.
     * This can be used by graphics or for recognition purposes.
     *
     * @param {Object.addUpdate} plugin - Plugin that has an addUpdate method that can be called.
     * @callback {Function} addUpdate
     * @callbackParam {ProtobufUpdate} update - The update the was just executed by the update manager.
     * @callbackParam {Boolean} redraw - True if the sketch should be redrawn after executing this update.
     * @callbackParam {Integer} index - What index this update was.  (Typically is or close to the number of updates in the list)
     * @callbackParam {Integer} updateType - The last update type
     * @callbackParam {String} pluginId - The id of the plugin that added this update. (Undefined if no plugin added the update)
     */
    this.addPlugin = function(plugin) {
        plugins.push(plugin);
    };

    /**
     * Clears the current updates.
     *
     * @param {Boolean} redraw - If true then the sketch will be redrawn.
     * @param {Boolean} deepClear - If true does some manual unlinking to hopefully help out the gc
     */
    this.clearUpdates = function clearUpdates(redraw, deepClear) {
        currentUpdateIndex = 0;
        if (deepClear) {
            for (var i = 0; i < updateList.length; i++) {
                updateList[i].sketchManager = undefined;
                var commandList = updateList[i].commands;
                for (var k = 0; k < commandList.length; k++) {
                    commandList[k].decodedData = undefined;
                }
            }
        }

        // Empties the array
        while (updateList.length > 0) {
            updateList.pop();
        }

        lastUpdateType = 0;
        lastSubmissionPointer = 0;
        inRedoUndoMode = false;
        skippingMarkerMode = false;
        sketchManager.getCurrentSketch().resetSketch();
    };

    /**
     * Switches to a certain sketch with the given Id.
     *
     * @param {UUID} id - The id of which sketch to switch to.
     */
    function switchToSketch(id) {
        if (isUndefined(sketchManager)) {
            throw new UpdateException('Can not switch sketch with an invalid manager');
        }
        if (isUndefined(id)) {
            throw new UpdateException('Can not switch to an undefined sketch');
        }
        currentSketchId = id;
        sketchManager.setCurrentSketch(id);
    }

    /**
     * Generates a marker that can be used for marking things.
     *
     * Returns the result as a Command.
     *
     * @param {Boolean} userCreated - True if the user created this marker.
     * @param {MarkerType} markerType - The type that the marker is.
     * @param {String} otherData - Contains other important data.
     * @returns {SrlCommand} The command of the created marker.
     */
    this.createMarker = function createMarker(userCreated, markerType, otherData) {
        var marker = CourseSketch.prutil.Marker();
        marker.setType(markerType);
        if (!isUndefined(otherData)) {
            marker.setOtherData(otherData);
        }

        var command = CourseSketch.prutil.SrlCommand();
        command.setCommandType(CourseSketch.prutil.CommandType.MARKER);
        command.setIsUserCreated(userCreated);
        command.setCommandData(marker.toArrayBuffer());
        command.setCommandId(generateUUID());
        return command;
    };

    /**
     * Executes the update locking execution.
     * NOTE: This should only be called be emptyLocalQueue and emptyLocalQueueSynchronously.
     */
    function executeUpdateLocked() {
        executionLock = true;
        var nextUpdate = removeObjectByIndex(queuedLocalUpdates, 0);
        var updateCreatedByPlugin = nextUpdate.pluginId;
        try {
            var redraw = executeUpdate(nextUpdate);
            if (!skippingMarkerMode) {
                var updateIndex = currentUpdateIndex;
                var offset = lastUpdateType === -1 ? 0 : 1;
                var pluginUpdate = updateList[updateIndex - offset];
                var updateType = lastUpdateType;
                for (var i = 0; i < plugins.length; i++) {
                    if (!isUndefined(plugins[i].addUpdate)) {
                        plugins[i].addUpdate(pluginUpdate, redraw, updateIndex, updateType, updateCreatedByPlugin);
                    }
                }
                updateType = undefined;
                pluginUpdate = undefined;
                updateIndex = undefined;
            }
        } catch (exception) {
            executionLock = false;
            if (!isUndefined(onError)) {
                onError(exception);
            } else {
                console.error(exception);
            }
        }
        executionLock = false;
    }


    /**
     * Tries to quickly empty the local queue.
     *
     * Ensures that even with the rapid addition of updates there are no
     * executions that overlap.
     */
    function emptyLocalQueue() {
        if (queuedLocalUpdates.length > 0) {
            if (!executionLock) {
                executeUpdateLocked();
                setTimeout(function() {
                    emptyLocalQueue();
                }, 10);
            } else {
                // We wait and try again when the executionLock is gone
                setTimeout(function() {
                    emptyLocalQueue();
                }, 10);
            }
        }
    }

    /**
     * Tries to quickly empty the local queue.
     *
     * Ensures that even with the rapid addition of updates there are no
     * executions that overlap, does not use timers and can freeze the browser if there are lots of updates.
     * Please use sparingly.
     */
    function emptyLocalQueueSynchronously() {
        while (queuedLocalUpdates.length > 0) {
            if (!executionLock) {
                executeUpdateLocked();
                executionLock = false;
            } else {
                throw new UpdateException('Execution is locked can not add update synchronously');
            }
        }
    }

    /**
     * Executes an update.
     *
     * Does special handling with redo and undo.
     *
     * @param {SrlUpdate} update - The update that is being executed.
     * @returns {Boolean} True if the object needs to be redrawn.
     */
    function executeUpdate(update) {
        /*jshint maxcomplexity:11 */
        /*
        update.getLocalSketchSurface = function() {
            return sketchManager.get(this.sketchId);
        };
        */
        if (update.getCommands().length <= 0) {
            throw new UpdateException('Can not execute an empty update.');
        }
        var command = update.getCommands()[0].commandType;
        if (skippingMarkerMode) {
            updateList.push(update);
            amountToSkip -= 1;
            if (amountToSkip <= 0) {
                skippingMarkerMode = false;
            }
            return;
        }
        if (inRedoUndoMode) {
            if ((command !== CourseSketch.prutil.CommandType.REDO && command !== CourseSketch.prutil.CommandType.UNDO)) {
                // We do a bunch of changing then we call the executeUpdate
                // method again
                var splitDifference = updateList.length - currentUpdateIndex;

                // Creates and inserts the first marker [update] -> [marker] ->
                // [unreachable update]
                var startingMarker = localScope.createMarker(false, CourseSketch.prutil.getMarkerClass().MarkerType.SPLIT, '' +
                        splitDifference);
                updateList.splice(currentUpdateIndex, 0, CourseSketch.prutil.createUpdateFromCommands([ startingMarker ]));

                // Creates and inserts the second marker [unreachable update
                // (probably undo or redo)] -> [marker] -> [index out of range]
                var endingMarker = localScope.createMarker(false, CourseSketch.prutil.getMarkerClass().MarkerType.SPLIT, '' +
                        (0 - splitDifference));
                updateList.push(CourseSketch.prutil.createUpdateFromCommands([ endingMarker ]));

                // Reset the information
                inRedoUndoMode = false;
                netCount = 0;
                currentUpdateIndex = updateList.length;
                return executeUpdate(update);
            }
        }
        if (command === CourseSketch.prutil.CommandType.REDO) {
            lastUpdateType = 1;
            if (netCount >= 0) {
                throw new UndoRedoException('Can not Redo Anymore');
            }
            updateList.push(update);
            currentEndingIndex += 1;
            netCount += 1;
            var redrawRedo = redoUpdate(updateList[currentUpdateIndex]);
            currentUpdateIndex += 1;
            return redrawRedo;
        } else if (command === CourseSketch.prutil.CommandType.UNDO) {
            lastUpdateType = -1;
            if (currentUpdateIndex <= 0) {
                throw new UndoRedoException('Can not Undo Anymore');
            }
            if (!inRedoUndoMode) {
                netCount = 0;
                inRedoUndoMode = true;
            }
            netCount -= 1;
            updateList.push(update);
            currentEndingIndex += 1;
            var redrawUndo = undoUpdate(updateList[currentUpdateIndex - 1]);
            currentUpdateIndex -= 1;
            return redrawUndo;
        } else {
            lastUpdateType = 0;
            // A normal update
            currentEndingIndex += 1;
            updateList.push(update);
            var redrawExecute = redoUpdate(update);
            currentUpdateIndex += 1;
            return redrawExecute;
        }
    }

    /**
     * If the update is a marker than it will skip that parts that can not be
     * reached.
     *
     * @param {SrlUpdate} update - The update that is being redone.
     * @returns {Boolean} True if the sketch needs to be redrawn.
     */
    function redoUpdate(update) {
        /*jshint maxcomplexity:13 */
        var command = update.getCommands()[0];
        // Marker will not have any other commands with its update
        if (command.commandType === CourseSketch.prutil.CommandType.MARKER) {
            var marker = CourseSketch.prutil.decodeProtobuf(command.commandData, CourseSketch.prutil.getMarkerClass());
            if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SPLIT) {
                var tempIndex = currentUpdateIndex;
                currentUpdateIndex += parseInt(marker.otherData, 10) + 1;
                if (currentUpdateIndex > updateList.length) {
                    amountToSkip = parseInt(marker.otherData, 10) + 1;
                    skippingMarkerMode = true;
                }
            } else if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SUBMISSION) {
                if (currentUpdateIndex > lastSubmissionPointer) {
                    lastSubmissionPointer = currentUpdateIndex;
                }
                if (currentUpdateIndex > lastSavePointer) {
                    lastSavePointer = currentUpdateIndex;
                }
            }  else if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SAVE) {
                if (currentUpdateIndex > lastSavePointer) {
                    lastSavePointer = currentUpdateIndex;
                }
            }
            return false;
        // This can have other commands with its update.
        } else if (command.commandType === CourseSketch.prutil.CommandType.CREATE_SKETCH) {
            // For undo we need the sketch id before we switch
            command.decodedData = currentSketchId;
            var sketchData = CourseSketch.prutil.decodeProtobuf(command.commandData, CourseSketch.prutil.getActionCreateSketchClass());
            var newSketchId = sketchData.sketchId.idChain[0];
            if (!isUndefined(sketchManager) &&
                    (!isUndefined(sketchManager.getCurrentSketch()) && sketchManager.getCurrentSketch().id !== newSketchId) ||
                    isUndefined(sketchManager.getCurrentSketch())) {
                sketchManager.createSketch(newSketchId, sketchData);
            }
            switchToSketch(newSketchId);
        // This can have other commands with its update.
        } else if (command.commandType === CourseSketch.prutil.CommandType.SWITCH_SKETCH) {
            // For undoing
            command.decodedData = currentSketchId;
            var sketchId = CourseSketch.prutil.decodeProtobuf(command.commandData, CourseSketch.prutil.getIdChainClass()).idChain[0];
            switchToSketch(sketchId);
        }
        return update.redo();
    }

    /**
     * If the update is a marker than it will skip that parts that can not be reached.
     *
     * @param {SrlUpdate} update - The update that is being undone.
     * @returns {Boolean} True if the sketch needs to be redrawn.
     */
    function undoUpdate(update) {

        var command = update.getCommands()[0];
        if (command.commandType === CourseSketch.prutil.CommandType.MARKER) {
            var marker = CourseSketch.prutil.decodeProtobuf(command.commandData, CourseSketch.prutil.getMarkerClass());
            if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SPLIT) {
                currentUpdateIndex += parseInt(marker.otherData, 10) - 1;
            }
            // Does not actually change sketch so no drawing happens
            return false;
        } else if (command.commandType === CourseSketch.prutil.CommandType.CREATE_SKETCH) {
            var sketchData = CourseSketch.prutil.decodeProtobuf(command.commandData, CourseSketch.prutil.getActionCreateSketchClass());
            var id = sketchData.sketchId.idChain[0];

            // Undo happens in reverse order so it must happen before switchToSketchHappens
            update.undo();

            if (!isUndefined(sketchManager)) {
                sketchManager.deleteSketch(id);
            }
            switchToSketch(command.decodedData);
            return true;
        } else if (command.commandType === CourseSketch.prutil.CommandType.SWITCH_SKETCH) {
            // Undo happens in reverse order so it must happen before switchToSketchHappens
            update.undo();

            switchToSketch(command.decodedData);
            return true;
        }
        return update.undo();
    }

    /**
     * Returns a copy of the updateList for the purpose of not being edited
     * while in use.
     *
     * This is a delayed method to prevent javascript from freezing the browser.
     *
     * @param {Function} callback - Called after the list is cleaned.
     */
    this.getCleanUpdateList = function(callback) {
        var index = 0;
        var maxIndex = updateList.length;
        var newList = [];
        // For local scoping
        var oldList = updateList;
        var intervalHolder = setInterval(function() {
            var startIndex = index;
            while (index < maxIndex && startIndex - index <= 5) {
                var update = oldList[index];
                var newUpdate = cleanUpdate(update);
                newList.push(newUpdate);
                index++;
            }
            if (index >= maxIndex) {
                clearInterval(intervalHolder);
                if (callback) {
                    callback(newList);
                }
            }
        }, 10);
    };

    /**
     * Changes the time of the update at index to be the new time.
     *
     * @param {Number} time - The time at which the update took place.
     * @param {Number} index - Used to specify what update to update.
     */
    this.setUpdateTime = function(time, index) {
        updateList[index].setTime(time.toString());
    };

    /**
     * Changes the time of the last save or submission update to be the new time.
     *
     * @param {Number} time - Used to specify what update to update.
     */
    this.setLastSaveTime = function(time) {
        this.setUpdateTime(time, lastSavePointer);
    };

    /**
     * Returns a direct copy of the update list that is modifiable.
     *
     * @param {Function} [callback] - called with the update list.
     */
    this.getUpdateList = function(callback) {
        if (callback) {
            callback(updateList);
        }
        return updateList;
    };

    /**
     * @returns {Integer} The length of the current list.
     */
    this.getListLength = function() {
        return updateList.length;
    };

    /**
     * @returns {Boolean} True IFF a submission marker is the last item that was
     *          submitted.
     */
    this.isLastUpdateSubmission = function() {
        if (updateList.length <= 0) {
            return false;
        }
        var update = updateList[updateList.length - 1];
        var commandList = update.getCommands();
        var currentCommand = commandList[0];
        if (currentCommand.commandType === CourseSketch.prutil.CommandType.MARKER) {
            var marker = CourseSketch.prutil.decodeProtobuf(currentCommand.commandData, CourseSketch.prutil.getMarkerClass());
            if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SUBMISSION) {
                return true;
            }
        }
        return false;
    };

    /**
     * @returns {Boolean} True IFF a save marker is the last item that was
     *          submitted.
     */
    this.isLastUpdateSave = function() {
        if (updateList.length <= 0) {
            return false;
        }
        var update = updateList[updateList.length - 1];
        var commandList = update.getCommands();
        var currentCommand = commandList[0];
        if (currentCommand.commandType === CourseSketch.prutil.CommandType.MARKER) {
            var marker = CourseSketch.prutil.decodeProtobuf(currentCommand.commandData, CourseSketch.prutil.getMarkerClass());
            if (marker.type === CourseSketch.prutil.getMarkerClass().MarkerType.SAVE) {
                return true;
            }
        }
        return false;
    };

    /**
     * @returns {Boolean} The opposite of isLastUpdateSubmission, except in the
     *          case where updateList.length() is non-positive.
     */
    this.isValidForSubmission = function() {
        if (updateList.length <= 0) {
            return false;
        }
        return !this.isLastUpdateSubmission();
    };

    /**
     * @returns {Boolean} True if the last update is not a submission and the last update is not a save marker.
     */
    this.isValidForSaving = function() {
        if (updateList.length <= 0) {
            return false;
        }
        return !(this.isLastUpdateSubmission() || this.isLastUpdateSave());
    };

    /**
     * @returns {Number} The current update index.
     */
    this.getCurrentPointer = function() {
        return currentUpdateIndex;
    };

    /**
     * This clears any current updates and replaces the list with a new list.
     *
     * @param {Array} list - The list that is will be added to the sketch
     * @param {Object} percentBar - The bar that will show these updates. It is called with how much is left to be completed.
     * @param {Function} finishedCallback - Called when the update list is set and executed.
     */
    this.setUpdateList = function(list, percentBar, finishedCallback) {
        if (!Array.isArray(list)) {
            throw new UpdateException('Input list is not an array: ' + list);
        }
        var initializing = true;
        this.clearUpdates(false);
        var index = 0;
        var maxIndex = list.length;
        var numberOfUpdatesAtOnce = 2;
        var intervalHolder = setInterval(function() {
            var startIndex = index;
            while (index < maxIndex && index - startIndex < numberOfUpdatesAtOnce) {
                if (percentBar) {
                    percentBar.updatePercentBar(index, maxIndex);
                }
                localScope.addUpdate(list[index]);
                index++;
            }
            if (index >= maxIndex) {
                clearInterval(intervalHolder);
                if (percentBar && percentBar.isRunning()) {
                    percentBar.updatePercentBar(1, 1);
                    percentBar.finishWaiting(300);
                }
                if (!isUndefined(finishedCallback)) {
                    finishedCallback();
                }
                percentBar = undefined;
                finishedCallback = undefined;
                list = undefined;
            }
        }, 20);
    };

    /**
     * Creates and adds a redo update to the stack.
     *
     * @param {Boolean} userCreated - True if the userCreated the command false otherwise.
     */
    this.redoAction = function(userCreated) {
        var redoCommand = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.REDO, userCreated);
        var update = CourseSketch.prutil.createUpdateFromCommands([ redoCommand ]);
        this.addUpdate(update, false);
    };

    /**
     * Creates and adds a redo update to the stack.
     *
     * @param {Boolean} userCreated - True if the userCreated the command false otherwise.
     */
    this.undoAction = function(userCreated) {
        var undoCommand = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.UNDO, userCreated);
        var update = CourseSketch.prutil.createUpdateFromCommands([ undoCommand ]);
        var tempIndex = currentUpdateIndex;
        this.addUpdate(update, false);
    };

    /**
     * Sets the new sketch manager.
     *
     * @param {SketchManager} sketch - The manager of the current sketch.
     */
    this.setSketchManager = function(sketch) {
        sketchManager = sketch;
    };

    /**
     * Cleans the update to make sure it is the same as all new versions.
     *
     * @param {SrlUpdate} update - the update being cleaned.
     * @returns {SrlUpdate} the cleaned version of the update.
     */
    function cleanUpdate(update) {
        return CourseSketch.prutil.decodeProtobuf(update.toArrayBuffer(), CourseSketch.prutil.getSrlUpdateClass());
    }
}
