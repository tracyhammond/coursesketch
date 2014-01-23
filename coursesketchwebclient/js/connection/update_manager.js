function UpdateManager(sketch, connection, ProtoSrlUpdate, ProtoSrlCommand, ProtoSrlCommandType, Action) {
	var serverConnection = connection;
	var currentUpdateIndex = 0; // holds a state of updates (for undoing and redoing)
	var localScope = this;
	/*
	 * Holds the list of updates that are waiting to be sent to the server.
	 *
	 * This list should almost always be near empty.
	 */
	var queuedUpdates = [];

	/*
	 * Holds the entire list of updates
	 */
	var updateList = [];
	var inUndoMode = false;

	this.getUpdates = function(callback) {
		if (callback) {
			callback(updateList);
		}
		return updateList;
	};

	/**
	 * Adds an update to the updateList
	 *
	 * If the currentUpdateIndex less than the list size then we need to remove all the other updates from the list
	 * and add the newest one on.
	 * These commands are only executed if the command is fromRemote or execute is true.
	 */
	this.addUpdate = function(update, fromRemote, execute) {
		if (currentUpdateIndex < updateList.length) {
			// TODO: pop all others
		}
		currentUpdateIndex++;
		updateList.push(update);
		if (!fromRemote) {
			queuedUpdates.push(update);
			this.emptyQueue();
		}
		if (fromRemote || execute) {
			setTimeout(function() {
				var redraw = update.redo();
				if (redraw && sketch.drawEntireSketch) {
					sketch.drawEntireSketch();
				}
			}.bind(this),10); // Assumes local update are executed before they are created.
		}
	};

	/**
	 * Slowly empties the queue for sending messages to the server.
	 */
	this.emptyQueue = function() {
		setTimeout(function() {
			if (queuedUpdates.length > 0) {
				var update = queuedUpdates.removeObjectByIndex(0);
				var request = serverConnection.createRequestFromUpdate(update, parent.Request.MessageType.RECOGNITION);
				serverConnection.sendRequest(request);
				if (queuedUpdates.length > 0) {
					this.emptyQueue(); // recursion!
				}
			}
		}.bind(this),10);
	};

	/**
	 * Returns a copy of the updateList for the purpose of not being edited while in use.
	 *
	 * this is a delayed method to prevent javascript from freezing the browser.
	 */
	this.getUpdateList = function(callback) {
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
	}

	/**
	 * This clears any current updates and replaces the list with a new list
	 */
	this.setUpdateList = function(list) {
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
	}

	this.clearUpdates = function(redraw) {
		currentUpdateIndex = 0;
		updateList.length = 0;
		sketch.resetSketch();
		if (redraw) {
			sketch.drawEntireSketch();
		}
	}

	/**
	 * creates and adds a redo update to the stack.
	 */
	this.redoUpdate = function() {
		var redoCommand = new ProtoSrlCommand(ProtoSrlCommandType.REDO, new Date().getTime());
		var udate = createUpdateFromCommands([redoCommand]);
		var tempIndex = currentUpdateIndex;
		this.addUpdate(update, false, false);
		currentUpdateIndex = tempIndex + 1;
		updateList[currentUpdateIndex].undo();
	}

	/**
	 * creates and adds a redo update to the stack.
	 */
	this.undoUpdate = function() {
		var undoCommand = new ProtoSrlCommand(ProtoSrlCommandType.REDO, new Date().getTime());
		var udate = createUpdateFromCommands([undoCommand]);
		var tempIndex = currentUpdateIndex;
		this.addUpdate(update, false, false);
		currentUpdateIndex = tempIndex - 1;
		updateList[currentUpdateIndex].undo();
	}

	/**
	 * Decodes the data and perserves the bytebuffer for later use
	 */
	function decodeCommandData(commandData, proto) {
		commandData.mark();
		var decoded = proto.decode(commandData);
		commandData.reset();
		return decoded;
	}
}
