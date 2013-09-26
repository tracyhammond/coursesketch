/**
 * Requires:
 * touch_event.js
 * SRL_Library.js
 */

function drawingInputCreator(externalInputListener) {
	var inputListener = externalInputListener;
	var currentPoint;
	var currentStroke;
	this.inputListener.setInputDraggedListener(function(drawingEvent) {
		this.currentPoint = new SRL_Point(drawingEvent.x, drawingEvent.y);
		this.currentPoint.setTime(drawingEvent.time);
		this.currentPoint.setPressure(drawingEvent.pressure);
		this.currentPoint.setSize(drawingEvent.size);
	});

	this.inputListener.setDraggingStartListener(function(drawingEvent) {
	
	});
	
	this.inputListener.setDraggingEndListener(function(drawingEvent) {
	
	});
}

function sketchContainer() {
	var objectList = [];
	objectList.remove = function(srl_object) {
		var i = array.indexOf(srl_object);
		if(i != -1) {
			this.splice(i, 1);
		}
	};

	var objectMap = {};
	this.addObject(srl_object) {
		objectList.push(srl_object);
	}

	this.addObject(srl_object) {
		objectList.push(srl_object);
	}
}
