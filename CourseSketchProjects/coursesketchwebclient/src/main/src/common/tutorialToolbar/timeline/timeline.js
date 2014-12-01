function Timeline () {

	 /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        addToolArea(shadowRoot.querySelector('.timeline'));
		continueButton(shadowRoot);
    };

	function continueButton(shadowRoot) {
		var continueButton = shadowRoot.querySelector(".btn");
		continueButton.onclick = function() {
		addToolArea(shadowRoot.querySelector('.timeline'));
		};
	}

	function addToolArea (parent) {
		var toolArea = document.createElement("div");
		toolArea.className = "toolarea";
		parent.appendChild(toolArea);
		addPlusButton(toolArea);
	}

	function addPlusButton (parent) {
		var plusButton = document.createElement("div");
		plusButton.className = "plusbutton";
		parent.appendChild(plusButton);
		plusButton.onclick = function() {
			showTools(plusButton, parent);
		};
	}


	function showTools(plusButton, toolArea) {
		addTextBoxButton(plusButton, toolArea);
		addTtsBoxButton(plusButton, toolArea);
		addHighlightButton(plusButton, toolArea);
	}

	function addTextBoxButton (plusButton, toolArea) {
		var textBoxButton = document.createElement("div");
		textBoxButton.className = "textboxbutton";
		plusButton.appendChild(textBoxButton);
		textBoxButton.onclick = function(event) {
			event.stopPropagation();
			/*creating the textbox*/
			var textBox = document.createElement('text-box-creation');
			document.body.appendChild(textBox);
			var textArea = textBox.shadowRoot.querySelector('textarea');
			function closeTextBox() {
				textBox.parentNode.removeChild(textBox);
			}

			/*end of creating the textbox*/
			var textBoxMarker = document.createElement("timeline-marker");
			textBoxMarker.className = "textbox";
			toolArea.appendChild(textBoxMarker);
			textBoxMarker.showBox = textBox;
			$(plusButton).empty();

			textBoxMarker.setRemoveFunction(closeTextBox);
			textBox.setFinishedListener(function(command) {
				globalcommand = command;
				textBoxMarker.setPreviewText(textArea.value);
			});
		};
	}

	function addTtsBoxButton (plusButton, toolArea) {
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

	function addHighlightButton (plusButton, toolArea) {
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
}
Timeline.prototype = Object.create(HTMLElement.prototype);
