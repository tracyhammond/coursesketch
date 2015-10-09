/**
 * The custom element for navigating a problem.
 *
 * @class NavigationPanel
 * @attribute loop {Existence} If this property exist the navigator will loop.  (Setting the navigator overrides this property).
 * @attribute assignment_id {String} uses the given value as the assignment id inside the navigator.
 * @attribute index {Number} if the value exist then this is the number used to define the current index.
 *
 */
function ProblemPanel() {

    /**
     * @param {node} templateClone is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     * @instance
     * @memberof NavigationPanel
     * @function intializeElement
     */
    this.initializeElement = function(templateClone) {
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    };

    /**
     * Sets the question text if one it exists.
     *
     * TODO: run question text
     * @param {ProblemNavigator} navPanel the nav panel that is being used.
     * @instance
     * @memberof NavigationPanel
     * @function setNavigator
     */
    this.setQuestionText = function(navPanel) {
        this.itemNavigator = navPanel;
    };
}

NavigationPanel.prototype = Object.create(HTMLElement.prototype);
