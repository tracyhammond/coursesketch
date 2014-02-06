function UpdateManager(inputSketch, connection, ProtoCommandBuilder, onError) {
	var serverConnection = connection;
	var sketch = inputSketch;
	var currentUpdateIndex = 0; // holds a state of updates (for undoing and redoing)
	var currentEndingIndex = 0; // holds the pointer to the end of the list (only really used with markers)
	var skippingMarkerMode = false; // if a marker is executed with an index larger than what we have then we wait.
	var amountToSkip = 0;
	var localScope = this;
	var lastSubmissionPointer = 0;

	/*
	 * Holds the list of updates that are waiting to be sent to the server.
	 *
	 * This list should almost always be near empty.
	 */
	var queuedServerUpdates = new Array();

	/*
	 * Holds the list of updates that are waiting to be executed locally
	 *
	 * This list should almost always be near empty.
	 * there is also an executionLock and a boolean for the queue being empty
	 */
	var queuedLocalUpdates = new Array();
	var executionLock = false;
	var queueEmpty = false;

	var inRedoUndoMode = false;
	var netCount = 0; // this is equal to undo - redo

	/*
	 * Holds the entire list of updates
	 */
	var updateList = new Array();

	/**
	 * Adds an update to the updateList
	 *
	 * If the currentUpdateIndex less than the list size then we need to remove all the other updates from the list
	 * and add the newest one on.
	 * These commands are only executed if the command is fromRemote or execute is true.
	 */
	this.addUpdate = function(update, fromRemote) {
		queuedLocalUpdates.push(update);
		//console.log("adding an update: " + queuedLocalUpdates.length);
		emptyLocalQueue();

		if (isUndefined(fromRemote) || !fromRemote) {
			// we send to the remote server
			queuedServerUpdates.push(update);
			this.emptyQueue();
		}
	};

	/**
	 * Slowly empties the queue for sending messages to the server.
	 */
	this.emptyQueue = function() {
		setTimeout(function() {
			if (queuedServerUpdates.length > 0) {
				var update = queuedServerUpdates.removeObjectByIndex(0);
				var request = serverConnection.createRequestFromUpdate(update, parent.Request.MessageType.RECOGNITION);
				serverConnection.sendRequest(request);
				if (queuedServerUpdates.length > 0) {
					this.emptyQueue(); // recursion! (kind of)
				}
			}
		}.bind(this),10);
	};

	/**
	 * Decodes the data and perserves the bytebuffer for later use
	 */
	function decodeCommandData(commandData, proto) {
		try {commandData.mark();} catch(exception) {}
		var decoded = proto.decode(commandData);
		try {commandData.reset();} catch(exception) {}
		return decoded;
	}

	/**
	 * Clears the current updates.
	 *
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
	 */
	function clearSketch(redraw) {
		sketch.resetSketch();
		if (redraw) {
			sketch.drawEntireSketch();
		}
	};
	this.clearSketch = clearSketch;

	/**
	 * Generates a marker that can be used for marking things.
	 *
	 * Returns the result as a Command.
	 */
	function createMarker(userCreated, markerType, otherData) {
		var marker = new ProtoCommandBuilder.Marker(markerType, otherData);
		return new ProtoCommandBuilder.SrlCommand(ProtoCommandBuilder.CommandType.MARKER, false, marker.toArrayBuffer(), generateUUID());
	}
	this.createMarker = createMarker;

	this.createBaseCommand = serverConnection.createBaseCommand;

	this.createUpdateFromCommands = serverConnection.createUpdateFromCommands;

	/**
	 * Tries to quickly empty the local queue.
	 *
	 * Ensures that even with the rapid addition of updates there are no executions that overlap.
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
				} catch(exception) {
					executionLock = false;
					console.log(exception.stack);
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
		} else {
		}
	}

	/**
	 * Executes an update.
	 *
	 * Does special handling with redo and undo
	 */
	function executeUpdate(update) {
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
			if ((command != ProtoCommandBuilder.CommandType.REDO && command != ProtoCommandBuilder.CommandType.UNDO)) {
				// we do a bunch of changing then we call the executeUpdate method again
				var splitDifference = updateList.length - currentUpdateIndex;

				// creates and inserts the first marker [update] -> [marker] -> [unreachable update]
				var startingMarker = createMarker(false, ProtoCommandBuilder.Marker.MarkerType.SPLIT, splitDifference);
				updateList.splice(currentUpdateIndex, 0, serverConnection.createUpdateFromCommands([startingMarker]));

				// creates and inserts the second marker [unreachable update (probably undo or redo)] -> [marker] -> [index out of range]
				var endingMarker = createMarker(false, ProtoCommandBuilder.Marker.MarkerType.SPLIT, 0 - splitDifference);
				updateList.push(serverConnection.createUpdateFromCommands([endingMarker]));

				// reset the information
				inRedoUndoMode = false;
				netCount = 0;
				currentUpdateIndex = updateList.length;
				return executeUpdate(update);
			}
		}
		if (command == ProtoCommandBuilder.CommandType.REDO) {
			if (netCount >= 0) {
				throw "Can't Redo Anymore";
			}
			updateList.push(update);
			currentEndingIndex += 1;
			netCount += 1;
			var redraw = redoUpdate(updateList[currentUpdateIndex]);
			currentUpdateIndex += 1;
			return redraw;
		} else if (command == ProtoCommandBuilder.CommandType.UNDO) {
			if (currentUpdateIndex <= 0) {
				throw "Can't Undo Anymore";
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
	 * If the update is a marker than it will skip that parts that can not be reached.
	 */
	function redoUpdate(update) {
		var command = update.getCommands()[0];
		if (command.commandType == ProtoCommandBuilder.CommandType.MARKER) {
			var marker = decodeCommandData(command.commandData, ProtoCommandBuilder.Marker);
			if (marker.type == ProtoCommandBuilder.Marker.MarkerType.SPLIT) {
				var tempIndex = currentUpdateIndex;
				currentUpdateIndex += parseInt(marker.otherData) + 1;
				if (currentUpdateIndex > updateList.length) {
					amountToSkip = parseInt(marker.otherData) + 1;
					skippingMarkerMode = true;
				}
			} else if (marker.type == ProtoCommandBuilder.Marker.MarkerType.SUBMISSION) {
				if (currentUpdateIndex > lastSubmissionPointer) {
					lastSubmissionPointer = currentUpdateIndex;
					//console.log("Updated submission pointer: " + lastSubmissionPointer);
				}
			} else if (marker.type == ProtoCommandBuilder.Marker.MarkerType.CLEAR) {
				clearSketch(true);
			}
			return false;
		} else {
			return update.redo();
		}
	}

	/**
	 * If the update is a marker than it will skip that parts that can not be reached
	 */
	function undoUpdate(update) {
		console.log("UNDOING AN UPDATE!" + update);
		var command = update.getCommands()[0];
		if (command.commandType == ProtoCommandBuilder.CommandType.MARKER) {
			var marker = decodeCommandData(command.commandData, ProtoCommandBuilder.Marker);
			if (marker.type == ProtoCommandBuilder.Marker.MarkerType.SPLIT) {
				currentUpdateIndex += parseInt(marker.otherData) - 1;
			} else {
				throw "You can't undo that (something went wrong)";
			}
			return false;
		} else {
			return update.undo();
		}
	}

	/**
	 * Returns a copy of the updateList for the purpose of not being edited while in use.
	 *
	 * this is a delayed method to prevent javascript from freezing the browser.
	 */
	this.getCleanUpdateList = function(callback) {
		var index = 0;
		var maxIndex = updateList.length;
		var newList = new Array();
		var oldList = updateList; // for local scoping
		var intervalHolder = setInterval(function() {
			var startIndex = index;
			while (index < maxIndex && startIndex - index <= 5) {
				var update = oldList[index];
				var newUpdate = new ProtoSrlUpdate();
				var newCommandList = new Array();
				for (var i = 0; i < update.commands.length; i++) {
					var command = update.commands[i];
					var cleanCommand = new ProtoSrlCommand();
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
	 * Returns true IFF a submission marker is the last item that was submitted.
	 */
	this.isLastUpdateSubmission = function() {
		if (updateList.length <= 0) {
			return false;
		}
		var update = updateList[updateList.length -1];
		var commandList = update.getCommands();
		if (commandList.length <= 0) {
			return false;
		}
		var currentCommand = commandList[0];
		if (currentCommand.commandType == ProtoCommandBuilder.CommandType.MARKER) {
			var marker = decodeCommandData(currentCommand.commandData, ProtoCommandBuilder.Marker);
			if (marker.type == ProtoCommandBuilder.Marker.MarkerType.SUBMISSION) {
				return true;
			}
		}
		return false;
	};
	
	/**
	 * Returns true IFF a submission marker is the last item that was submitted.
	 */
	this.isValidForSubmission = function() {
		if (updateList.length <= 0) {
			return false;
		}
		var update = updateList[updateList.length -1];
		var commandList = update.getCommands();
		if (commandList.length <= 0) {
			return true;
		}
		var currentCommand = commandList[0];
		if (currentCommand.commandType == ProtoCommandBuilder.CommandType.MARKER) {
			var marker = decodeCommandData(currentCommand.commandData, ProtoCommandBuilder.Marker);
			if (marker.type == ProtoCommandBuilder.Marker.MarkerType.SUBMISSION) {
				return false;
			}
		}
		return true;
	};

	this.getCurrentPointer = function() {
		return currentUpdateIndex;
	};

	/**
	 * This clears any current updates and replaces the list with a new list.
	 */

	this.setUpdateList = function(list) {
		initializing = true;
		this.clearUpdates(false);
		var index = 0;
		var maxIndex = list.length;
		var intervalHolder = setInterval(function() {
			var startIndex = index;
			while (index < maxIndex && index - startIndex < 1) {
				localScope.addUpdate(list[index], false, true);
				index++;
			}
			if (index >= maxIndex) {
				clearInterval(intervalHolder);
			}
		}, 20);
	};

	/**
	 * creates and adds a redo update to the stack.
	 */
	this.redoAction = function() {
		var redoCommand = new ProtoSrlCommand(ProtoSrlCommandType.REDO, new Date().getTime());
		var update = serverConnection.createUpdateFromCommands([redoCommand]);
		var tempIndex = currentUpdateIndex;
		this.addUpdate(update, false);
	};

	/**
	 * creates and adds a redo update to the stack.
	 */
	this.undoAction = function() {
		var undoCommand = new ProtoSrlCommand(ProtoSrlCommandType.UNDO, new Date().getTime());
		var update = serverConnection.createUpdateFromCommands([undoCommand]);
		var tempIndex = currentUpdateIndex;
		this.addUpdate(update, false);
	};

	/******************************************
	 * METHODS FOR THE REDO AND UNDO ARE BELOW.
	 *
	 * Each method is a prototype of the command or the update
	 *****************************************/
	(function(ProtoSrlUpdate, ProtoSrlCommand, Action) {

		/**
		 * Redo the commands in a certain order.
		 *
		 * Returns true if the sketch needs to be redrawn.
		 */
		ProtoSrlUpdate.prototype.redo = function() {
			var redraw = false;
			var commandList = this.getCommands();
			var commandLength = commandList.length;
			for (var i = 0; i < commandLength; i++) {
				if (commandList[i].redo() == true) redraw = true;
			}
			return redraw;
		};

		/**
		 * Undoes the commands in reverse order.
		 *
		 * Returns true if the sketch needs to be redrawn.
		 */
		ProtoSrlUpdate.prototype.undo = function() {
			var commandList = this.getCommands();
			var commandLength = commandList.length;
			var redraw = false;
			for(var i = commandLength -1; i >= 0; i--) {
				if (commandList[i].undo() == true) redraw = true;
			}
			return redraw;
		};

		/**
		 * returns the human readable name of the given command type
		 */
		ProtoSrlCommand.prototype.getCommandTypeName = function() {
			switch(this.getCommandType()) {
				case this.CommandType.ADD_STROKE:
					return 'ADD_STROKE';
				case this.CommandType.ADD_SHAPE:
					return 'ADD_SHAPE';
				case this.CommandType.PACKAGE_SHAPE:
					return 'PACKAGE_SHAPE';
				case this.CommandType.ADD_SUBSHAPE:
					return 'ADD_SUBSHAPE';
				case this.CommandType.REMOVE_OBJECT:
					return 'REMOVE_OBJECT';
				case this.CommandType.ADD_SUBSHAPE:
					return 'ADD_SUBSHAPE';
				case this.CommandType.ASSIGN_ATTRIBUTE:
					return 'ASSIGN_ATTRIBUTE';
				case this.CommandType.REMOVE_ATTRIBUTE:
					return 'REMOVE_ATTRIBUTE';
				case this.CommandType.FORCE_INTERPRETATION:
					return 'FORCE_INTERPRETATION';
				case this.CommandType.UNDO:
					return 'UNDO';
				case this.CommandType.REDO:
					return 'REDO';
				case this.CommandType.REWRITE:
					return 'REWRITE';
				case this.CommandType.CLEAR_STACK:
					return 'CLEAR_STACK';
				case this.CommandType.SYNC:
					return 'SYNC';
				case this.CommandType.MARKER:
					return 'MARKER';
			}
			return "NO_NAME # is: " +this.getCommandType();
		};

		ProtoSrlCommand.prototype.CommandType = ProtoSrlCommandType; // TODO: figure out how to get static properties from instance.
		ProtoSrlCommand.prototype.decodedData = false;

		/**
		 * Executes a command.
		 *
		 * Returns true if the sketch needs to be redrawn.
		 */
		ProtoSrlCommand.prototype.redo = function() {
			var redraw = false;
			var command = this.getCommandType();
			switch (command) {
				case this.CommandType.ADD_STROKE:
					if (!this.decodedData) {
						var stroke = decodeCommandData(this.commandData, parent.ProtoSrlStroke);
						this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
					}
					sketch.addObject(this.decodedData);
					redraw = true;
				break;
				case this.CommandType.ADD_SHAPE:
					if (!this.decodedData) {
						var shape = decodeCommandData(this.commandData, parent.ProtoSrlShape);
						this.decodedData = SRL_Shape.createFromProtobuf(shape);
					}
					sketch.addObject(this.decodedData);
				break;
				case this.CommandType.REMOVE_OBJECT:
					if (!this.decodedData || !isArray(this.decodedData)) {
						this.decodedData = new Array();
						var idChain = decodeCommandData(this.commandData, parent.IdChain);
						this.decodedData[0] = idChain;
					}
					this.decodedData[1] = sketch.removeSubObjectByIdChain(this.decodedData[0].idChain);
					redraw = true;
				break;
				case this.CommandType.PACKAGE_SHAPE:
					if (isUndefined(this.decodedData) || (!this.decodedData)) {
						this.decodedData = decodeCommandData(this.commandData, Action.ActionPackageShape);
					}
					this.decodedData.redo();
				break;
			}
			return redraw;
		};

		/**
		 * Executes a command.
		 *
		 * Returns true if the sketch needs to be redrawn.
		 */
		ProtoSrlCommand.prototype.undo = function() {
			var redraw = false;
			var command = this.getCommandType();
			switch (command) {
				case this.CommandType.ADD_STROKE:
					if (!this.decodedData) {
						var stroke = decodeCommandData(this.commandData, parent.ProtoSrlStroke);
						this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
					}
					sketch.removeSubObjectById(this.decodedData.getId());
					redraw = true;
				break;
				case this.CommandType.ADD_SHAPE:
					if (!this.decodedData) {
						var shape = decodeCommandData(this.commandData, parent.ProtoSrlShape);
						this.decodedData = SRL_Shape.createFromProtobuf(shape);
					}
					sketch.removeSubObjectById(this.decodedData.getId());
					redraw = true;
					sketch.addObject(this.decodedData);
				break;
				case this.CommandType.REMOVE_OBJECT:
					if (!this.decodedData || !isArray(this.decodedData)) {
						this.decodedData = new Array();
						var idChain = decodeCommandData(this.commandData, parent.IdChain);
						this.decodedData[0] = idChain;
					}
					//sketch.addObject(this.decodedData);
					//this.decodedData[1];
					throw "REMOVE_OBJECT undo not supported";
					redraw = true;
				break;
				case this.CommandType.PACKAGE_SHAPE:
					if (isUndefined(this.decodedData) || (!this.decodedData)) {
						this.decodedData = decodeCommandData(this.commandData, Action.ActionPackageShape);
					}
					console.log(this.decodedData);
					this.decodedData.undo();
				break;
			}
			return redraw;
		};

		/****
		 * MARKER SPECIFIC UPDATES.
		 ***/

		/**
		 * returns the human readable name of the given marker type
		 */
		Action.Marker.prototype.getCommandTypeName = function() {
			switch(this.getType()) {
				case this.MarkerType.SUBMISSION:
					return 'SUBMISSION';
				case this.CommandType.FEEDBACK:
					return 'FEEDBACK';
				case this.CommandType.SAVE:
					return 'SAVE';
				case this.CommandType.SPLIT:
					return 'SPLIT';
				case this.CommandType.CLEAR:
					return 'CLEAR';
			}
			return "NO_NAME # is: " +this.getCommandType();
		};
		
		/*********
		 * Specific commands and their actions.
		 *******/

		/**
		 * Moves the shapes from the old container to the new container.
		 */
		Action.ActionPackageShape.prototype.redo = function() {
			var oldContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());
			var newContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());

			if (oldContainingObject == newContainingObject)
				return; // done moving to same place.
			for (var shapeIndex = 0; shapeIndex < this.shapesToBeContained.length; shapeIndex++) {
				var shapeId = this.shapesToBeContained[shapeIndex];
				var object = oldContainingObject.removeSubObjectById(shapeId);
				newContainingObject.addSubObject(object);
			}
		};

		/**
		 * Moves the shapes from the new container to the old container.
		 *
		 * This is a reverse of the process used in redo.
		 */
		Action.ActionPackageShape.prototype.undo = function() {
			var oldContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());
			var newContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());

			if (oldContainingObject == newContainingObject)
				return; // done moving to same place.

			for (shapeId in this.shapesToBeContained) {
				var object = oldContainingObject.removeSubObjectById(shapeId);
				if (newContainerId) {
					newContainingObject.addSubObject(object);
				} else {
					newContainingObject.addObject(object);
				}
			}
		};

	})(ProtoCommandBuilder.SrlUpdate, ProtoCommandBuilder.SrlCommand, ProtoCommandBuilder);
}