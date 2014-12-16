/**
 * Represents a custom image element that can have data saved/loaded to/from protobuf.
 */
function ImageBox() {
    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    }

    this.setSrc = function(src) {
        this.shadowRoot.querySelector(".image").src = src;
    }

    /**
     * Saves the embedded HTML element to a protobuf object. Calls finished callback when done.
     *
     * @param event event that triggered this function
     * @return the created protobuf object
     */
    this.saveData = function(event) {
        var imageProto = CourseSketch.PROTOBUF_UTIL.Image();

        // Populate data in the proto object
        imageProto.src = this.shadowRoot.querySelector(".image").src;

        // If the image does not have an id, then a command has not been created for the image
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_IMAGE,true);
        }
        this.command.setCommandData(imageProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        var callback = this.getFinishedCallback();
        if (!isUndefined(callback)) {
            callback(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
        }
        return imageProto;
    }

    /**
     * @param imageProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(imageProto) {
        if (isUndefined(shadowRoot) || isUndefined(imageProto)) {
            return;
        }
        this.shadowRoot.querySelector(".image").src = imageProto.src;
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
ImageBox.prototype = Object.create(HTMLDialogElement.prototype);
ImageBox.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
ImageBox.prototype.createdCommand = undefined;
