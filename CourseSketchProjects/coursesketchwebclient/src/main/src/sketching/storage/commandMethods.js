validateFirstGlobalRun(document.currentScript);

/**
 * Adds a couple of really useful methods to the commands. depends on
 * /src/utilities/connection/protobufInclude.html
 */
(function() {
    /**
     * @class CommandException
     * @extends BaseException
     */
    function CommandException(message, cause) {
        this.name = 'CommandException';
        this.setMessage(message);
        this.message = '';
        this.setCause(cause);
        this.createStackTrace();
    }
    CommandException.prototype = new BaseException();

    var ProtoSrlUpdate = Object.getPrototypeOf(CourseSketch.PROTOBUF_UTIL.SrlUpdate());
    var ProtoSrlCommand = Object.getPrototypeOf(CourseSketch.PROTOBUF_UTIL.SrlCommand());

    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().prototype.sketchId = undefined;
    CourseSketch.PROTOBUF_UTIL.getSrlUpdateClass().prototype.sketchId = undefined;

    // these functions should not be created more than once in the entirety of the program.
    if (!isUndefined(ProtoSrlCommand.getLocalSketchSurface)) {
        return;
    }

    /**
     * Calls redo on an {@link SrlCommand} list in the order they are added to the list.
     *
     * @returns {Boolean} true if the sketch needs to be redrawn, false
     *          otherwise.
     *
     * @memberof SrlUpdate
     * @function redo
     * @instance
     */
    CourseSketch.PROTOBUF_UTIL.getSrlUpdateClass().prototype.redo = function() {
        var redraw = false;
        var commandList = this.getCommands();
        var commandLength = commandList.length;
        var getLocalSketchSurface = function() {
            return this.sketchManager.getCurrentSketch();
        }.bind(this);
        for (var i = 0; i < commandLength; i++) {
            var command = commandList[i];
            // the command needs to know what sketch object to act upon.
            command.getLocalSketchSurface = getLocalSketchSurface;
            if (command.redo() === true) {
                redraw = true;
            }
        }
        return redraw;
    };

    /**
     *
     * Calls undo on an {@link SrlCommand} list in the reverse of the order they are added to the list
     *
     * <b>Note</b> that we do not add the methods we added in redo.
     * This is because we assert that you can not undo something until it has been redone first.  So the methods already exist.
     * @returns {Boolean} true if the sketch needs to be redrawn, false otherwise.
     *
     * @memberof SrlUpdate
     * @function undo
     * @instance
     */
    CourseSketch.PROTOBUF_UTIL.getSrlUpdateClass().prototype.undo = function() {
        var commandList = this.getCommands();
        var commandLength = commandList.length;
        var redraw = false;
        for (var i = commandLength - 1; i >= 0; i--) {
            commandList[i].sketchId = this.sketchId;
            if (commandList[i].undo() === true) {
                redraw = true;
            }
        }
        return redraw;
    };

    /**
     * @memberof SrlCommand
     * @function getCommandTypeName
     * @instance
     * @returns {String} The human readable name of the given command type.
     */
    ProtoSrlCommand.getCommandTypeName = function() {
        var commandType = this.getCommandType();
        for (var type in CourseSketch.PROTOBUF_UTIL.CommandType) {
            if (CourseSketch.PROTOBUF_UTIL.CommandType[type] === commandType) {
                return '' + type;
            }
        }
        throw new CourseSketch.PROTOBUF_UTIL.ProtobufException('The assigned type (' + commandType + ') is not a value for enum CommandType');
    };

    ProtoSrlCommand.decodedData = false;

    /**
     * Redoes the specific command.  How the command is redone depends on the command type.
     * @memberof SrlCommand
     * @function redo
     * @instance
     * @returns {Boolean} true if redoing the command requires a redraw of the screen.
     */
    ProtoSrlCommand.redo = function() {
        var redoFunc = this['redo' + this.getCommandType()];
        if (isUndefined(redoFunc)) {
            throw (this.getCommandTypeName() + ' is not defined as a redo function');
        }
        return redoFunc.bind(this)();
    };

    /**
     * Undoes the specific command.  How the command is undone depends on the command type.
     * @memberof SrlCommand
     * @function undo
     * @instance
     * @returns {Boolean} true if undoing the command requires a redraw of the screen.
     */
    ProtoSrlCommand.undo = function() {
        var undoFunc = this['undo' + this.getCommandType()];
        if (isUndefined(undoFunc)) {
            throw (this.getCommandTypeName() + ' is not defined as an undo function');
        }
        return undoFunc.bind(this)();
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     * @memberof SrlCommand
     * @static
     * @function addRedoMethod
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod = function(commandType, func) {
        if (isUndefined(commandType)) {
            throw new CommandException('The input commandType can not be undefined');
        }
        if (!isUndefined(ProtoSrlCommand['redo' + commandType])) {
            throw new CommandException('Method is already defined');
        }
        ProtoSrlCommand['redo' + commandType] = func;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     * @memberof SrlCommand
     * @static
     * @function removeRedoMethod
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().removeRedoMethod = function(commandType) {
        if (isUndefined(commandType)) {
            throw new CommandException('The input commandType can not be undefined');
        }
        if (isUndefined(ProtoSrlCommand['redo' + commandType])) {
            throw new CommandException('Method does not exist');
        }
        ProtoSrlCommand['redo' + commandType] = undefined;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     * @memberof SrlCommand
     * @static
     * @function addUndoMethod
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod = function(commandType, func) {
        if (isUndefined(commandType)) {
            throw new CommandException('The input commandType can not be undefined');
        }
        if (!isUndefined(ProtoSrlCommand['undo' + commandType])) {
            throw new CommandException('Method is already defined');
        }
        ProtoSrlCommand['undo' + commandType] = func;
    };

    /**
     * Allows one to dynamically add and remove methods to the command type.
     * @memberof SrlCommand
     * @static
     * @function removeUndoMethod
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().removeUndoMethod = function(commandType) {
        if (isUndefined(commandType)) {
            throw new CommandException('The input commandType can not be undefined');
        }
        if (isUndefined(ProtoSrlCommand['undo' + commandType])) {
            throw new CommandException('Method does not exist');
        }
        ProtoSrlCommand['undo' + commandType] = undefined;
    };
})();
