/**
 * A toolbar that is used in problems.
 * This element appears over everything else
 */
function ProblemToolBar() {
    /**
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(templateClone) {
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
    };


    /**
     * Sets the callback for the submit button.
     */
    this.setSubmitCallback = function(submitCallback) {
        this.shadowRoot.querySelector("#submission").onclick = submitCallback;
    };

    /**
     * Sets the callback for the save button.
     */
    this.setSaveCallback = function(saveCallback) {
        this.shadowRoot.querySelector("#save").onclick = saveCallback;
    };

    /**
     * Sets the callback for the undo button.
     */
    this.setUndoCallback = function(undoCallback) {
        this.shadowRoot.querySelector("#undo").onclick = undoCallback;
    };

    /**
     * Sets the callback for the redo button.
     */
    this.setRedoCallback = function(redoCallback) {
        this.shadowRoot.querySelector("#redo").onclick = redoCallback;
    };

    /**
     * Returns an image element that can be added to the tool bar.
     * It will have the custom load functions and click functions
     */
    this.createButton = function(imgLocation, onclickFunction, onloadFunction) {
        var element = document.createElement("img");
        element.src = img;
        element.onclick = onclickFunction;
        element.onload = onloadFunction;
        element.className = ".specific_button";
        return createButton;
    };

}
ProblemToolBar.prototype = Object.create(HTMLDialogElement.prototype);
