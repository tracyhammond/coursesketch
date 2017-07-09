/**
 * The custom element for navigating a problem.
 *
 * @constructor NavigationPanel
 * @attribute loop {Existence} If this property exist the navigator will loop.  (Setting the navigator overrides this property).
 * @attribute assignment_id {String} uses the given value as the assignment id inside the navigator.
 * @attribute index {Number} if the value exist then this is the number used to define the current index.
 *
 */
function ProblemTextPanel() {

    var bufferQuery = '#buffer';
    var textViewQuery = '#visual';

    /**
     * Makes the exit button close the box and enables dragging.
     *
     * @param {node} templateClone - Is a clone of the custom HTML Element for the text box.
     * @instance
     * @function intializeElement
     */
    this.initializeElement = function(templateClone) {
        this.shadowRoot = this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
    };

    /**
     * Sets the question text if one it exists.
     *
     * @param {String} questionText - The text/instructions for the problem.
     * @instance
     * @function setNavigator
     */
    this.setProblemText = function(questionText) {
        if (isUndefined(this.textBuffer)) {
            this.textBuffer = document.createElement('p');
        }
        this.textBuffer.id = 'mathBuffer';
        this.textBuffer.style.display = 'none';
        document.body.appendChild(this.textBuffer);
        var textBuffer = this.textBuffer;
        var actualText = this.shadowRoot.querySelector(textViewQuery);
        textBuffer.innerHTML = questionText;
        actualText.innerHTML = questionText;
        MathJax.Hub.Queue(
            [ 'Typeset', MathJax.Hub, textBuffer ], [ 'swapBuffer', this ]
        );
    };

    /**
     * Sets the question text if one it exists.
     *
     * adds a delay after setting the text once before it will set it again.
     * @param {String} questionText - The text/instructions for the problem.
     */
    this.setRapidProblemText = function(questionText) {
        if (this.ableToSet) {
            this.setProblemText(questionText);
            this.ableToSet = false;
            this.createSetTimeout();
        } else {
            this.createSetTimeout(questionText);
        }
    };

    this.createSetTimeout = function(questionText) {
        var localScope = this;
        if (!isUndefined(this.ableToSetTimeout))  {
            clearTimeout(this.ableToSetTimeout);
            this.ableToSetTimeout = undefined;
            this.createSetTimeout(questionText);
        } else {
            this.ableToSetTimeout = setTimeout(function() {
                localScope.ableToSet = true;
                localScope.ableToSetTimeout = undefined;
                if (!isUndefined(questionText)) {
                    localScope.setRapidProblemText(questionText);
                }
            }, 300);
        }
    };

    /**
     * Renders the textBuffer onto the actual Text.
     */
    this.swapBuffer = function() {
        var textBuffer = this.textBuffer;
        var actualText = this.shadowRoot.querySelector(textViewQuery);
        actualText.innerHTML = textBuffer.innerHTML;

        textBuffer.parentNode.removeChild(textBuffer);
    };
}

ProblemTextPanel.prototype = Object.create(HTMLElement.prototype);
