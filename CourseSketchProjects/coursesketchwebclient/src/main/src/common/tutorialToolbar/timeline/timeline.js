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
		redoCreator();
		undoCreator();
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
		plusButton.className = "plusbutton";
		parent.appendChild(plusButton);
		plusButton.onclick = function() {
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
		textBoxButton.className = "textboxbutton";
		plusButton.appendChild(textBoxButton);
		textBoxButton.onclick = function(event) {
			event.stopPropagation();
			var update = localScope.index.getCurrentUpdate();
			/*creating the textbox*/
			var textBox = document.createElement('text-box-creation');
			document.body.appendChild(textBox);
			var textArea = textBox.shadowRoot.querySelector('textarea');
			function closeTextBox(command) {
				if (!isUndefined(command)) {
					removeObjectFromArray(update.commands, command);
				}
				textBox.parentNode.removeChild(textBox);
			}

			/*end of creating the textbox*/
			var textBoxMarker = document.createElement("timeline-marker");
			textBoxMarker.className = "textbox";
			toolArea.appendChild(textBoxMarker);
			textBoxMarker.showBox = textBox;
			$(plusButton).empty();

            textBoxFinishedListener = function(command, event) {
                if (!isUndefined(event)) {
                    textBoxMarker.parentNode.removeChild(textBoxMarker);
                }
                // If the command does not exist in the current update, add it to the update
                if (update.commands.indexOf(command) < 0) {
                    console.log("saving data");
                    update.commands.push(command);
                }
                textBox.id = command.commandId;
                textBoxMarker.setPreviewText(textArea.value);
            };
            
			textBoxMarker.setRemoveFunction(function() {
				closeTextBox(textBox.createdCommand);
			});

			textBox.setFinishedListener(textBoxFinishedListener);
		};
	}
    
	function addTtsBoxButton (plusButton, toolArea, localScope) {
		var ttsBoxButton = document.createElement("div");
		ttsBoxButton.className = "ttsboxbutton";
		plusButton.appendChild(ttsBoxButton);
		ttsBoxButton.onclick = function(event) {
			event.stopPropagation();
			/*creating the textbox*/
			var ttsBox = document.createElement('text-speech-creation');
			document.body.appendChild(ttsBox);
			function closeTtsBox() {
				ttsBox.parentNode.removeChild(ttsBox);
			}
			ttsBox.setFinishedListener(function(command) {
				globalcommand = command;
			});
			/*end of creating the textbox*/
			var ttsBoxMarker = document.createElement("timeline-marker");
			ttsBoxMarker.className = "ttsbox";
			toolArea.appendChild(ttsBoxMarker);
			$(plusButton).empty();

			var textArea = ttsBox.shadowRoot.querySelector('textarea');
			ttsBoxMarker.setRemoveFunction(closeTtsBox);
			ttsBox.setFinishedListener(function(command) {
				globalcommand = command;
				ttsBoxMarker.setPreviewText(textArea.value);
			});
		};
	}

	function addHighlightButton (plusButton, toolArea, localScope) {
		var highlightButton = document.createElement("div");
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
			var elementToDelete = document.getElementById(this.commandId);
			document.body.removeChild(elementToDelete);
		});
	}
	function redoCreator () {
		CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
			var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
					CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
			var textBox  = document.createElement('text-box-creation');
			document.body.appendChild(textBox);
			textBox.loadData(decoded);
			textBox.id = this.commandId;
            textBox.setFinishedListener(textBoxFinishedListener);
		});
	}
	
}
Timeline.prototype = Object.create(HTMLElement.prototype);
