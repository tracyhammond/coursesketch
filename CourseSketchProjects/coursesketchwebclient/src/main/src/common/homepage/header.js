/**
 * @class HeaderHandler
 * Handles the opening and closing of menu bar and changing the title.
 */
function HeaderHandler() {
    var open;

    /**
     * Animates the menu opening and closing.
     * @param {Boolean} value - true if we want to close the header, false otherwise.
     */
    this.animateHeader = function(value) {
        var height = $('.ui-header').height();
        if (value) { // Close header
            $('.ui-header').animate({
                top: '-' + (height + 2) + 'px',
                }, 300, function() {
                    open = false;
                }
            );
            return false;
        } else { // Open header
            $('.ui-header').animate({
                top: '0px',
                }, 300, function() {
                    open = true;
                }
            );
            return false;
        }
    };

    /**
     * @instance
     * @function
     * @memberof HeaderHandler
     * changes the text of the title
     * @param {String} titleText what the title is changing to
     */
    this.changeText = function(titleText) {
        document.getElementById('nameBlock').textContent = titleText;
    };

    /**
     * @instance
     * @function
     * @memberof HeaderHandler
     * @returns {Boolean} true if the menu is open false otherwise.
     */
    this.isOpen = function() {
        return open;
    };
}
