function Question() {
    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    }

    this.saveData = function(event) {
        var questionProto = CourseSketch.PROTOBUF_UTIL.SrlQuestion();

        // Populate data in the proto object
        questionProto.id = generateUUID();
        questionProto.setQuestionText(this.shadowRoot.querySelector('#text').value);

        // If the textbox does not have an id, then a command has not been created for the textbox
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_QUESTION,true);
        }
        this.command.setCommandData(questionProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        var callback = this.getFinishedCallback();
        if(!isUndefined(callback)) {
            callback(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
        }
    }

    /**
     * @param textBoxProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(questionProto) {
        if (isUndefined(shadowRoot) || isUndefined(questionProto)) {
            return;
        }
        this.shadowRoot.querySelector("#text").value = questionProto.getQuestionText();
    }

    /**
     * @return finishedCallback {function} is the callback set at implementation.
     * The callback can be called immediately using .getFinishedCallback()(argument) with argument being optional
     */
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };

    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };
}
Question.prototype = Object.create(HTMLDialogElement.prototype);
Question.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
Question.prototype.createdCommand = undefined;
