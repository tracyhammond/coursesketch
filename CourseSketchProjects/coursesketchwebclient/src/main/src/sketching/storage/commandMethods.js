/**
 * Adds a couple of really useful methods to the commands. depends on
 * /src/utilities/includes/protobufInclude.html
 */
(function() {
    var ProtoSrlUpdate = Object.getPrototypeOf(PROTOBUF_UTIL.SrlUpdate());
    var ProtoSrlCommand = Object.getPrototypeOf(PROTOBUF_UTIL.SrlCommand());

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
        for (var type in PROTOBUF_UTIL.CommandType) {
            if (PROTOBUF_UTIL.CommandType[type] == commandType) {
                return '' + type;
            }
        }
        throw new ProtobufException("The assigned type (" + commandType + ") is not a value for enum CommandType");
    };

    ProtoSrlCommand.decodedData = false;

    ProtoSrlCommand.redo = function() {
    };

    ProtoSrlCommand.undo = function() {
    };

})();
