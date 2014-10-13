function clickSelectionManager() {
    this.selectedItems = [];
    this.selectionClassName = ' selected_box';

    this.addSelectedItem = function(id) {
        this.selectedItems.push(id);
        this.selectItem(id);
    };

    this.selectItem = function(id) {
        document.getElementById(id).className += this.selectionClassName;
    };

    this.clearItem = function(id) {
        var toSelect = document.getElementById(id);
        if (!toSelect) {
            return;
        }
        var className = toSelect.className;
        var index = className.indexOf(this.selectionClassName);
        var firstHalf = className.substring(0, index);
        var secondHalf = className.substring(index + this.selectionClassName.length);
        toSelect.className = firstHalf + secondHalf;
    };

    this.clearAllSelectedItems = function() {
        for (var i = 0; i < this.selectedItems.length; i++) {
            this.clearItem(this.selectedItems[i]);
        }
        // Clears array.
        this.selectedItems = [];
    };

    this.isItemSelected = function(id) {
        return this.selectedItems.indexOf(id) > -1;
    };
}