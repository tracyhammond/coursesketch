function ClickSelectionManager() {
    this.selectedItems = [];
    this.selectionClassName = ' selectedBox';
    this.localDoc = document;

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

    this.selectItem = function(element) {
        element.className += this.selectionClassName;
    };

    this.clearItem = function(element) {
        if (!element) {
            return;
        }
        $(element).removeClass(this.selectionClassName);
    };

    this.clearAllSelectedItems = function() {
        for (var i = 0; i < this.selectedItems.length; i++) {
            this.clearItem(this.selectedItems[i]);
        }
        // Clears array.
        this.selectedItems = [];
    };

    this.isItemSelected = function(element) {
        return this.selectedItems.indexOf(element) > -1;
    };
}
