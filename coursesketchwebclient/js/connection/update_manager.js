function UpdateManager(sketch, connection, ProtoSrlUpdate, ProtoSrlCommand, ProtoSrlCommandType, Action, sketchBuilder) {
	var sketch = sketch;
	var serverConnection = connection;
	var currentUpdateIndex = 0; // holds a state of updates (for undoing and redoing)
	var sketchProtoBuilder = sketchBuilder;
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
	var Action = Action;

	this.getUpdates = function() {
		return updateList;
	}

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
			console.log("executing! update");
			setTimeout(function() {
				var redraw = update.redo();
				if (redraw && sketch.drawEntireSketch) {
					sketch.drawEntireSketch();
				}
			}.bind(this),10); // Assumes local update are executed before they are created.
		}
	}

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
	}

	/**
	 * Redo the commands in a certain order.
	 *
	 * Returns true if the sketch needs to be redrawn.
	 */
	ProtoSrlUpdate.prototype.redo = function() {
		var redraw = false;
		var commandList = this.getCommands();
		var commandLength = commandList.length;
		for(var i = 0; i < commandLength; i++) {
			if (commandList[i].redo() == true) {
				redraw = true;
			}
			
		}
		return redraw;
	}

	/**
	 * Undoes the commands in reverse order.
	 *
	 * Returns true if the sketch needs to be redrawn.
	 */
	ProtoSrlUpdate.prototype.undo = function() {
		var commandList = this.getCommands();
		var commandLength = commandList.length;
		var redraw = false;
		for(var i = commandLength -1; i >= 0; i++) {
			if (commandList[i].undo() == true)
				redraw = true;
		}
		return redraw;
	}

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
		}
		return "NO_NAME # is: " +this.getCommandType();
	}	
	
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
		console.log(typeof command)
		if (typeof command !== 'number') {
	        throw new Error('You must pass a number to setPlaceType!');
	    }

		switch(command) {
			case this.CommandType.ADD_STROKE:
				if (!this.decodedData) {
					//console.log("Executing " + this.CommandType.ADD_STROKE);
					var stroke = parent.ProtoSrlStroke.decode(this.commandData);
					this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
				}
				sketch.addObject(this.decodedData);
				redraw = true;
			break;
			case this.CommandType.ADD_SHAPE:
				if (!this.decodedData) {
					//console.log("Executing " + this.CommandType.ADD_SHAPE);
					var shape = parent.ProtoSrlShape.decode(this.commandData);
					this.decodedData = SRL_Shape.createFromProtobuf(shape);
				}
				sketch.addObject(this.decodedData);
			break;
			case this.CommandType.REMOVE_OBJECT:
				if (!this.decodedData || !isArray(this.decodedData)) {
					this.decodedData = new Array();
					//console.log("Executing " + this.CommandType.ADD_SHAPE);
					var idChain = parent.IdChain.decode(this.commandData);

					this.decodedData[0] = idChain;
				}
				// holds the decoded data in the second part of the list.
				this.decodedData[1] = sketch.removeSubObjectByIdChain(this.decodedData[0].idChain);
				redraw = true;
			break;
			case this.CommandType.PACKAGE_SHAPE:
				console.log("Executing PACKAGE_SHAPE");
				if (isUndefined(this.decodedData) || (!this.decodedData)) {
					this.decodedData = Action.ActionPackageShape.decode(this.commandData);
				}
				this.decodedData.redo();
			break;
		}
		return redraw;
	}
	
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
			console.log('id ' + shapeId);
			var object = oldContainingObject.removeSubObjectById(shapeId);
			console.log('result');
			console.log(object);
			newContainingObject.addSubObject(object);
		}
	}

	/**
	 * Moves the shapes from the new container to the old container.
	 *
	 * This is a reverse of the process used in redo.
	 */
	Action.ActionPackageShape.undo = function() {
		var oldContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());
		var newContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());

		if (oldContainingObject == newContainingObject)
			return; // done moving to same place.

		for (shapeId in this.shapesToBeContained) {
			var object = oldContainingObject.removeObjectById(shapeId);
			if (newContainerId) {
				newContainingObject.addSubObject(object);
			} else {
				newContainingObject.addObject(object);
			}
		}
	}
}
