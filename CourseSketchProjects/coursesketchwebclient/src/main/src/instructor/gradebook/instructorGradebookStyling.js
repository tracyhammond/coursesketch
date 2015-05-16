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
                verticalHeader = deepComputedClone(scrollingTable.querySelector('thead'));
                var table = document.createElement('table');
                $(table).addClass('fixedHead z-depth-1');
                table.appendChild(verticalHeader);
                document.querySelector('.horizontalTableHolder').appendChild(table);
            } else if (offset > triggerVerticalOffset && verticalHeader.style.display === 'none') {
                verticalHeader.style.display = 'block';
            } else if (offset <= triggerVerticalOffset && !isUndefined(verticalHeader)) {
                verticalHeader.style.display = 'none';
            }
        });

        // independent of which table is active
        $('.verticalTableHolder').scroll(function(event) {
            //console.log(event);
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

            var newTop = newPosition.left;
            var offset = startingPosition.left - newTop;
            var scrolledElement = document.querySelector('.tabletalk');
            console.log($(scrolledElement).offset());
            //console.log($('.horizontalTableHolder').scrollTop());
            var y_scroll_pos = window.pageYOffset;
            var scroll_pos_test = 150;
            // set to whatever you want it to be

            if(y_scroll_pos > scroll_pos_test) {
                $('body').css('background-color','#000');
            }
            else
            {
                $('body').css('background-color','#FFF');
            }
        });
    }); // document ready

    /**
     * Returns a deep clone copying height and width.
     * @param {Element} node The node we want a copy of that contains the width and height needed.
     */
    function deepComputedClone(node) {
        var result = undefined;
        if (node.children.length <= 0) {
            result = node.cloneNode(true);
        } else {
            result = node.cloneNode(false);
            for (var i = 0; i < node.children.length; i++) {
                result.appendChild(deepComputedClone(node.children[i]));
            }
        }
        //$(result).width($(node).width());
        //$(result).height($(node).height());
        return result;
    }
})();
