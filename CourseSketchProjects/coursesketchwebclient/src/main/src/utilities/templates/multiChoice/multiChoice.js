/**
 * Represents a custom multi-choice element that can have data saved/loaded to/from protobuf.
 */
function MultiChoice() {

    /**
     * Removes an answer choice from this multiple choice element.
     *
     * @param {ProtobufObject} answer - the answer element to be removed
     * @param {Element} element - the child of the answer element to be removed
     */
    this.removeAnswer = function(answer, element) {
        this.getAnswerHolderElement().removeChild(answer);
    };

    /**
     * Marks an answer choice as correct.
     *
     * @param {Event} event - the event that triggered this function
     * @param {Element} answer - the answer element to set as the correct answer
     */
    this.setCorrectAnswer = function(answerChoice, element) {
        this.protoData.selectedId = answerChoice.id;
        this.correctId = element.id;
    };

    /**
     * Adds an answer choice to this multiple choice element.
     *
     * @param {Event} event - the event that triggered this function
     * @returns {Element} The element that was created that holds the answer.
     */
    this.addAnswer = function() {
        var protoData = CourseSketch.prutil.cleanProtobuf(this.protoData, 'MultipleChoice');
        this.saveToProto(protoData);
        var answers = protoData.answerChoices;
        if (isUndefined(answers) || answers === null) {
            answers = [];
        }
        var newAnswer = CourseSketch.prutil.AnswerChoice();
        newAnswer.id = 'AI' + answers.length;
        answers.push(newAnswer);
        protoData.answerChoices = answers;

        this.loadData(protoData);
    };

    this.getAnswerHolderElement = function() {
        return this.shadowRoot.querySelector('#answer-choices');
    };

    /**
     * @param {Node} templateClone - is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        this.shadowRoot = this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);

        this.style.backgroundColor = '#cfd8dc';

        /**
         * Bind addAnswer to click.
         *
         * @param {Event} event the event that was clicked.
         */
        localScope.shadowRoot.querySelector('#add').onclick = function(event) {
            localScope.addAnswer(event);
        };
    };

    this.saveToProto = function(protoData) {
        this.editPanel.getInput(protoData, this.shadowRoot, this.initialData);
    };

    /**
     * Saves the embedded HTML element to a protobuf object. Calls finished callback when done.
     *
     * @param {Event} event - event that triggered this function.
     * @returns {MultipleChoice} the created protobuf object.
     */
    this.saveData = function(event) {
        var mcProto = CourseSketch.prutil.cleanProtobuf(this.protoData, 'MultipleChoice');
        this.saveToProto(mcProto);

        return mcProto;
    };

    this.clearAnswers = function() {
        var elements = this.shadowRoot.querySelectorAll('.answer-choice');
        for (var i = 0; i < elements.length; i++) {
            elements[i].parentNode.removeChild(elements[i]);
        }
    };
    /**
     * @param {ProtobufObject} mcProto - is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(mcProto) {

        if (isUndefined(this.shadowRoot) || isUndefined(mcProto)) {
            return;
        }
        this.clearAnswers();
        if (isUndefined(this.editPanel)) {
            var localScope = this;
            this.editPanel = new CourseSketch.AdvanceEditPanel();
            this.editPanel.setActions({
                close: function(answerChoice, element) {
                    console.log('removing choice', answerChoice);
                    localScope.removeAnswer(answerChoice, element);
                },
                setCorrect: function(answerChoice, element) {
                    console.log('Selecting answer choice', answerChoice);
                    localScope.setCorrectAnswer(answerChoice, element);
                }
            });
        }

        if (this.studentMode && !isUndefined(this.protoData)) {
            this.protoData.selectedId = mcProto.selectedId;
            mcProto = this.protoData;
        }

        this.initialData = this.editPanel.loadData(mcProto, this.shadowRoot);
        this.protoData = CourseSketch.prutil.cleanProtobuf(mcProto, 'MultipleChoice');
        if (!isUndefined(this.protoData.selectedId) && this.protoData.selectedId !== null && this.studentMode) {
            this.shadowRoot.querySelector('#' + this.protoData.selectedId).checked = true;
        }
    };

    this.turnOnStudentMode = function() {
        this.shadowRoot.querySelector('#instructorTemplate').className = 'ignore';
        this.shadowRoot.querySelector('#studentTemplate').className = 'template';
        this.shadowRoot.querySelector('#remove').style.display = 'none';
        this.shadowRoot.querySelector('#add').style.display = 'none';
        this.studentMode = true;
    };

    this.turnOnReadOnlyMode = function() {
        console.log('do nothing');
    };

    this.setSelected = function() {
        console.log('do nothing2');
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
     * The listener is called with (SrlCommand, event, SrlUpdate, MultiChoice)
     * With MultiChoice being the same type as LoadData.
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
