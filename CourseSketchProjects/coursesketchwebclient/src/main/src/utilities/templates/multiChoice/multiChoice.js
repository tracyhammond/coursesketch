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
     * @param {AnswerChoice} answerChoice - The answer choice that was selected.
     */
    this.setCorrectAnswer = function(answerChoice) {
        if (this.protoData.displayType === CourseSketch.prutil.MultipleChoiceDisplayType.CHECKBOX) {
            var index = this.protoData.selectedIds.indexOf(answerChoice.id);
            if (index === -1) {
                this.protoData.selectedIds.push(answerChoice.id);
            } else {
                this.protoData.selectedIds.splice(index, 1);
            }
        } else {
            this.protoData.selectedIds = [ answerChoice.id ];
        }
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
     * @returns {MultipleChoice} the created protobuf object.
     */
    this.saveData = function() {
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

    this.convertToCheckbox = function() {
        var elements = this.shadowRoot.querySelectorAll('.answer-choice');
        for (var i = 0; i < elements.length; i++) {
            var inputButton = elements[i].querySelector('input[type="radio"]');
            if (inputButton !== null) {
                inputButton.type = 'checkbox';
            }
        }
    };

    /**
     * @param {MultipleChoice} mcProto - is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(mcProto) {
        if (isUndefined(this.shadowRoot) || isUndefined(mcProto)) {
            this.protoData = CourseSketch.prutil.MultipleChoice();
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

        this.initialData = this.editPanel.loadData(mcProto, this.shadowRoot);
        this.protoData = CourseSketch.prutil.cleanProtobuf(mcProto, 'MultipleChoice');

        if (this.protoData.displayType === CourseSketch.prutil.MultipleChoiceDisplayType.CHECKBOX) {
            this.convertToCheckbox();
        }
        if (!isUndefined(this.protoData.selectedIds) && this.protoData.selectedIds !== null && this.studentMode) {
            for (var i = 0; i < this.protoData.selectedIds.length; i++) {
                this.setSelectedId(this.protoData.selectedIds[i]);
            }
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

    this.turnOnCheckboxMode = function() {
        this.convertToCheckbox();
        this.protoData.displayType = CourseSketch.prutil.MultipleChoiceDisplayType.CHECKBOX;
    };

    this.setSelectedId = function(selectedId) {
        this.shadowRoot.querySelector('#' + selectedId).checked = true;
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
