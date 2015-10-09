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

    var bufferQuery = "#buffer";
    var textViewQuery = "#visual";

    /**
     * @param {node} templateClone is a clone of the custom HTML Element for the text box
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
     * TODO: run question text through math jax
     * @param {ProblemNavigator} navPanel the nav panel that is being used.
     * @instance
     * @memberof NavigationPanel
     * @function setNavigator
     */
    this.setProblemText = function(questionText) {
        var textBuffer = this.shadowRoot.querySelection(bufferQuery);
        var actualText = this.shadowRoot.querySelection(textViewQuery);
        textBuffer.setTextContent(questionText);
        actualText.setTextContet(actualText);
        MathJax.hub.Typeset(textBuffer, ["swapBuffer"], this);
    };

    /**
     * Renders the textBuffer onto the actual Text
     */
    this.swapBuffer = function() {
        var textBuffer = this.shadowRoot.querySelection(bufferQuery);
        var actualText = this.shadowRoot.querySelection(textViewQuery);
        actualText.innerHTML = textBuffer.innerHTML;
    }
}

NavigationPanel.prototype = Object.create(HTMLElement.prototype);
