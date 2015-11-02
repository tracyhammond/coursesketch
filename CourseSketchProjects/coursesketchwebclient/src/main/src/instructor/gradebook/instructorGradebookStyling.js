(function() {

    var verticalHeader = undefined;
    var horizontalHeader = undefined;
    var scrollingTable = undefined;
    var startingPosition = undefined;
    var triggerVerticalOffset = -30;


    /**
     * Sets up scrolling for the current table.
     * @param {Element} table
     * @param {Object} startPosition
     */
    CourseSketch.gradeBook.initializeTableScrolling = function(table, startPosition) {
        var oldHead = document.querySelector('.fixedHead');
        if (oldHead !== null) {
            oldHead.parentNode.removeChild(oldHead);
        }
        var oldSide = document.querySelector('.fixedSide');
        if (oldSide !== null) {
            oldSide.parentNode.removeChild(oldSide);
        }
        verticalHeader = undefined;
        horizontalHeader = undefined;
        startingPosition = startPosition;
        scrollingTable = table;
    };

    /**
     * Scrolling code for tables
     */
    $(document).ready(function() {
        // independent of which table is active
        $('.verticalTableHolder').scroll(function(event) {
            var newPosition = $(scrollingTable).offset();
            if (isUndefined(newPosition)) {
                return;
            }
            if (isUndefined(startingPosition)) {
                startingPosition = newPosition;
                return;
            }
            var newTop = newPosition.top;
            var offset = startingPosition.top - newTop;

            if (offset > triggerVerticalOffset && isUndefined(verticalHeader)) {
                var clonedElement = scrollingTable.querySelector('thead').cloneNode(true);

                synchronizeFirstColumnWidth(clonedElement, scrollingTable.querySelector('thead'));

                var table = document.createElement('table');
                table.appendChild(clonedElement);
                verticalHeader = table;
                $(table).addClass('fixedHead z-depth-1');

                document.querySelector('.horizontalTableHolder').appendChild(table);
                $(verticalHeader).offset({ left: $(scrollingTable).offset().left });
            } else if (offset > triggerVerticalOffset && verticalHeader.style.display === 'none') {
                verticalHeader.style.display = 'block';
            } else if (offset <= triggerVerticalOffset && !isUndefined(verticalHeader)) {
                verticalHeader.style.display = 'none';
            }
        });

        // independent of which table is active
        $('.verticalTableHolder').scroll(function(event) {
            var newPosition = $(scrollingTable).offset();
            if (isUndefined(newPosition)) {
                return;
            }

            if (!isUndefined(verticalHeader)) {
                $(verticalHeader).offset({ left: $(scrollingTable).offset().left });
            }

            if (isUndefined(startingPosition)) {
                startingPosition = newPosition;
                return;
            }

            var newLeft = newPosition.left;
            var offset = startingPosition.left - newLeft;
        });
    }); // document ready

    /**
     * Makes column widths the same.
     *
     * @param {HTMLElement} clonedElement
     * @param {HTMLElement} originalElement
     */
    function synchronizeFirstColumnWidth(clonedElement, originalElement) {
        var clonedFirstDiv = clonedElement.querySelector('div');
        var originalFirstDice = scrollingTable.querySelector('thead div');
        $(clonedFirstDiv).width($(originalFirstDice).width());
    }
})();
