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

	this.continueButton = function(shadowRoot) {
		var continueButtonScope = this;
		var continueButton = shadowRoot.querySelector(".btn");
		continueButton.onclick = function() {
			continueButtonScope.addToolArea(shadowRoot.querySelector('.timeline'));
		};
	};

	this.addToolArea = function(parent) {
		var toolArea = document.createElement("div");
		toolArea.className = "toolarea";
		parent.appendChild(toolArea);
		addPlusButton(toolArea, this);
		this.index.addNewToolArea(toolArea);
	};

	function addPlusButton (parent, localScope) {
		var plusButton = document.createElement("div");
        plusButton.title = "Add tutorial element";
		plusButton.className = "plusbutton";
		parent.appendChild(plusButton);
		plusButton.onclick = function() {
            $(plusButton).empty();
			showTools(plusButton, parent, localScope);
		};
	}

	function showTools(plusButton, toolArea, localScope) {
		addTextBoxButton(plusButton, toolArea, localScope);
		addTtsBoxButton(plusButton, toolArea, localScope);
		addHighlightButton(plusButton, toolArea, localScope);
	}

	function addTextBoxButton (plusButton, toolArea, localScope) {
		var textBoxButton = document.createElement("div");
        textBoxButton.title = "Add text box";
		textBoxButton.className = "textboxbutton";
		plusButton.appendChild(textBoxButton);
		textBoxButton.onclick = function(event) {
			event.stopPropagation();
			/*creating the textbox*/
			var textBox = document.createElement('text-box-creation');
			document.body.appendChild(textBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            textBox.currentUpdate = currentUpdate;
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

			/*end of creating the textbox*/
			var textBoxMarker = document.createElement("timeline-marker");
			textBoxMarker.className = "textbox";
			toolArea.appendChild(textBoxMarker);
			textBoxMarker.showBox = textBox;
			$(plusButton).empty();

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

	function addTtsBoxButton (plusButton, toolArea, localScope) {
		var ttsBoxButton = document.createElement("div");
        ttsBoxButton.title = "Add text to speech box";
		ttsBoxButton.className = "ttsboxbutton";
		plusButton.appendChild(ttsBoxButton);
		ttsBoxButton.onclick = function(event) {
			event.stopPropagation();
			/*creating the textbox*/
			var ttsBox = document.createElement('tts-box-creation');
			document.body.appendChild(ttsBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            ttsBox.currentUpdate = currentUpdate;
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
			/*end of creating the textbox*/
			var ttsBoxMarker = document.createElement("timeline-marker");
			ttsBoxMarker.className = "ttsbox";
			toolArea.appendChild(ttsBoxMarker);
            ttsBoxMarker.showBox = ttsBox;
			$(plusButton).empty();
            
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

	function addHighlightButton (plusButton, toolArea, localScope) {
		var highlightButton = document.createElement("div");
        highlightButton.title = "Highlight text";
		highlightButton.className = "highlightbutton";
		plusButton.appendChild(highlightButton);
		highlightButton.onclick = function(event) {
			event.stopPropagation();
			/*creating the textbox*/
			var highlightBox = document.createElement('highlight-text-creation');
			document.body.appendChild(highlightBox);
			/* Add this function back when highlightTool can save
			highlightBox.setFinishedListener(function(command) {
				globalcommand = command;
			});
			*/

			/*end of creating the textbox*/
			var highlightMarker = document.createElement("div");
			highlightMarker.className = "highlightmarker";
			toolArea.appendChild(highlightMarker);
			$(plusButton).empty();
		};
	}

	function undoCreator () {
		CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
			if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete != null) {
                    elementToDelete.saveData();
                    document.body.removeChild(elementToDelete);
                }
            }
		});
        
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, function() {
			if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete != null) {
                    elementToDelete.saveData();
                    document.body.removeChild(elementToDelete);
                }
            }
		});
	}
    
	function redoCreator () {
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
	}

}
Timeline.prototype = Object.create(HTMLElement.prototype);
