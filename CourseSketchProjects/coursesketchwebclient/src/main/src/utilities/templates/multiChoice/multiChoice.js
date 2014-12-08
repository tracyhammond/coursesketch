function MultiChoice() {
    var correctId = undefined;

    /**
     * Removes an answer choice from this multiple choice element.
     * @param event the event that triggered this function
     * @param answer the answer element to be removed
     */
    this.removeAnswer = function(event, answer) {
        this.shadowRoot.querySelector("#answer-choices").removeChild(answer);
    }

    /**
     * Marks an answer choice as correct.
     * @param event the event that triggered this function
     * @param answer the answer element to set as the correct answer
     */
    this.setCorrectAnswer = function(event, answer) {
        var answerChoices = this.shadowRoot.querySelectorAll(".answer-choice");
        for(var i = 0; i < answerChoices.length; ++i) {
            answerChoices[i].querySelector(".correct").textContent = "";
        }
        answer.querySelector(".correct").textContent = "✔";
        this.correctId = answer.id;
    }

    /**
     * Adds an answer choice to this multiple choice element.
     * @param event the event that triggered this function
     */
    this.addAnswer = function(event) {
        // Set up the parent
        var answer = document.createElement("div");
        var lastAnswer = this.shadowRoot.querySelector("#answer-choices").lastChild;
        answer.className = "answer-choice";
        if(isUndefined(lastAnswer) || lastAnswer.className !== "answer-choice") {
            answer.id = "1";
        } else {
            answer.id = parseInt(lastAnswer.id) + 1;
        }

        // Radio button
        var radio = document.createElement("input");
        radio.className = "radio";
        radio.type = "radio";
        radio.name = "answer";
        radio.disabled = true;
        answer.appendChild(radio);

        // Need a newline after radio or things don't display properly
        var text = document.createTextNode("\n");
        answer.appendChild(text);

        // Radio label (will be an input for instructors)
        var label = document.createElement("input");
        label.className = "label";
        label.placeholder = "Answer choice"
        answer.appendChild(label);

        // Correct check box
        var correct = document.createElement("span");
        correct.className = "correct";
        correct.onclick = function(event) {
            localScope.setCorrectAnswer(event, answer);
        }
        answer.appendChild(correct);

        // Close icon
        var close = document.createElement("span");
        close.className = "close";
        close.textContent = "×";
        close.onclick = function(event) {
            localScope.removeAnswer(event, answer);
        }
        answer.appendChild(close);

        // Now that we are done creating the answer choice, add it
        this.shadowRoot.querySelector("#answer-choices").appendChild(answer);
    }

    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        // Bind addAnswer to click
        localScope.shadowRoot.querySelector("#add").onclick = function(event) {
            localScope.addAnswer(event);
        }
    }

    this.saveData = function(event) {
        var mcProto = CourseSketch.PROTOBUF_UTIL.MultipleChoice();

        // Populate data in the proto object
        var answerChoices = this.shadowRoot.querySelectorAll(".answer-choice");
        for(var i = 0; i < answerChoices.length; ++i) {
            var answerChoice = CourseSketch.PROTOBUF_UTIL.AnswerChoice();
            answerChoice.id = answerChoices[i].id;
            answerChoice.text = answerChoices[i].querySelector(".label").value;
            mcProto.answerChoices.push(answerChoice);
        }
        mcProto.correctId = this.correctId;

        // If the multi-choice item does not have an id, then a command has not been created for the multi-choice item
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_MULTIPLE_CHOICE,true);
        }
        this.command.setCommandData(mcProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        var callback = this.getFinishedCallback();
        if(!isUndefined(callback)) {
            callback(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
        }
        return mcProto;
    }

    /**
     * @param textBoxProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(mcProto) {
        if (isUndefined(shadowRoot) || isUndefined(mcProto)) {
            return;
        }
        for(var i = 0; i < mcProto.answerChoices.length; ++i) {
            this.addAnswer();
            var newAnswerId = "" + (i + 1);
            var answer = this.shadowRoot.getElementById(newAnswerId);
            answer.id = mcProto.answerChoices[i].id;
            answer.querySelector(".label").value = mcProto.answerChoices[i].text;
        }
        this.correctId = mcProto.correctId;
        this.shadowRoot.getElementById(this.correctId).querySelector(".correct").textContent = "✔";
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
MultiChoice.prototype = Object.create(HTMLDialogElement.prototype);
MultiChoice.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
MultiChoice.prototype.createdCommand = undefined;
