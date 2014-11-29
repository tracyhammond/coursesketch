function addNewTimeline() {
    var newTimeline = document.createElement("div");
    newTimeline.className = "timeline";
    addToolArea(newTimeline);
    document.body.appendChild(newTimeline);
}

/*function continueButton () {
    var button = document.createElement("button");
    var text = document.createTextNode("continue"); 
    document.body.appendChild(continueButton);
    object.onclick=function() {
        
    };
}*/

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
        textArea.onblur = textBox.saveData.bind(textBox);
        function closeTextBox() {
			textBox.parentNode.removeChild(textBox);
		}	
        

		/*end of creating the textbox*/
        var textBoxMarker = document.createElement("timeline-marker");
        textBoxMarker.className = "textbox";
        toolArea.appendChild(textBoxMarker);
        $(plusButton).empty();
        
        textBoxMarker.setRemoveFunction(closeTextBox);
        textBox.setFinishedListener(function(command) {
            globalcommand = command;
			console.log(textArea.value);
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
        var ttsBoxMarker = document.createElement("div");
        ttsBoxMarker.className = "ttsboxmarker";
        toolArea.appendChild(ttsBoxMarker);
        $(plusButton).empty();
        ttsBoxMarker.onclick = function() {
			addCross(ttsBoxMarker, closeTtsBox);
		};
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
