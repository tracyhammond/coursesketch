/**
 * Handles the opening and closing of menu bar and changing the title.
 *
 * @class HeaderHandler
 */
function HeaderHandler() {
    var open;

    /**
     * Animates the menu opening and closing.
     *
     * @param {Boolean} value - true if we want to close the header, false otherwise.
     */
    this.animateHeader = function(value) {
        var header = $('.ui-header');
        var height = header.height();
        if (value) { // Close header
            header.animate({
                top: '-' + (height + 2) + 'px'
            }, 300, function() {
                open = false;
            });
            return false;
        } else { // Open header
            header.animate({
                top: '0px'
                }, 300, function() {
                    open = true;
                }
            );
            return false;
        }
    };

    /**
     * Changes the text of the title.
     *
     * @instance
     * @function
     * @memberof HeaderHandler
     * @param {String} titleText what the title is changing to
     */
    this.changeText = function(titleText) {
        document.getElementById('nameBlock').textContent = titleText;
    };

    /**
     * Checks true if the menu is open.
     *
     * @instance
     * @function
     * @memberof HeaderHandler
     * @returns {Boolean} true if the menu is open false otherwise.
     */
    this.isOpen = function() {
        return open;
    };
}
