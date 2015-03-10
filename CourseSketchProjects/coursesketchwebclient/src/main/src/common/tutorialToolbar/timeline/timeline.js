function Timeline () {
     /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        this.updateList = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
        this.index = new IndexManager(this);
        this.addToolArea(shadowRoot.querySelector('.timeline'));
        this.continueButton(shadowRoot);
        undoCreator();
        redoCreator();
    };

    /**
     * this continue button adds a step.  In the future, this button will be coupled with
     * recognition on when a html page is changed or when input from a user is obtained
     */
    this.continueButton = function(shadowRoot) {
        var continueButtonScope = this;
        var continueButton = shadowRoot.querySelector(".btn");
        continueButton.onclick = function() {
            continueButtonScope.addToolArea(shadowRoot.querySelector('.timeline'));
        };
    };

    /**
     * a tool area holds all of the different tools that can be used for tutorial creation, such
     * as textbox, tts, highlight, sketch surface, etc
     */
    this.addToolArea = function(parent) {
        var toolArea = document.createElement("div");
        toolArea.className = "toolarea";
        parent.appendChild(toolArea);
        addPlusButton(toolArea, this);
        this.index.addNewToolArea(toolArea);
    };

    /**
     * the plus button calls show tools to list out the available tools
     */
    function addPlusButton (parent, localScope) {
        var plusButton = document.createElement("div");
        plusButton.title = "Add tutorial element";
        plusButton.className = "plusbutton";
        parent.appendChild(plusButton);
        plusButton.onclick = function() {
            $(plusButton).empty();
            $(plusButton).addClass("tall");
            showTools(plusButton, parent, localScope);
        };
    }

    /**
     * sketch surface is currently not fully implemented.  To see what it does, uncomment the line
     */
    function showTools(plusButton, toolArea, localScope) {
        addTextBoxButton(plusButton, toolArea, localScope);
        addTtsBoxButton(plusButton, toolArea, localScope);
        addHighlightButton(plusButton, toolArea, localScope);
        //addSketchSurfaceButton(plusButton, toolArea, localScope);
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * This allows the user to create a text box to further explain steps in the tutorial
     */
    function addTextBoxButton (plusButton, toolArea, localScope) {
        var textBoxButton = document.createElement("div");
        textBoxButton.title = "Add text box";
        textBoxButton.className = "textboxbutton";
        plusButton.appendChild(textBoxButton);

        textBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var textBox = document.createElement('text-box-creation');
            document.body.appendChild(textBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            textBox.currentUpdate = currentUpdate;
            /* end of creating the textbox */

            function closeTextBox(command) {
                var textBox = document.getElementById(command.commandId);
                var textBoxMarker = localScope.shadowRoot.getElementById(command.commandId);
                if (!isUndefined(textBox.command)) {
                    removeObjectFromArray(textBox.currentUpdate.commands, textBox.command);
                }
                textBox.parentNode.removeChild(textBox);
                if (textBoxMarker != null) {
                    textBoxMarker.parentNode.removeChild(textBoxMarker);
                }
            }

            var textBoxMarker = document.createElement("timeline-marker");
            textBoxMarker.className = "textbox";
            toolArea.appendChild(textBoxMarker);
            textBoxMarker.showBox = textBox;
            $(plusButton).empty();
            /**
             * alter the css tall class to show more rows for more tools
             */
            $(plusButton).removeClass("tall");
            textBoxFinishedListener = function(command, event, currentUpdate) {
                var textBox = document.getElementById(command.commandId);
                //textBox.id = command.commandId;
                if (isUndefined(currentUpdate.commands)) {
                    return;
                }
                if (currentUpdate.commands.indexOf(command) < 0) {
                    currentUpdate.commands.push(command);
                }
                if (!isUndefined(event)) {
                    closeTextBox(command);
                    return;
                }
                var textArea = textBox.shadowRoot.querySelector('textarea');
                textBoxMarker.setPreviewText(textArea.value);
            };

            textBoxMarker.setRemoveFunction(function() {
                closeTextBox(textBox.createdCommand);
            });

            textBox.setFinishedListener(textBoxFinishedListener);
            textBox.saveData();
            textBoxMarker.id = textBox.id;
        };
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * This allows the user to create audible text
     */
    function addTtsBoxButton (plusButton, toolArea, localScope) {
        var ttsBoxButton = document.createElement("div");
        ttsBoxButton.title = "Add text to speech box";
        ttsBoxButton.className = "ttsboxbutton";
        plusButton.appendChild(ttsBoxButton);

        ttsBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var ttsBox = document.createElement('tts-box-creation');
            document.body.appendChild(ttsBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            ttsBox.currentUpdate = currentUpdate;
            /* end of creating the textbox */

            function closeTtsBox(command) {
                var ttsBox = document.getElementById(command.commandId);
                var ttsBoxMarker = localScope.shadowRoot.getElementById(command.commandId);
                if (!isUndefined(ttsBox.command)) {
                    removeObjectFromArray(ttsBox.currentUpdate.commands, ttsBox.command);
                }
                ttsBox.parentNode.removeChild(ttsBox);
                if (ttsBoxMarker != null) {
                    ttsBoxMarker.parentNode.removeChild(ttsBoxMarker);
                }
            }

            var ttsBoxMarker = document.createElement("timeline-marker");
            ttsBoxMarker.className = "ttsbox";
            toolArea.appendChild(ttsBoxMarker);
            ttsBoxMarker.showBox = ttsBox;
            $(plusButton).empty();
            $(plusButton).removeClass("tall");

            ttsBoxFinishedListener = function(command, event, currentUpdate) {
                var ttsBox = document.getElementById(command.commandId);
                if (isUndefined(currentUpdate.commands)) {
                    return;
                }
                if (currentUpdate.commands.indexOf(command) < 0) {
                    currentUpdate.commands.push(command);
                }
                if (!isUndefined(event)) {
                    closeTtsBox(command);
                    return;
                }
                var textArea = ttsBox.shadowRoot.querySelector('textarea');
                ttsBoxMarker.setPreviewText(textArea.value);
            };

            ttsBoxMarker.setRemoveFunction(function() {
                closeTtsBox(ttsBox.createdCommand);
            });

            ttsBox.setFinishedListener(ttsBoxFinishedListener);
            ttsBox.saveData();
            ttsBoxMarker.id = ttsBox.id;
        };
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * The highlight tool will highlight any valid text  for a given step.  Saving the highlighting still needs to be worked on
     */
    function addHighlightButton (plusButton, toolArea, localScope) {
        var highlightButton = document.createElement("div");
        highlightButton.title = "Highlight text";
        highlightButton.className = "highlightbutton";
        plusButton.appendChild(highlightButton);
        highlightButton.onclick = function(event) {
            event.stopPropagation();

            // This prevents the user from making two highlightText tools in the same tutorial step
            if (document.querySelector('highlight-text-creation') == null) {

                /* creating the highlightTool */
                var highlightText = document.createElement('highlight-text-creation');
                document.body.appendChild(highlightText);
                var currentUpdate = localScope.index.getCurrentUpdate();
                highlightText.currentUpdate = currentUpdate;
                /* end of creating the highlightTool */

                var highlightMarker = document.createElement("timeline-marker");
                highlightMarker.className = "highlight";
                toolArea.appendChild(highlightMarker);
                $(plusButton).empty();
                $(plusButton).removeClass("tall");
            } else {
                alert("You already have a highlight tool for this step!");
                $(plusButton).empty();
                $(plusButton).removeClass("tall");
                return;
            }

            function closeHighlightText(command) {
                var highlightText = document.getElementById(command.commandId);
                var highlightMarker = localScope.shadowRoot.getElementById(command.commandId);
                if (!isUndefined(highlightText.command)) {
                    removeObjectFromArray(highlightText.currentUpdate.commands, highlightText.command);
                }
                highlightText.parentNode.removeChild(highlightText);
                if (highlightMarker != null) {
                    highlightMarker.parentNode.removeChild(highlightMarker);
                }
                $(".highlightedText").contents().unwrap();
                document.normalize();
            }

            highlightTextFinishedListener = function(command, event, currentUpdate) {
                var highlightText = document.getElementById(command.commandId);
                if (isUndefined(currentUpdate.commands)) {
                    return;
                }
                if (currentUpdate.commands.indexOf(command) < 0) {
                    currentUpdate.commands.push(command);
                }
                if (!isUndefined(event)) {
                    closeHighlightText(command);
                    return;
                }
            };

            highlightMarker.setRemoveFunction(function() {
                closeHighlightText(highlightText.createdCommand);
            });

            highlightText.setFinishedListener(highlightTextFinishedListener);
            highlightText.saveData();
            highlightMarker.id = highlightText.id;
        };
    }
    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * This will create a simple sketch surface to draw on.  Currently this isn't called because it isn't finished
     */
    function addSketchSurfaceButton(plusButton, toolArea, localScope) {
        var sketchSurfaceButton = document.createElement("div");
        sketchSurfaceButton.title = "Sketch Surface";
        sketchSurfaceButton.className = "sketchsurfacebutton";
        plusButton.appendChild(sketchSurfaceButton);
        sketchSurfaceButton.onclick = function(event) {
            event.stopPropagation();
            var sketchSurface = document.createElement('sketch-surface');
            document.body.appendChild(sketchSurface);
            $(plusButton).empty();
            $(plusButton).removeClass("tall");
        };
    }
    /**
     * creates undos
     */
    function undoCreator () {
        // creates undo for textbox
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete != null) {
                    elementToDelete.saveData();
                    document.body.removeChild(elementToDelete);
                }
            }
        });
        // creates undo for tts box
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete != null) {
                    elementToDelete.saveData();
                    document.body.removeChild(elementToDelete);
                }
            }
        });
        // creates undo for highlight box
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete != null) {
                    document.body.removeChild(elementToDelete);
                }
            }
            $(".highlightedText").contents().unwrap();
            document.normalize();
            // Normalize joins adjacent text nodes. The wrap/unwrap ends up with 3 adjacent text nodes. Visually no different, but needed for future highlighting
        });
    }
    /**
     * creates redos
     */
    function redoCreator () {
        //creates textbox redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
                var textBox  = document.createElement('text-box-creation');
                document.body.appendChild(textBox);
                textBox.loadData(decoded);
                textBox.id = this.commandId;
                textBox.command = this;
                textBox.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                textBox.setFinishedListener(textBoxFinishedListener);
                textBox.saveData();
            }
        });
        // creates tts box redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
                var ttsBox  = document.createElement('tts-box-creation');
                document.body.appendChild(ttsBox);
                ttsBox.loadData(decoded);
                ttsBox.id = this.commandId;
                ttsBox.command = this;
                ttsBox.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                ttsBox.setFinishedListener(ttsBoxFinishedListener);
                ttsBox.saveData();
            }
        });
        // creates highlightText redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateHighlightTextClass());
                var highlightText = document.createElement('highlight-text-creation');
                document.body.appendChild(highlightText);
                highlightText.loadData(decoded);
                highlightText.id = this.commandId;
                highlightText.command = this;
                highlightText.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                highlightText.setFinishedListener(highlightTextFinishedListener);
            }
        });
    }

}
Timeline.prototype = Object.create(HTMLElement.prototype);
