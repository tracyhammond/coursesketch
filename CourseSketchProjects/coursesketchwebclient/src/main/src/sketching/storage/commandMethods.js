/**
 * Adds a couple of really useful methods to the commands. depends on
 * /src/utilities/connection/protobufInclude.html
 */
(function() {
    function CommandException(message) {
        this.name = "CommandException";
        this.setMessage(message);
        this.message = "";
        this.htmlMessage = "";
    }

    CommandException.prototype = BaseException;

    var ProtoSrlUpdate = Object.getPrototypeOf(PROTOBUF_UTIL.SrlUpdate());
    var ProtoSrlCommand = Object.getPrototypeOf(PROTOBUF_UTIL.SrlCommand());

    PROTOBUF_UTIL.getSrlCommandClass().prototype.sketchId = undefined;
    PROTOBUF_UTIL.getSrlUpdateClass().prototype.sketchId = undefined;
    /**
     * @Method Calls redo on an {@link SrlCommand} list in the order they are
     *         added to the list.
     * 
     * @returns {boolean} true if the sketch needs to be redrawn, false
     *          otherwise.
     */
    ProtoSrlUpdate.redo = function() {
        var redraw = false;
        var commandList = this.getCommands();
        var commandLength = commandList.length;
        for (var i = 0; i < commandLength; i++) {
            commandList[i].sketchId = this.sketchId;
            if (commandList[i].redo() == true) redraw = true;
        }
        return redraw;
    };

    /**
     * @Method Calls undo on an {@link SrlCommand} list in the reverse of the
     *         order they are added to the list
     * 
     * @returns {boolean} true if the sketch needs to be redrawn, false
     *          otherwise.
     */
    ProtoSrlUpdate.undo = function() {
        var commandList = this.getCommands();
        var commandLength = commandList.length;
        var redraw = false;
        for (var i = commandLength - 1; i >= 0; i--) {
            commandList[i].sketchId = this.sketchId;
            if (commandList[i].undo() == true) redraw = true;
        }
        return redraw;
    };

    /**
     * @Method
     * @returns The human readable name of the given command type.
     */
    ProtoSrlCommand.getCommandTypeName = function() {
        var commandType = this.getCommandType();
        for ( var type in PROTOBUF_UTIL.CommandType) {
            if (PROTOBUF_UTIL.CommandType[type] == commandType) {
                return '' + type;
            }
        }
        throw new PROTOBUF_UTIL.ProtobufException("The assigned type (" + commandType + ") is not a value for enum CommandType");
    };

    ProtoSrlCommand.decodedData = false;

    ProtoSrlCommand.redo = function() {
        return this["redo" + this.getCommandType()]();
    };

    ProtoSrlCommand.undo = function() {
        return this["undo" + this.getCommandType()]();
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     */
    PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod = function(commandType, func) {
        if (isUndefined(commandType)) {
            throw new CommandException("The input commandType can not be undefined");
        }
        if (!isUndefined(ProtoSrlCommand["redo" + commandType])) {
            throw new CommandException("Method is already defined");
        }
        ProtoSrlCommand["redo" + commandType] = func;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     */
    PROTOBUF_UTIL.getSrlCommandClass().removeRedoMethod = function(commandType) {
        if (isUndefined(commandType)) {
            throw new CommandException("The input commandType can not be undefined");
        }
        if (isUndefined(ProtoSrlCommand["redo" + commandType])) {
            throw new CommandException("Method does not exist");
        }
        ProtoSrlCommand["redo" + commandType] = undefined;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     */
    PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod = function(commandType, func) {
        if (isUndefined(commandType)) {
            throw new CommandException("The input commandType can not be undefined");
        }
        if (!isUndefined(ProtoSrlCommand["undo" + commandType])) {
            throw new CommandException("Method is already defined");
        }
        ProtoSrlCommand["undo" + commandType] = func;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     */
    PROTOBUF_UTIL.getSrlCommandClass().removeUndoMethod = function(commandType) {
        if (isUndefined(commandType)) {
            throw new CommandException("The input commandType can not be undefined");
        }
        if (isUndefined(ProtoSrlCommand["undo" + commandType])) {
            throw new CommandException("Method does not exist");
        }
        ProtoSrlCommand["undo" + commandType] = undefined;
    };
})();
