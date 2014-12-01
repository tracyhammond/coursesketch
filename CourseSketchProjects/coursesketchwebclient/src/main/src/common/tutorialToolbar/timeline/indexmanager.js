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
		return i - 1;
	}

	this.getCurrentUpdate = function () {
		var update = timeline.updateList.list[index];
		if (isUndefined(update) && index != -1) {
			update = timeline.updateList.list[index] = CourseSketch.PROTOBUF_UTIL.createBaseUpdate();
		}
		return update;
	};

	function changeListIndex (oldIndex, newIndex) {
		if (oldIndex != -1) {
			timeline.updateList.list[oldIndex].undo();
			timeline.updateList.list[newIndex].redo();
		}
	}

	this.getIndex = function() {
		return index;
	};
}
