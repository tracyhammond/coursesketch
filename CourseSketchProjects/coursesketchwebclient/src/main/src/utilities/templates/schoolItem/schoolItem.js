function SchoolItem() {

    var shadowRoot = undefined;

    /**
     * @param document
     *            {document} The document in which the node is being imported
     *            to.
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(document, templateClone) {
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    };

    function setOverflow() {
        
    }
}

SchoolItem.prototype = Object.create(HTMLElement.prototype);