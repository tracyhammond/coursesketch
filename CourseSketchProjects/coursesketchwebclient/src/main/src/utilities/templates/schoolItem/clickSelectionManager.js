/**
 *
 * @class ClickSelectionManager
 */
function ClickSelectionManager() {
    this.selectedItems = [];
    this.selectionClassName = ' selectedBox';
    this.localDoc = document;

    /**
     * Adds a selected element to the list of selected elements
     * @param {Element} element
     */
    this.addSelectedItem = function(element) {
        this.selectedItems.push(element);
        this.selectItem(element);
    };

    /**
     * Toggles the selection of the given element.
     */
    this.toggleSelection = function(element) {
        $(element).toggleClass(this.selectionClassName);
    };

    /**
     * Adds the selections class to this specific element.
     * This makes it known that it should be highlighted.
     * @param {Element} element - the element that the class is being added to.
     */
    this.selectItem = function(element) {
        element.className += this.selectionClassName;
    };

    /**
     * clears a specific item of its selected class.
     * @param {Element} element - the HTML element that the class is being added to.
     */
    this.clearItem = function(element) {
        if (!element) {
            return;
        }
        $(element).removeClass(this.selectionClassName);
    };

    /**
     * Removes any selected elements from being selected.
     */
    this.clearAllSelectedItems = function() {
        for (var i = 0; i < this.selectedItems.length; i++) {
            this.clearItem(this.selectedItems[i]);
        }
        // Clears array.
        this.selectedItems = [];
    };

    /**
     * @param {Element} element - the item that we are checking if it is selected.
     * @returns {Boolean} true if the given element is in fact currently selected by this specific manager.
     */
    this.isItemSelected = function(element) {
        return this.selectedItems.indexOf(element) > -1;
    };

    /**
     * Adds the list of selected items to this manager.
     * @param {Array<Element>} listOfElements the elements that the selection is being applied to.
     */
    this.applySelections = function(listOfElements) {
        for (var i = 0; i < listOfElements.length; i++) {
            this.addSelectedItem(listOfElements[i]);
        }
    };
}
