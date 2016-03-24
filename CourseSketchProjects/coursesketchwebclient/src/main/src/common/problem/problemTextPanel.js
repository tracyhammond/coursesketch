/**
 * The custom element for navigating a problem.
 *
 * @class NavigationPanel
 * @attribute loop {Existence} If this property exist the navigator will loop.  (Setting the navigator overrides this property).
 * @attribute assignment_id {String} uses the given value as the assignment id inside the navigator.
 * @attribute index {Number} if the value exist then this is the number used to define the current index.
 *
 */
function ProblemTextPanel() {

    var bufferQuery = '#buffer';
    var textViewQuery = '#visual';

    /**
     * @param {node} templateClone - Is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     * @instance
     * @memberof NavigationPanel
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
     * @memberof NavigationPanel
     * @function setNavigator
     */
    this.setProblemText = function(questionText) {
        this.textBuffer = document.createElement('p');
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
