/**
 * Represents a custom multi-choice element that can have data saved/loaded to/from protobuf.
 */
function MultiChoice() {
    var correctId = undefined;

    /**
     * Removes an answer choice from this multiple choice element.
     *
     * @param {Event} event - the event that triggered this function
     * @param {Element} answer - the answer element to be removed
     */
    this.removeAnswer = function(event, answer) {
        this.getAnswerHolderElement().removeChild(answer);
    };

    /**
     * Marks an answer choice as correct.
     *
     * @param {Event} event - the event that triggered this function
     * @param {Element} answer - the answer element to set as the correct answer
     */
    this.setCorrectAnswer = function(event, answer) {
        var answerChoices = this.shadowRoot.querySelectorAll('.answer-choice');
        for (var i = 0; i < answerChoices.length; ++i) {
            answerChoices[i].querySelector('.correct').textContent = '';
        }
        answer.querySelector('.correct').textContent = '✔';
        this.correctId = answer.id;
    };

    /**
     * Adds an answer choice to this multiple choice element.
     *
     * @param {Event} event - the event that triggered this function
     * @returns {Element} The element that was created that holds the answer.
     */
    this.addAnswer = function(event) {
        // Set up the parent
        var answer = document.createElement('div');
        var lastAnswer = this.shadowRoot.querySelector('#answer-choices').lastChild;
        answer.className = 'answer-choice';
        if (isUndefined(lastAnswer) || lastAnswer.className !== 'answer-choice') {
            answer.id = 'A1';
            answer.setAttribute('data-index', 1);
        } else {
            var nextIndex = parseInt(answer.getAttribute('data-index'), 10) + 1;
            answer.id = 'A' + nextIndex;
            answer.setAttribute('data-index', nextIndex);
        }

        // Radio button
        var radio = document.createElement('input');
        radio.className = 'radio';
        radio.type = 'radio';
        radio.name = 'answer';
        radio.disabled = true;
        answer.appendChild(radio);

        // Need a newline after radio or things don't display properly
        var text = document.createTextNode('\n');
        answer.appendChild(text);

        // Radio label (will be an input for instructors)
        var label = document.createElement('input');
        label.className = 'label';
        label.placeholder = 'Answer choice';
        answer.appendChild(label);

        // Correct check box
        var correct = document.createElement('span');
        correct.className = 'correct';
        /**
         * Called to say that a check box is correct.
         *
         * @param {Event} onClickEvent - On Click event.
         */
        correct.onclick = function(onClickEvent) {
            localScope.setCorrectAnswer(onClickEvent, answer);
        };
        answer.appendChild(correct);

        // Close icon
        var close = document.createElement('span');
        close.className = 'close';
        close.textContent = '×';
        /**
         * Called to remove the answer choice.
         *
         * @param {Event} onClickEvent - On Click event.
         */
        close.onclick = function(onClickEvent) {
            localScope.removeAnswer(onClickEvent, answer);
        };
        answer.appendChild(close);

        // Now that we are done creating the answer choice, add it
        this.getAnswerHolderElement().appendChild(answer);
        return answer;
    };

    this.getAnswerHolderElement = function() {
        return this.shadowRoot.querySelector('#answer-choices');
    };

    /**
     * @param {Node} templateClone - is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        /**
         * Bind addAnswer to click.
         *
         * @param {Event} event the event that was clicked.
         */
        localScope.shadowRoot.querySelector('#add').onclick = function(event) {
            localScope.addAnswer(event);
        };
    };

    /**
     * Saves the embedded HTML element to a protobuf object. Calls finished callback when done.
     *
     * @param {Event} event - event that triggered this function.
     * @returns {MultipleChoice} the created protobuf object.
     */
    this.saveData = function(event) {
        var mcProto = CourseSketch.prutil.MultipleChoice();

        // Populate data in the proto object
        var answerChoices = this.shadowRoot.querySelectorAll('.answer-choice');
        for (var i = 0; i < answerChoices.length; ++i) {
            var answerChoice = CourseSketch.prutil.AnswerChoice();
            answerChoice.id = answerChoices[i].id;
            answerChoice.text = answerChoices[i].querySelector('.label').value;
            mcProto.answerChoices.push(answerChoice);
        }
        mcProto.correctId = this.correctId;

        // If the multi-choice item does not have an id, then a command has not been created for the multi-choice item
        if ((isUndefined(this.id) || this.id === null || this.id === '')) {
            this.command = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.CREATE_MULTIPLE_CHOICE, true);
        }
        this.command.setCommandData(mcProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        var callback = this.getFinishedCallback();
        if (!isUndefined(callback)) {
            callback(this.command, event, this.currentUpdate, mcProto); // Gets finishedCallback and calls it with command as parameter
        }
        return mcProto;
    };

    /**
     * @param {ProtoCommand} mcProto - is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(mcProto) {
        if (isUndefined(shadowRoot) || isUndefined(mcProto)) {
            return;
        }
        for (var i = 0; i < mcProto.answerChoices.length; ++i) {
            var answer = this.addAnswer();
            answer.id = mcProto.answerChoices[i].id;
            answer.querySelector('.label').value = mcProto.answerChoices[i].text;
        }
        this.correctId = mcProto.correctId;
        if (!isUndefined(this.correctId) && this.correctId !== '' && this.correctId !== null) {
            var result = this.getAnswerHolderElement().querySelectorAll('#' + this.correctId)[0];
            result.querySelector('.correct').textContent = '✔';
        }
    };

    /**
     * @returns {Function} finishedCallback is the callback set at implementation.
     * The callback can be called immediately using .getFinishedCallback()(argument) with argument being optional
     */
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };

    /**
     * Sets the listener.
     *
     * @param {Function} listener - called when the data is finished saving.
     */
    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };
}
MultiChoice.prototype = Object.create(HTMLElement.prototype);
MultiChoice.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
MultiChoice.prototype.createdCommand = undefined;
