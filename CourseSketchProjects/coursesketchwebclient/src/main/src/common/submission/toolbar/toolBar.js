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

ToolBar.prototype.setSubmitCallback = function(submitCallback) {
    this.shadowRoot.querySelect("#submission").onclick = submitCallback;
};

ToolBar.prototype.setSaveCallback = function(saveCallback) {
    this.shadowRoot.querySelect("#save").onclick = saveCallback;
};

ToolBar.prototype.setUndoCallback = function(undoCallback) {
    this.shadowRoot.querySelect("#undo").onclick = undoCallback;
};

ToolBar.prototype.setRedoCallback = function(redoCallback) {
    this.shadowRoot.querySelect("#redo").onclick = redoCallback;
};

/**
 * Returns an image element that can be added to the tool bar.
 * It will have the custom load functions and click functions
 */
ToolBar.prototype.createButton = function(imgLocation, onclickFunction, onloadFunction) {
    var element = document.createElement("img");
    element.src = img;
    element.onclick = onclickFunction;
    element.onload = onloadFunction;
    element.className = ".specific_button";
    return createButton;
}
