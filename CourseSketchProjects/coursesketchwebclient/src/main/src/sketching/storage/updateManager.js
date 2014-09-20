function UpdateException(message) {
    this.name = "UpdateException";
    this.setMessage(message);
    this.message = "";
    this.htmlMessage = "";
}

UpdateException.prototype = BaseException;

function UndoRedoException(message) {
    this.name = "UndoRedoException";
    this.setMessage(message);
}
UndoRedoException.prototype = new UpdateException();

/**
 * The update manager manages the lists of actions that have occured for a
 * sketch (or multiple sketches)
 * 
 * Goals: The update manager can be used for multiple sketches (using the switch
 * sketch command)
 * 
 * 
 * @param inputSketch
 *            {SrlSketch} The sketch that all of these updates are being added
 *            to.
 * @param onError
 *            {Function} A method that is called when an error occurs
 */
function UpdateManager(inputSketch, onError, sketchManager) {

    /**
     * The current sketch object that is being used by the update list (this may
     * switch multiple times)
     */
    var sketch = inputSketch;

    /**
     * the id of the current sketch that is being used by the update list (this
     * may switch multiple times)
     */
    var currentSketchId;

    /**
     * holds a state of updates (for undoing and redoing)
     */
    var currentUpdateIndex = 0;

    /**
     * holds the pointer to the end of the list (only really used with markers)
     */
    var currentEndingIndex = 0;

    /**
     * if a marker is executed with an index larger than what we have then we
     * wait.
     */
    var skippingMarkerMode = false;
    var amountToSkip = 0;
    var localScope = this;
    var lastSubmissionPointer = 0;

    /**
     * A list of plugins whose methods are called when addUpdate is called.
     */
    var plugins = new Array();

    /**
     * Holds the list of updates that are waiting to be executed locally
     * 
     * This list should almost always be near empty. there is also an
     * executionLock and a boolean for the queue being empty
     */
    var queuedLocalUpdates = new Array();
    var executionLock = false;

    var inRedoUndoMode = false;
    var netCount = 0; // this is equal to undo - redo

    /**
     * Holds the entire list of updates
     */
    var updateList = new Array();

    /**
     * Adds an update to the updateList
     * 
     * If the currentUpdateIndex less than the list size then we need to remove
     * all the other updates from the list and add the newest one on. These
     * commands are only executed if the command is fromRemote or execute is
     * true.
     * 
     * @param update
     *            {SrlUpdate} the update that is being added to this specific
     *            update manager.
     */
    this.addUpdate = function(update) {
        queuedLocalUpdates.push(update);
        emptyLocalQueue();

        for (var i = 0; i < plugins.length; i++) {
            if (!isUndefined(plugins[i].addUpdate)) {
                plugins[i].addUpdate(arguments);
            }
        }
    };

    /**
     * Clears the current updates.
     * 
     * @param redraw
     *            {boolean} if true then the sketch will be redrawn.
     */
    this.clearUpdates = function clearUpdates(redraw) {
        currentUpdateIndex = 0;
        updateList.length = 0;
        lastSubmissionPointer = 0;
        inRedoUndoMode = false;
        skippingMarkerMode = false;
        this.clearSketch(redraw);
    };

    /**
     * Clears the sketch but does not clear any updates.
     * 
     * @param redraw
     *            {boolean} if true then the sketch will be redrawn.
     */
    this.clearSketch = function clearSketch(redraw) {
        sketch.resetSketch();
        if (redraw && sketch.drawEntireSketch) {
            sketch.drawEntireSketch();
        }
    };

    /**
     * Switches to a certain sketch with the given Id
     */
    function switchToSketch(id) {
        if (isUndefined(sketchManager)) {
            throw new UpdateException("Can not switch sketch with an invalid manager");
        }
        sketch = sketchManager.getSketch(id);
        currentSketchId = id;
    }

    /**
     * Generates a marker that can be used for marking things.
     * 
     * Returns the result as a Command.
     * 
     * @param userCreated
     *            {boolean} True if the user created this marker.
     * @param markerType
     *            {MarkerType} The type that the marker is.
     * @param otherData
     *            {string} Contains other important data.
     * @returns {SrlCommnand}.
     */
    this.createMarker = function createMarker(userCreated, markerType, otherData) {
        var marker = PROTOBUF_UTIL.Marker();
        marker.setType(markerType);
        marker.setOtherData(otherData);

        var command = PROTOBUF_UTIL.SrlCommand();
        command.setCommandType(PROTOBUF_UTIL.CommandType.MARKER);
        command.setIsUserCreated(userCreated);
        command.setCommandData(marker.toArrayBuffer());
        command.setCommandId(generateUUID());
        return command;
    };

    /**
     * Tries to quickly empty the local queue.
     * 
     * Ensures that even with the rapid addition of updates there are no
     * executions that overlap.
     */
    function emptyLocalQueue() {
        if (queuedLocalUpdates.length > 0) {
            if (!executionLock) {
                executionLock = true;
                var nextUpdate = queuedLocalUpdates.removeObjectByIndex(0);
                try {
                    var redraw = executeUpdate(nextUpdate);
                    if (redraw && sketch.drawEntireSketch) {
                        setTimeout(function() {
                            sketch.drawEntireSketch();
                        }, 10);
                    }
                } catch (exception) {
                    executionLock = false;
                    if (onError) onError(exception);
                }
                executionLock = false;
                setTimeout(function() {
                    emptyLocalQueue();
                }, 10);
            } else {
                // we wait and try again when the executionLock is gone
                setTimeout(function() {
                    emptyLocalQueue();
                }, 10);
            }
        }
    }

    /**
     * Executes an update.
     * 
     * Does special handling with redo and undo
     * 
     * @param update
     * @returns {boolean} true if the object needs to be redrawn.
     */
    function executeUpdate(update) {
        update.sketchId = currentSketchId;
        if (update.getCommands().length <= 0) {
            throw new UpdateException("Can not execute an empty update.");
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
            if ((command != PROTOBUF_UTIL.CommandType.REDO && command != PROTOBUF_UTIL.CommandType.UNDO)) {
                // we do a bunch of changing then we call the executeUpdate
                // method again
                var splitDifference = updateList.length - currentUpdateIndex;

                // creates and inserts the first marker [update] -> [marker] ->
                // [unreachable update]
                var startingMarker = localScope.createMarker(false, PROTOBUF_UTIL.getMarkerClass().MarkerType.SPLIT, "" + splitDifference);
                updateList.splice(currentUpdateIndex, 0, PROTOBUF_UTIL.createUpdateFromCommands([ startingMarker ]));

                // creates and inserts the second marker [unreachable update
                // (probably undo or redo)] -> [marker] -> [index out of range]
                var endingMarker = localScope.createMarker(false, PROTOBUF_UTIL.getMarkerClass().MarkerType.SPLIT, "" + (0 - splitDifference));
                updateList.push(PROTOBUF_UTIL.createUpdateFromCommands([ endingMarker ]));

                // reset the information
                inRedoUndoMode = false;
                netCount = 0;
                currentUpdateIndex = updateList.length;
                return executeUpdate(update);
            }
        }
        if (command == PROTOBUF_UTIL.CommandType.REDO) {
            if (netCount >= 0) {
                throw new UndoRedoException("Can't Redo Anymore");
            }
            updateList.push(update);
            currentEndingIndex += 1;
            netCount += 1;
            var redraw = redoUpdate(updateList[currentUpdateIndex]);
            currentUpdateIndex += 1;
            return redraw;
        } else if (command == PROTOBUF_UTIL.CommandType.UNDO) {
            if (currentUpdateIndex <= 0) {
                throw new UndoRedoException("Can't Undo Anymore");
            }
            if (!inRedoUndoMode) {
                netCount = 0;
                inRedoUndoMode = true;
            }
            netCount -= 1;
            updateList.push(update);
            currentEndingIndex += 1;
            var redraw = undoUpdate(updateList[currentUpdateIndex - 1]);
            currentUpdateIndex -= 1;
            return redraw;
        } else {
            // A normal update
            currentEndingIndex += 1;
            updateList.push(update);
            var redraw = redoUpdate(update);
            currentUpdateIndex += 1;
            return redraw;
        }
    }

    /**
     * If the update is a marker than it will skip that parts that can not be
     * reached.
     * 
     * @param update
     *            {SrlUpdate}
     * @returns true if the sketch needs to be redrawn.
     */
    function redoUpdate(update) {
        var command = update.getCommands()[0];
        if (command.commandType == PROTOBUF_UTIL.CommandType.MARKER) {
            var marker = PROTOBUF_UTIL.decodeProtobuf(command.commandData, PROTOBUF_UTIL.getMarkerClass());
            if (marker.type == PROTOBUF_UTIL.getMarkerClass().MarkerType.SPLIT) {
                var tempIndex = currentUpdateIndex;
                currentUpdateIndex += parseInt(marker.otherData) + 1;
                if (currentUpdateIndex > updateList.length) {
                    amountToSkip = parseInt(marker.otherData) + 1;
                    skippingMarkerMode = true;
                }
            } else if (marker.type == PROTOBUF_UTIL.getMarkerClass().MarkerType.SUBMISSION) {
                if (currentUpdateIndex > lastSubmissionPointer) {
                    lastSubmissionPointer = currentUpdateIndex;
                }
            }
            return false;
        } else if (command.commandType == PROTOBUF_UTIL.CommandType.CREATE_SKETCH) {
            command.decodedData = currentSketchId;
            var id = PROTOBUF_UTIL.decodeProtobuf(command.commandData, PROTOBUF_UTIL.getIdChainClass()).idChain[0];
            if (!isUndefined(sketch) && sketch.id != id && !isUndefined(sketchManager)) {
                sketchManager.createSketch(id, this);
            }
            switchToSketch(id);
            return true;
        } else if (command.commandType == PROTOBUF_UTIL.CommandType.SWITCH_SKETCH) {
            command.decodedData = currentSketchId;
            var id = PROTOBUF_UTIL.decodeProtobuf(command.commandData, PROTOBUF_UTIL.getIdChainClass()).idChain[0];
            switchToSketch(id);
            return true;
        }
        return update.redo();
    }

    /**
     * If the update is a marker than it will skip that parts that can not be
     * reached
     * 
     * @param update
     *            {SrlUpdate}
     * @returns true if the sketch needs to be redrawn.
     */
    function undoUpdate(update) {

        var command = update.getCommands()[0];
        if (command.commandType == PROTOBUF_UTIL.CommandType.MARKER) {
            var marker = PROTOBUF_UTIL.decodeProtobuf(command.commandData, PROTOBUF_UTIL.getMarkerClass());
            if (marker.type == PROTOBUF_UTIL.getMarkerClass().MarkerType.SPLIT) {
                currentUpdateIndex += parseInt(marker.otherData) - 1;
            } else {
                throw new UpdateException("You can't undo that (something went wrong)");
            }
            return true;
        } else if (command.commandType == PROTOBUF_UTIL.CommandType.CREATE_SKETCH) {
            var id = PROTOBUF_UTIL.decodeProtobuf(command.commandData, PROTOBUF_UTIL.getIdChainClass()).idChain[0];
            if (!isUndefined(sketchManager)) {
                sketchManager.deleteSketch(id);
            }
            switchToSketch(command.decodedData);
            return true;
        } else if (command.commandType == PROTOBUF_UTIL.CommandType.SWITCH_SKETCH) {
            switchToSketch(command.decodedData);
            return true;
        }
        return update.undo();
    }

    /**
     * Returns a copy of the updateList for the purpose of not being edited
     * while in use.
     * 
     * this is a delayed method to prevent javascript from freezing the browser.
     * 
     * @param callback
     *            {Function}
     */
    this.getCleanUpdateList = function(callback) {
        var index = 0;
        var maxIndex = updateList.length;
        var newList = new Array();
        // for local scoping
        var oldList = updateList;
        var intervalHolder = setInterval(function() {
            var startIndex = index;
            while (index < maxIndex && startIndex - index <= 5) {
                var update = oldList[index];
                var newUpdate = PROTOBUF_UTIL.SrlUpdate();
                var newCommandList = new Array();
                for (var i = 0; i < update.commands.length; i++) {
                    var command = update.commands[i];
                    var cleanCommand = PROTOBUF_UTIL.SrlCommand();
                    cleanCommand.commandType = command.commandType;
                    cleanCommand.isUserCreated = command.isUserCreated;
                    cleanCommand.commandData = command.commandData;
                    cleanCommand.commandId = command.commandId;
                    newCommandList.push(cleanCommand);
                }
                newUpdate.updateId = update.updateId;
                newUpdate.time = update.time;
                if (update.commandNumber) newUpdate.commandNumber = update.commandNumber;
                newUpdate.commands = newCommandList;
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

    this.getUpdateList = function(callback) {
        if (callback) {
            callback(updateList);
        }
        return updateList;
    };

    /**
     * @returns the length of the current list.
     */
    this.getListLength = function() {
        return updateList.length;
    };

    /**
     * @returns {boolean} True IFF a submission marker is the last item that was
     *          submitted.
     */
    this.isLastUpdateSubmission = function() {
        if (updateList.length <= 0) {
            return false;
        }
        var update = updateList[updateList.length - 1];
        var commandList = update.getCommands();
        var currentCommand = commandList[0];
        if (currentCommand.commandType == PROTOBUF_UTIL.CommandType.MARKER) {
            var marker = PROTOBUF_UTIL.decodeProtobuf(currentCommand.commandData, PROTOBUF_UTIL.getMarkerClass());
            if (marker.type == PROTOBUF_UTIL.getMarkerClass().MarkerType.SUBMISSION) {
                return true;
            }
        }
        return false;
    };

    /**
     * @returns {boolean} The opposite of isLastUpdateSubmission, except in the
     *          case where updateList.length() is non-positive.
     */
    this.isValidForSubmission = function() {
        if (updateList.length <= 0) {
            return false;
        }
        return !this.isLastUpdateSubmission();
    };

    this.getCurrentPointer = function() {
        return currentUpdateIndex;
    };

    /**
     * This clears any current updates and replaces the list with a new list.
     * 
     * @param list
     *            The list that is will be added to the sketch
     * @param percentBar
     *            The bar that will show these updates. It is called with how
     *            much is left to be completed.
     */
    this.setUpdateList = function(list, percentBar) {
        var initializing = true;
        this.clearUpdates(false);
        var index = 0;
        var maxIndex = list.length;
        var intervalHolder = setInterval(function() {
            var startIndex = index;
            while (index < maxIndex && index - startIndex < 1) {
                if (percentBar) {
                    percentBar.updatePercentBar(index, maxIndex);
                }
                localScope.addUpdate(list[index], false, true);
                index++;
            }
            if (index >= maxIndex) {
                clearInterval(intervalHolder);
                if (percentBar && percentBar.isRunning()) {
                    percentBar.updatePercentBar(1, 1);
                    percentBar.finishWaiting(300);
                }
            }
        }, 20);
    };

    /**
     * creates and adds a redo update to the stack.
     * 
     * @param userCreated
     *            {boolean} true if the userCreated the command false otherwise.
     */
    this.redoAction = function(userCreated) {
        var redoCommand = PROTOBUF_UTIL.createBaseCommand(PROTOBUF_UTIL.CommandType.REDO, userCreated);
        var update = PROTOBUF_UTIL.createUpdateFromCommands([ redoCommand ]);
        this.addUpdate(update, false);
    };

    /**
     * creates and adds a redo update to the stack.
     * 
     * @param userCreated
     *            {boolean} true if the userCreated the command false otherwise.
     */
    this.undoAction = function(userCreated) {
        var undoCommand = PROTOBUF_UTIL.createBaseCommand(PROTOBUF_UTIL.CommandType.UNDO, userCreated);
        var update = PROTOBUF_UTIL.createUpdateFromCommands([ undoCommand ]);
        var tempIndex = currentUpdateIndex;
        this.addUpdate(update, false);
    };
}
