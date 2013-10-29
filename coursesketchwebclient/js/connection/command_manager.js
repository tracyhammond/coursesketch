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

	/**
	 * Adds an update to the updateList
	 *
	 * If the currentUpdateIndex less than the list size then we need to remove all the other updates from the list
	 * and add the newest one on
	 */
	this.addUpdate = function(update, fromRemote) {
		if (currentUpdateIndex < updateList.length) {
			// TODO: pop all others
		}
		currentUpdateIndex +1;
		updateList.push(update);
		if (!fromRemote) {
			queuedUpdates.push(fromRemote);
		} else {
			setTimeout(function() {
				var redraw = update.redo();
				if (redraw && sketch.drawEntireSketch) {
					sketch.drawEntireSketch();
				}
			}.bind(this),10); // Assumes local update are executed before they are created.
		}
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
				redraw = true;
			break;
		}
		return redraw;
	}
}
