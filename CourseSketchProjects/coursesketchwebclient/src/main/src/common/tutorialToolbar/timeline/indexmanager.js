function IndexManager (timeline) {
	var current;
	var index = -1;
	this.addNewToolArea = function (toolArea) {
		console.log("adding element");
		console.log(toolArea);
		toolArea.onclick = function () {
			console.log("switching element?");
			switchIndex(getElementIndex(this));
		};
		timeline.updateList.list.push(CourseSketch.PROTOBUF_UTIL.createBaseUpdate());
	}

	function switchIndex(destination) {
		if (destination == index) {
			return;
		}
		var oldIndex = index;
		index = destination;
		$(current).removeClass('focused');
		current = timeline.shadowRoot.querySelector('.timeline').children[destination];
		console.log(timeline.shadowRoot.querySelector('.timeline').children);
		$(current).addClass('focused');
		changeListIndex(oldIndex, index);
	}

	function getElementIndex(child) {
		var i = -1;
		while ((child = child.previousSibling) != null) {
			i++;
		}
		console.log(i-1 + " abc");
		return i - 1;
	}

	this.getCurrentUpdate = function () {
		var update = timeline.updateList.list[index];
		console.log("CURRENT UPDATE");
		console.log(update);
		return update;
	};

	function changeListIndex (oldIndex, newIndex) {
		oldIndex -= 1;
		newIndex -= 1;
		if (oldIndex >= 0) {
			console.log(timeline.updateList.list[oldIndex]);
			timeline.updateList.list[oldIndex].undo();
			if (!isUndefined(timeline.updateList.list[newIndex])) {
				timeline.updateList.list[newIndex].redo();
			}
		}
	}

	this.getIndex = function() {
		return index;
	};
}
