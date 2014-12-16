/**
 * @param timeline {object} is the timeline object that the index manager will be associated with
 * Creates an IndexManager for a tutorial timeline
 * Manages currently selected element as well as undoing and redoing update steps when editing tutorial
 */
function IndexManager (timeline) {
    var current; // Used to set currently selected index class to 'focused'
    var index = -1; // Tracks the current index in the IndexManager
    this.addNewToolArea = function (toolArea) {
        toolArea.onclick = function () {
            switchIndex(getElementIndex(this));
        };
        timeline.updateList.list.push(CourseSketch.PROTOBUF_UTIL.createBaseUpdate());
    }

    /**
     * @param destination {integer} is the index of a step that is clicked on
     * This removes the focused class from the previously selected step
     * It then adds the focused class to the currently selected step
     */
    function switchIndex(destination) {
        if (destination == index) {
            return; // No need to switch if the destination index and current index are the same
        }
        var oldIndex = index;
        index = destination;
        $(current).removeClass('focused');
        current = timeline.shadowRoot.querySelector('.timeline').children[destination]; // Continue btn is index 0. First toolArea is index 1.
        $(current).addClass('focused');
        changeListIndex(oldIndex, index);
    }

    /**
     * @param child {object} is the toolArea/step being queried
     * @return i {integer} is the index of the queried toolArea/step
     * Used to query the index in the step/toolArea order of the current element
     */
    function getElementIndex(child) {
        var i = -2; // There are 3 previous siblings to the initial toolArea until null, so count from -2 to make the initial have index 1
        // While the current child has a previous sibling. This then moves the "current" up one sibling and repeats
        while ((child = child.previousSibling) != null) {
            i++;
        }
        return i; // Initial toolArea will be index 1 (not 0)
    }

    /**
     * @return update {object} is the protobuf update of the currently selected step/toolArea
     */
    this.getCurrentUpdate = function () {
        var update;
        if (index < 1) {
            update = timeline.updateList.list[0];
        } else {
            update = timeline.updateList.list[index-1];
        }
        return update;
    };

    /**
     * @param oldIndex {integer} is the index to change away from
     * @param newIndex {integer} is the index to change to
     * Runs protobuf undo function on the update corresponding to oldIndex
     * Runs protobuf redo function on the update corresponding to newIndex
     */
    function changeListIndex (oldIndex, newIndex) {
        // The indexes of toolAreas in relation the children elements starts at 1. In relation to commands and updates, the index starts at 0.
        oldIndex -= 1;
        newIndex -= 1;
        if (oldIndex >= 0) {
            timeline.updateList.list[oldIndex].undo();
            if (!isUndefined(timeline.updateList.list[newIndex])) {
                timeline.updateList.list[newIndex].redo(timeline.updateList.list[newIndex]);
            }
        }
    }

    this.getIndex = function() {
        return index;
    };
}
