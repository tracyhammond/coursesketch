/**
 * Creates the text box dialog
 * The dialog is moveable and allows the creator to enter text to be displayed
 */
function TextBox() {
    var loadedData = undefined; // Utilized if the element does not exist when loadData() is called
    var shadowRoot = undefined; // Used only to tell if the data is ready to be loaded.

    /**
     * @param textToRead {string} contains the text to be read
     * @param callback {function} is the callback to be run after the text has been spoken
     * This function speaks the text using the meSpeak library
     */
    this.speakText = function(textToRead, callback) {
        meSpeak.speak(textToRead, callback);
    };

    /**
     * This is for making the dialog moveable with the interact.js library
     * It selects the created dialog by class name and makes it draggable with no inertia
     * It also ignores click and drag from textareas and buttons within the dialog
     * The dragging is restricted to the area of the parentNode the dialog is created in
     * NOTE: This code comes from the interact library examples page
     */
    function enableDragging(localScope) {
        interact(localScope.shadowRoot.querySelector(".draggable"))
            .ignoreFrom("textarea, button")
            .draggable({
                onmove: function (event) {
                    var target = event.target,
                        x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                        y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

                    target.style.webkitTransform =
                    target.style.transform =
                        'translate(' + x + 'px, ' + y + 'px)';

                    target.setAttribute('data-x', x);
                    target.setAttribute('data-y', y);
                },

            })
            .inertia(false)
            .restrict({
                drag: localScope.parentNode,
                endOnly: false,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
            });
    }

    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        /**
         * Sets onclick action for the close button
         * If the id 'creatorText' exists (not null) then the editor version is currently active
         * Since editor version is active, the close button saves the data and then removes from the DOM
         * Otherwise the element is merely removed from DOM if it is not in creator mode (in viewing mode)
         * The save must happen before being removed from the DOM and not in the detached callback
         * If the element is removed from the DOM, it does not have height and width values and the values will not save correctly
         */
        localScope.shadowRoot.querySelector("#closeButton").onclick = function(event) {
            if (localScope.shadowRoot.querySelector('#creatorText') != null) {
                if (confirm("You are about to permanently remove this element.")) {
                    localScope.saveData(event);
                    shadowRoot = undefined;
                    loadedData = undefined;
                }
                return;
            }
            shadowRoot = undefined;
            loadedData = undefined;
            localScope.parentNode.removeChild(localScope);
        };

        // Saves data on blur of the element
        if (localScope.shadowRoot.querySelector('textarea') != null) {
            localScope.shadowRoot.querySelector('textarea').onblur = function() {
                localScope.saveData();
            };
        }
        
        // Makes save button save data on creation mode
        saveButton = localScope.shadowRoot.querySelector("#saveButton");
        if (saveButton != null) {
            saveButton.onclick = function() {
                localScope.saveData();
                saveButton.textContent = 'Data saved';
            };
            
            saveButton.onblur = function() {
                saveButton.textContent = 'Save';
            };
        }
        
        // Makes continue button close box on viewing mode
        if (localScope.shadowRoot.querySelector("#continueButton") != null) {
            localScope.shadowRoot.querySelector('#continueButton').onclick = localScope.shadowRoot.querySelector('#closeButton').onclick;
        }

        /**
         * Makes the speak text button speak the text in the creatorText textarea
         * This textarea only exists in creation mode, and the speak text button only exists in creator mode
         */
        if (localScope.shadowRoot.querySelector("#speakText") != null) {
            localScope.shadowRoot.querySelector("#speakText").onclick = function() {
                localScope.speakText(shadowRoot.querySelector("#creatorText").value);
            };
        }
        enableDragging(localScope);

        this.loadData(loadedData); // Loads data if data exists. This should allow for editing of the element after it is created and saved.
    };

    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };

    // Saves Data for the proto message based on the position, height, width, and value of the text box
    this.saveData = function(event) {
        var textBoxProto = CourseSketch.PROTOBUF_UTIL.ActionCreateTextBox();
        textBoxProto.setText(this.shadowRoot.querySelector('#creatorText').value); // Sets Text value for proto message
        var dialog = this.shadowRoot.querySelector('#textBoxDialog');
        var x = "" + dialog.style.left; // Makes sure x is a string for following check function
        var y = "" + dialog.style.top; // Makes sure y is a string for following check function

        // Checks if x or y values has "px" on the end of the string. If so removes the "px" from the string
        if (x.indexOf("px") > 0) {
            x = x.substring(0, x.length - 2);
        }
        if (y.indexOf("px") > 0) {
            y= y.substring(0, y.length - 2);
        }

        // Checks if x or y values are blank strings. This occurs when the values are 0px, so it sets the variables to 0.
        if (x == "" || x == " ") {
            x = 0;
        }
        if (y == "" || y == " ") {
            y= 0;
        }

        // x and y are strings but need to save as Ints
        x = parseInt(x);
        y = parseInt(y);

        // Saves X and Y to proto message. Adds the 'data-x' and 'data-y' from dragging if it exists (if the box was dragged)
        if (dialog.getAttribute('data-x') == null || dialog.getAttribute('data-y') == null) {
            textBoxProto.setX(x);
            textBoxProto.setY(y);
        } else {
            textBoxProto.setX(x + parseInt(dialog.getAttribute('data-x')));
            textBoxProto.setY(y + parseInt(dialog.getAttribute('data-y')));
        }
        textBoxProto.setHeight(parseInt($(dialog).height())); // Sets height for proto message
        textBoxProto.setWidth(parseInt($(dialog).width())); // Sets width for proto message

        // If the textbox does not have an id, then a command has not been created for the textbox
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            if (this.tagName == 'TEXT-BOX-CREATION') {
                this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, true);
            } else if (this.tagName == 'TTS-BOX-CREATION') {
                this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, true);
            } else {
                return;
            }
        }
        this.command.setCommandData(textBoxProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        this.getFinishedCallback()(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
    };

    /**
     * @param textBoxProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(textBoxProto) {
        if (isUndefined(shadowRoot)) {
            loadedData = textBoxProto;
            return;
        }
        if (isUndefined(textBoxProto)) {
            return;
        }
        var node = shadowRoot.querySelector('#creatorText');
        var dialog = shadowRoot.querySelector('#textBoxDialog');

        // If creatorText element does not exist, make the selected node the viewText element
        if (node == null) {
            node = shadowRoot.querySelector('#viewText');
        }
        $(dialog).height(textBoxProto.getHeight()); // Sets dialog height
        $(dialog).width(textBoxProto.getWidth()); // Sets dialog width
        $(node).width(textBoxProto.getWidth()); // Sets node width
        $(node).height(textBoxProto.getHeight() - 16); // Sets node height minus 16px to account for default padding
        $(dialog).offset({ top: textBoxProto.getY(), left: textBoxProto.getX() }); // Sets dialog x and y positions
        node.textContent = textBoxProto.getText(); // Sets selected node (creatorText or viewTexet) text value

        // If the dialog is hidden, then the TTS display is the element. This speaks the text then removes the hidden element from the DOM.
        if (dialog.style.display == "none") {
            localScope.speakText(textBoxProto.getText(), localScope.getFinishedCallback());
            localScope.parentNode.removeChild(localScope);
        }
    };

    /**
     * @return finishedCallback {function} is the callback set at implementation.
     * The callback can be called immediately using .getFinishedCallback()(argument) with argument being optional
     */
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };
}
TextBox.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
TextBox.prototype.createdCommand = undefined;
TextBox.prototype = Object.create(HTMLDialogElement.prototype);
