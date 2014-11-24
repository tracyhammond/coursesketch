function newTimeline() {
    var newTimeline = document.createElement("div");
    newTimeline.className = "timeline";
    newInstance(newTimeline);
    document.body.appendChild(newTimeline);
}

function continueButton () {
    var button = document.createElement("button");
    var text = document.createTextNode("continue"); 
    document.body.appendChild(continueButton);
    object.onclick=function() {
        
    };
}

function newInstance (parent) {
    var thenewInstance = document.createElement("div"); 
    thenewInstance.className = "instance";
    parent.appendChild(thenewInstance);
    addThing(thenewInstance);
}

function addThing (parent) {
    var newThing = document.createElement("div"); 
    newThing.className = "add";
    parent.appendChild(newThing);
    newThing.onclick = function() {
        showTools(newThing, parent);
    };
}


function showTools(addThing, newInstance) {
    localScope = addThing;
    var textboxTool = document.createElement("div");
    textboxTool.className = "textboxtool";
    addThing.appendChild(textboxTool);
    textboxTool.onclick = function() {
        /*creating the textbox*/
        var textbox = document.createElement('text-box-creation');
        document.body.appendChild(textbox);
        textbox.setFinishedListener(function(command) {
            globalcommand = command;
        });
		/*end of creating the textbox*/
        var textboxSave = document.createElement("div");
        textboxSave.className = "textbox";
        newInstance.appendChild(textboxSave);
        console.log(typeof(localScope));
        localScope.removeChild(textboxTool);
    };
    
}

function addTextbox (parent) {
    var textbox = document.createElement("div");
    textbox.className = "textboxTool"; 
    parent.appendChild(textbox);
    textbox.onclick = function() {
        console.log('I made it');
        
		/*creating the textbox*/
        var textbox = document.createElement('text-box-creation');
        parent.appendChild(textbox);
        textbox.setFinishedListener(function(command) {
            globalcommand = command;
        });
		/*end of creating the textbox*/
		
    };
}


function addTts (parent, clickFunction) {
    var tts = document.createElement("div"); 
    parent.appendChild(newThing);
    if (isUndefined(clickFunction)) {
		clickFunction = function() {
		
        /*creating the tts box*/
        var ttsBox = document.createElement('text-speech-creation');
        parent.appendChild(ttsBox);
        ttsBox.setFinishedListener(function(command) {
            globalcommand = command;
        });
        /*end of creating the tts box*/
            
		};
	}
	tts.onclick = clickFunction;
}
function addHighlight (parent) {
    var highlight = document.createElement("div"); 
    parent.appendChild(newThing);
    if (isUndefined(clickFunction)) {
		clickFunction = function() {
			// do real click stuff
		};
	}
	highlight.onclick = clickFunction;
}
