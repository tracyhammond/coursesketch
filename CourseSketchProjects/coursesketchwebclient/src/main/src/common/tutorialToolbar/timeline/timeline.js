function Timeline () {
    /**
     * @param {Node} templateClone is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        this.viewingMode = false;

        // DELETE THIS AFTER DAVID's STUFF IS IMPLEMENTED!!!!!
        CourseSketch.dataManager.getTutorialList = function(url, callback) {
            var tutorialList = [];
            for (var i = 0; i < 5; i++) {
                var tutorial = CourseSketch.PROTOBUF_UTIL.Tutorial();
                tutorial.name = "TUT" + i;
                tutorial.description = "DESCRIPT" + i;
                tutorial.id = 'id' + i;
                tutorialList.push(tutorial);
            }
            callback(tutorialList);
        };

        // END OF STUPID DAVID STUFF!

        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        this.updateList = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
        this.index = new IndexManager(this);
        this.addToolArea(shadowRoot.querySelector('.timeline'));
        this.continueButton(shadowRoot);
        try {
            undoCreator();
            redoCreator();
        } catch(e) {
            console.log(e);
        }





        shadowRoot.querySelector('.tutorialtutorial').onclick = function() {
            this.parentNode.removeChild(this);
        };
    };

    /**
     * Call this to load the existing tutorials for the current page.
     */
    this.loadExistingTutorials = function() {
        shadowRoot.querySelector('.btn').style.display = 'none';
        var localScope = this;
        CourseSketch.dataManager.getTutorialList(window.location.href, function(tutorialList) {
            localScope.tutorialList(tutorialList);
        });
    };

    /**
     * When the tutorial view pops up, it has a page that lists all current tutorials
     * as well as a button that will allow the user to create a new tutorial
     */
    this.tutorialList = function(tutorialList) {
        var shadowRoot = this.shadowRoot;
        var localScope = this;
        var newTutorial = document.createElement('div');
        var timelinefd = this.shadowRoot.querySelector('.timeline');
        var addfd = document.createElement('div');

        // displays the list of tutorials and their info.
        for (var i = 0; i < tutorialList.length; i++) {
            (function(index) {
                var viewTutorial = document.createElement('div');
                var listfd = document.createElement('div');
                listfd.title = i;
                listfd.className = 'smallicon';
                timelinefd.appendChild(listfd);
                viewTutorial.onclick = function() {
                    // do tutorial loading here
                };
            })(i);
        }
        addfd.title = 'Create new tutorial';
        addfd.className = 'newicon';
        timelinefd.appendChild(addfd);
        addfd.onclick = function() {
            localScope.addToolArea(shadowRoot.querySelector('.timeline'));
            localScope.continueButton(shadowRoot);
            for (var i = 0; i < tutorialList.length; i++) {
                // remove first instance of the file descriptor
                timelinefd.removeChild(shadowRoot.querySelector('.smallicon'));
            }
            timelinefd.removeChild(shadowRoot.querySelector('.newicon'));
            shadowRoot.querySelector('.btn').style.display = 'inline-block';
        };
    };

    /**
     * this continue button adds a step.  In the future, this button will be coupled with
     * recognition on when a html page is changed or when input from a user is obtained
     */
    this.continueButton = function(shadowRoot) {
        var localScope = this;
        var continueButton = shadowRoot.querySelector('.btn');
        continueButton.onclick = function() {
            localScope.addToolArea(shadowRoot.querySelector('.timeline'));
        };
    };

    /**
     * a tool area holds all of the different tools that can be used for tutorial creation, such
     * as textbox, tts, highlight, sketch surface, etc
     */
    this.addToolArea = function(parent) {
        var toolArea = document.createElement('div');
        toolArea.className = 'toolarea';
        parent.appendChild(toolArea);
        if (!this.viewingMode) {
            addPlusButton(toolArea, this);
        }
        this.index.addNewToolArea(toolArea);
    };

    /**
     * loads a tutorial for viewing
     */
    this.loadTutorial = function(tutorial, viewingMode) {
        this.viewingMode = viewingMode;
        this.updateList = tutorial.steps;
        var initialToolArea = this.shadowRoot.querySelector('.toolarea');
        initialToolArea.parentNode.removeChild(initialToolArea); // Removes default toolArea that is added when a tutorial object is initialized
        for (var i = 0; i < this.updateList.list.length; i++) {
            //create one box for step
            this.addToolArea(this.shadowRoot.querySelector('.timeline'));
            var toolAreaList = this.shadowRoot.querySelectorAll('.toolarea'); // Grabs all tool areas on the screen
            var toolArea = toolAreaList[toolAreaList.length - 1]; // Gets current tool area (last tool area is for the step being loaded)
            if (!viewingMode) {
                this.updateList.list.pop(); // In creation mode, addToolArea adds a blank update to the end of the list. Removes blank update
                var commandList = this.updateList.list[i].commands;
                var markerClass = '';
                for (var j = 0; j < commandList.length; j++) {
                    var markerClass = getCommandClass(commandList[j].commandType);
                    var commandId = commandList[j].commandId;
                    addMarker(toolArea, commandId, markerClass, this);
                }
            } else {
                toolArea.textContent = i;
            }
        }
        // Creation mode shows last step, viewing mode shows first step
        if (!viewingMode) {
            this.index.switchIndex(this.updateList.list.length); // Sets indexManager to lastStep as the currentStep
            this.updateList.list[this.updateList.list.length - 1].redo();
        } else {
            this.index.switchIndex(1); // Sets indexManager to 1st step as currentStep. Step indexes start from 1. Reason in indexManager.
            this.updateList.list[0].redo();
        }
    }

    // returns command class of command type
    function getCommandClass(commandType) {
        var commandClass = '';
        if (commandType === CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX) {
            commandClass = 'textbox';
        } else if (commandType === CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX) {
            commandClass = 'ttsbox';
        } else if (commandType === CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT) {
            commandClass = 'highlight';
        }
        return commandClass;
    }

    /**
     * the plus button calls show tools to list out the available tools
     */
    function addPlusButton (parent, localScope) {
        var plusButton = document.createElement('div');
        plusButton.title = 'Add tutorial element';
        plusButton.className = 'plusbutton';
        parent.appendChild(plusButton);
        plusButton.onclick = function() {
            $(plusButton).empty();
            $(plusButton).addClass('tall');
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

    // adds marker for tutorial tool based on commandId
    function addMarker(toolArea, commandId, markerClass, localScope) {
        var marker = document.createElement('timeline-marker');
        marker.className = markerClass;
        toolArea.appendChild(marker);
        marker.id = commandId;
        var stepTool = document.getElementById(commandId);
        marker.showBox = stepTool;

        marker.setRemoveFunction(function() {
            closeTutorialTool(stepTool.createdCommand, localScope);
        });
    }

    // finished listener for all tutorial tools
    function tutorialToolFinishedListener(command, event, currentUpdate) {
        var stepTool = document.getElementById(command.commandId);
        var localScope = document.body.querySelector('entire-timeline');
        if (isUndefined(currentUpdate.commands)) {
            return;
        }
        if (currentUpdate.commands.indexOf(command) < 0) {
            currentUpdate.commands.push(command);
        }
        if (!isUndefined(event)) {
            closeTutorialTool(command, localScope);
            return;
        }

        if (!isUndefined(localScope) && localScope !== null && !isUndefined(localScope.shadowRoot)) {
            var marker = localScope.shadowRoot.getElementById(command.commandId);
        }
        var commandClass = getCommandClass(command.commandType);
        if ((commandClass === 'textbox' || commandClass === 'ttsbox') && marker !== null && !isUndefined(marker)) {
            var textArea = stepTool.shadowRoot.querySelector('textarea');
            marker.setPreviewText(textArea.value);
        }
    };

    // closes tutorial tool based on passed command
    function closeTutorialTool(command, localScope) {
        var stepTool = document.getElementById(command.commandId);
        var marker = localScope.shadowRoot.getElementById(command.commandId);
        if (!isUndefined(stepTool.command)) {
            removeObjectFromArray(stepTool.currentUpdate.commands, stepTool.command);
        }
        stepTool.parentNode.removeChild(stepTool);
        if (marker !== null) {
            marker.parentNode.removeChild(marker);
        }

        // Removes highlighted text if command is highlightText
        if (getCommandClass(command.commandType) === 'highlight') {
            $('.highlightedText').contents().unwrap();
            document.normalize();
        }
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the 'preview' button will be added to the step
     * This allows the user to create a text box to further explain steps in the tutorial
     */
    function addTextBoxButton (plusButton, toolArea, localScope) {
        var textBoxButton = document.createElement('div');
        textBoxButton.title = 'Add text box';
        textBoxButton.className = 'textboxbutton';
        plusButton.appendChild(textBoxButton);

        textBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var textBox = document.createElement('text-box-creation');
            document.body.appendChild(textBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            textBox.currentUpdate = currentUpdate;
            /* end of creating the textbox */

            $(plusButton).empty();
            /**
             * alter the css tall class to show more rows for more tools
             */
            $(plusButton).removeClass('tall');

            textBox.setFinishedListener(tutorialToolFinishedListener);
            textBox.saveData();
            addMarker(toolArea, textBox.id, 'textbox', localScope);
        };
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * This allows the user to create audible text
     */
    function addTtsBoxButton (plusButton, toolArea, localScope) {
        var ttsBoxButton = document.createElement('div');
        ttsBoxButton.title = 'Add text to speech box';
        ttsBoxButton.className = 'ttsboxbutton';
        plusButton.appendChild(ttsBoxButton);

        ttsBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var ttsBox = document.createElement('tts-box-creation');
            document.body.appendChild(ttsBox);
            var currentUpdate = localScope.index.getCurrentUpdate();
            ttsBox.currentUpdate = currentUpdate;
            /* end of creating the textbox */

            $(plusButton).empty();
            $(plusButton).removeClass('tall');

            ttsBox.setFinishedListener(tutorialToolFinishedListener);
            ttsBox.saveData();
            addMarker(toolArea, ttsBox.id, 'ttsbox', localScope);
        };
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * The highlight tool will highlight any valid text  for a given step.  Saving the highlighting still needs to be worked on
     */
    function addHighlightButton (plusButton, toolArea, localScope) {
        var highlightButton = document.createElement('div');
        highlightButton.title = 'Highlight text';
        highlightButton.className = 'highlightbutton';
        plusButton.appendChild(highlightButton);
        highlightButton.onclick = function(event) {
            event.stopPropagation();

            // This prevents the user from making two highlightText tools in the same tutorial step
            if (document.querySelector('highlight-text-creation') !== null) {
                alert('You already have a highlight tool for this step!');
                $(plusButton).empty();
                $(plusButton).removeClass('tall');
                return;
            }

            /* creating the highlightTool */
            var highlightText = document.createElement('highlight-text-creation');
            document.body.appendChild(highlightText);
            var currentUpdate = localScope.index.getCurrentUpdate();
            highlightText.currentUpdate = currentUpdate;
            /* end of creating the highlightTool */

            $(plusButton).empty();
            $(plusButton).removeClass('tall');

            highlightText.setFinishedListener(tutorialToolFinishedListener);
            highlightText.saveData();
            addMarker(toolArea, highlightText.id, 'highlight', localScope);
        };
    }

    /**
     * the tools all follow a format of creating a div, adding the css, and appending the child to the right thing.
     * when clicked, the "preview" button will be added to the step
     * This will create a simple sketch surface to draw on.  Currently this isn't called because it isn't finished
     */
    function addSketchSurfaceButton(plusButton, toolArea, localScope) {
        var sketchSurfaceButton = document.createElement('div');
        sketchSurfaceButton.title = 'Sketch Surface';
        sketchSurfaceButton.className = 'sketchsurfacebutton';
        plusButton.appendChild(sketchSurfaceButton);
        sketchSurfaceButton.onclick = function(event) {
            event.stopPropagation();
            var sketchSurface = document.createElement('sketch-surface');
            document.body.appendChild(sketchSurface);
            $(plusButton).empty();
            $(plusButton).removeClass('tall');
        };
    }

    /**
     * creates undos
     */
    function undoCreator() {
        // creates undo for textbox
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete !== null) {
                    if (!document.querySelector('entire-timeline').viewingMode) {
                        elementToDelete.saveData();
                    }
                    document.body.removeChild(elementToDelete);
                }
            }
        });
        // creates undo for tts box
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete !== null) {
                    if (!document.querySelector('entire-timeline').viewingMode) {
                        elementToDelete.saveData();
                    }
                    document.body.removeChild(elementToDelete);
                }
            }
        });
        // creates undo for highlight box
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
            if (!isUndefined(this.commandId)) {
                var elementToDelete = document.getElementById(this.commandId);
                if (elementToDelete !== null) {
                    document.body.removeChild(elementToDelete);
                }
            }
            $('.highlightedText').contents().unwrap();
            document.normalize();
            /* Normalize joins adjacent text nodes. The wrap/unwrap ends up with 3 adjacent text nodes.
               Visually no different, but needed for future highlighting */
        });
    }
    /**
     * creates redos
     */
    function redoCreator() {
        //creates textbox redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
                if (!document.querySelector('entire-timeline').viewingMode) {
                    var textBox = document.createElement('text-box-creation');
                } else {
                    var textBox = document.createElement('text-box-viewing');
                }
                document.body.appendChild(textBox);
                textBox.loadData(decoded);
                textBox.id = this.commandId;
                textBox.command = this;
                textBox.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                textBox.setFinishedListener(tutorialToolFinishedListener);
                if (!document.querySelector('entire-timeline').viewingMode) {
                    textBox.saveData();
                }
            }
        });
        // creates tts box redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TTSBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
                if (!document.querySelector('entire-timeline').viewingMode) {
                    var ttsBox = document.createElement('tts-box-creation');
                } else {
                    var ttsBox = document.createElement('tts-box-viewing');
                }
                document.body.appendChild(ttsBox);
                ttsBox.loadData(decoded);
                ttsBox.id = this.commandId;
                ttsBox.command = this;
                ttsBox.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                ttsBox.setFinishedListener(tutorialToolFinishedListener);
                if (!document.querySelector('entire-timeline').viewingMode) {
                    ttsBox.saveData();
                }
            }
        });
        // creates highlightText redo
        CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData,
                        CourseSketch.PROTOBUF_UTIL.getActionCreateHighlightTextClass());
                if (!document.querySelector('entire-timeline').viewingMode) {
                    var highlightText = document.createElement('highlight-text-creation');
                } else {
                    var highlightText = document.createElement('highlight-text-viewing');
                }
                document.body.appendChild(highlightText);
                highlightText.loadData(decoded);
                highlightText.id = this.commandId;
                highlightText.command = this;
                highlightText.currentUpdate = document.querySelector('entire-timeline').index.getCurrentUpdate();
                highlightText.setFinishedListener(tutorialToolFinishedListener);
            }
        });
    }

    this.clearTimeline = function() {
        for (var i = 0; i < this.updateList.list.length; i++) {
            this.updateList.list[i].undo();
        }
    }

}
Timeline.prototype = Object.create(HTMLElement.prototype);
