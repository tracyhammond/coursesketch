/* eslint-disable */
/* jshint ignore:start */
function Timeline() {
    /**
     * @param {Node} templateClone - is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        this.viewingMode = false;

        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        this.updateList = CourseSketch.prutil.SrlUpdateList();
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
        this.shadowRoot.querySelector('.savetutorial').style.display = 'none';
        this.shadowRoot.querySelector('.btn').style.display = 'none';
        var toolElement = this.shadowRoot.querySelector('.toolarea');
        if (toolElement !== null) {
            toolElement.parentNode.removeChild(toolElement);
        }
        var localScope = this;
        CourseSketch.dataManager.getTutorialList(CourseSketch.currentUrl, function(tutorialList) {
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
        var timeline = this.shadowRoot.querySelector('.timeline');
        var createNewTutorialButton = document.createElement('div');

        // displays the list of tutorials and their info.
        for (var i = 0; i < tutorialList.length; i++) {
            var tutorial = tutorialList[i];
            console.log(tutorial);
            var existingTutorial = document.createElement('div');
            existingTutorial.title = tutorial.name;
            existingTutorial.className = 'smallicon';
            existingTutorial.id = tutorial.id;
            existingTutorial.onclick = function() {
                console.log('LOADING EXISTING TUTORIAL');
                console.log(this.id);
                loadTutorialFromServer(localScope, this.id, true);
            };
            timeline.appendChild(existingTutorial);
        }
        createNewTutorialButton.title = 'Create new tutorial';
        createNewTutorialButton.className = 'newicon';
        timeline.appendChild(createNewTutorialButton);
        createNewTutorialButton.onclick = function() {
            localScope.addToolArea(shadowRoot.querySelector('.timeline'));
            localScope.continueButton(shadowRoot);
            for (var i = 0; i < tutorialList.length; i++) {
                // remove first instance of the file descriptor
                timeline.removeChild(shadowRoot.querySelector('.smallicon'));
            }
            timeline.removeChild(shadowRoot.querySelector('.newicon'));
            shadowRoot.querySelector('.btn').style.display = 'inline-block';
            shadowRoot.querySelector('.savetutorial').style.display = 'initial';
            setupSaveButton(localScope);
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
     * Loads a tutorial for viewing.
     *
     * @param {ProtobufTutorial} tutorial The protobuf object representing the tutorial to be loaded.
     * @param {bool} viewingMode Tells if the tutorial is to be loaded in viewing mode or not.
     */
    this.loadTutorial = function(tutorial, viewingMode) {
        this.viewingMode = viewingMode;
        this.updateList = CourseSketch.PROTOBUF_UTIL.cleanProtobuf(tutorial.steps, CourseSketch.PROTOBUF_UTIL.getSrlUpdateListClass());
        console.log(this.updateList);
        var shadowRoot = this.shadowRoot;
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
            shadowRoot.querySelector('.savetutorial').style.display = 'initial';
            this.index.switchIndex(this.updateList.list.length); // Sets indexManager to lastStep as the currentStep
            this.updateList.list[this.updateList.list.length - 1].redo();
        } else {
            shadowRoot.querySelector('.savetutorial').style.display = 'none';
            this.index.switchIndex(1); // Sets indexManager to 1st step as currentStep. Step indexes start from 1. Reason in indexManager.
            this.updateList.list[0].redo();
        }
    };

    // returns command class of command type
    function getCommandClass(commandType) {
        var commandClass = '';
        if (commandType === CourseSketch.prutil.CommandType.CREATE_TEXTBOX) {
            commandClass = 'textbox';
        } else if (commandType === CourseSketch.prutil.CommandType.CREATE_TTSBOX) {
            commandClass = 'ttsbox';
        } else if (commandType === CourseSketch.prutil.CommandType.CREATE_HIGHLIGHT_TEXT) {
            commandClass = 'highlight';
        }
        return commandClass;
    }

    /**
     * the plus button calls show tools to list out the available tools
     */
    function addPlusButton(parent, self) {
        var plusButton = document.createElement('div');
        plusButton.title = 'Add tutorial element';
        plusButton.className = 'plusbutton';
        parent.appendChild(plusButton);
        plusButton.onclick = function() {
            $(plusButton).empty();
            $(plusButton).addClass('tall');
            showTools(plusButton, parent, self);
        };
    }

    /**
     * sketch surface is currently not fully implemented.  To see what it does, uncomment the line
     */
    function showTools(plusButton, toolArea, self) {
        addTextBoxButton(plusButton, toolArea, self);
        addTtsBoxButton(plusButton, toolArea, self);
        addHighlightButton(plusButton, toolArea, self);
        //addSketchSurfaceButton(plusButton, toolArea, self);
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
        if (isUndefined(command)) {
            return;
        }
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
    }

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
    function addTextBoxButton(plusButton, toolArea, self) {
        var textBoxButton = document.createElement('div');
        textBoxButton.title = 'Add text box';
        textBoxButton.className = 'textboxbutton';
        plusButton.appendChild(textBoxButton);

        textBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var textBox = document.createElement('text-box-creation');
            document.body.appendChild(textBox);
            var currentUpdate = self.index.getCurrentUpdate();
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
    function addTtsBoxButton(plusButton, toolArea, self) {
        var ttsBoxButton = document.createElement('div');
        ttsBoxButton.title = 'Add text to speech box';
        ttsBoxButton.className = 'ttsboxbutton';
        plusButton.appendChild(ttsBoxButton);

        ttsBoxButton.onclick = function(event) {
            event.stopPropagation();

            /* creating the textbox */
            var ttsBox = document.createElement('tts-box-creation');
            document.body.appendChild(ttsBox);
            var currentUpdate = self.index.getCurrentUpdate();
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
    function addHighlightButton(plusButton, toolArea, self) {
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
            var currentUpdate = self.index.getCurrentUpdate();
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
    function addSketchSurfaceButton(plusButton, toolArea, self) {
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
        CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.CREATE_TEXTBOX, function() {
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
        CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.CREATE_TTSBOX, function() {
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
        CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
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
        CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.CREATE_TEXTBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.prutil.decodeProtobuf(this.commandData,
                        CourseSketch.prutil.getActionCreateTextBoxClass());
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
        CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.CREATE_TTSBOX, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.prutil.decodeProtobuf(this.commandData,
                        CourseSketch.prutil.getActionCreateTextBoxClass());
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
        CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.CREATE_HIGHLIGHT_TEXT, function() {
            if (!isUndefined(this.commandId)) {
                var decoded = CourseSketch.prutil.decodeProtobuf(this.commandData,
                        CourseSketch.prutil.getActionCreateHighlightTextClass());
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

    /**
     * Saves a tutorial to the database.
     */
    this.saveTutorial = function() {
        var tutorial = CourseSketch.PROTOBUF_UTIL.Tutorial();
        tutorial.id = generateUUID();
        tutorial.name = prompt('Enter a name for the tutorial: ', 'defaultName');
        tutorial.description = prompt('Enter a description for the tutorial: ', 'defaultDescription');
        tutorial.steps = this.updateList;
        tutorial.url = CourseSketch.currentUrl;
        CourseSketch.dataManager.insertTutorial(tutorial);
    };

    /**
     * Inserts a tutorial into the server.
     *
     * @param {HTMLElement} timeline The timeline element in the DOM.
     */
    function setupSaveButton(timeline) {
        var savefd = timeline.shadowRoot.querySelector('.savetutorial');
        savefd.onclick = function() {
            //save tutorial
            timeline.saveTutorial();
            // reset timeline!
            /*var timeParent = timeline.parentNode;
            timeParent.removeChild(timeline);
            var timeline = document.createElement('entire-timeline');
            timeParent.appendChild(timeline);*/
        };
    }

    /**
     * Clears the tutorial timeline of all elements. Acts like a reset.
     */
    this.clearTimeline = function() {
        var currentStep = this.index.getCurrentUpdate();
        for (var i = 0; i < currentStep.commands.length; i++) {
            var commandId = currentStep.commands[i].commandId;
            var elementToDelete = document.getElementById(commandId);
            if (this.viewingMode) {
                elementToDelete.saveData();
            }
            elementToDelete.parentNode.removeChild(elementToDelete);
        }
        $('.highlightedText').contents().unwrap();
        document.normalize();
    };

    /**
     * Loads a tutorial from the server.
     *
     * @param {HTMLElement} timeline The timeline element in the DOM.
     * @param {String} tutorialId The id of the tutorial to be loaded.
     * @param {bool} viewingMode Tells if the tutorial is to be loaded in viewing mode or not.
     */
    function loadTutorialFromServer(timeline, tutorialId, viewingMode) {
        CourseSketch.dataManager.getTutorial(tutorialId, function(tutorial) {
            var parentNode = timeline.parentNode;
            parentNode.removeChild(timeline);
            var newTimeline = document.createElement('entire-timeline');
            parentNode.appendChild(newTimeline);
            newTimeline.loadTutorial(tutorial, viewingMode);
        });
    }

}
Timeline.prototype = Object.create(HTMLElement.prototype);
