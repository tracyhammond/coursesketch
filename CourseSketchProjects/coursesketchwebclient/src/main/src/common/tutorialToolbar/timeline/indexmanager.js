function IndexManager (timeline) {
	var current;
	var index = -1;
	this.addNewToolArea = function (toolArea) {
		toolArea.onclick = function () {
			switchIndex(getElementIndex(this));
		};
		timeline.updateList.list.push(CourseSketch.PROTOBUF_UTIL.createBaseUpdate());
	}

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

	function getElementIndex(child) {
		var i = -2; // There are 3 previous siblings to the initial toolArea until null, so count from -2 to make the initial have index 1
        // While the current child has a previous sibling. This then moves the "current" up one sibling and repeats
        while ((child = child.previousSibling) != null) {
			i++;
		}
		return i; // Initial toolArea will be index 1 (not 0)
	}

	this.getCurrentUpdate = function () {
        var update;
        if (index < 1) {
            update = timeline.updateList.list[0];
        } else {
            update = timeline.updateList.list[index-1];
        }
		return update;
	};

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
