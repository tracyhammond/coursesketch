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
}

ProblemToolBar.prototype.setSubmitCallback = function(submitCallback) {
    this.shadowRoot.querySelect("#submission").onclick = submitCallback;
};

ProblemToolBar.prototype.setSaveCallback = function(saveCallback) {
    this.shadowRoot.querySelect("#save").onclick = saveCallback;
};

ProblemToolBar.prototype.setUndoCallback = function(undoCallback) {
    this.shadowRoot.querySelect("#undo").onclick = undoCallback;
};

ProblemToolBar.prototype.setRedoCallback = function(redoCallback) {
    this.shadowRoot.querySelect("#redo").onclick = redoCallback;
};

/**
 * Returns an image element that can be added to the tool bar.
 * It will have the custom load functions and click functions
 */
ProblemToolBar.prototype.createButton = function(imgLocation, onclickFunction, onloadFunction) {
    var element = document.createElement("img");
    element.src = img;
    element.onclick = onclickFunction;
    element.onload = onloadFunction;
    element.className = ".specific_button";
    return createButton;
};

ProblemToolBar.prototype = Object.create(HTMLDialogElement.prototype);
