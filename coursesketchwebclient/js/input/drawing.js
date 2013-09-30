/**
 * Requires:
 * touch_event.js
 * SRL_Library.js
 * srl_display.js
 */

/**
 * Gets input events and creates sketch components out of the events.
 */
function drawingInputCreator(externalInputListener, externalSketchContainer, strokeCreationCallback, graphics) {
	var inputListener = externalInputListener;
	var sketchContainer = externalSketchContainer;
	var currentPoint;
	var pastPoint;
	var currentStroke;
	var graphics = graphics;

	inputListener.listenerScope = this;

	/**
	 * Creates a new stroke and point and adds the point to the stroke.
	 */
	inputListener.setDraggingStartListener(function(drawingEvent) {
		currentPoint = this.listenerScope.createPointFromEvent(drawingEvent);
		currentStroke = new SRL_Stroke(currentPoint);
		pastPoint = currentPoint;
	});

	/**
	 * Sets the drag function that creates a new stroke and adds a point to the stroke.
	 */
	inputListener.setInputDraggedListener(function(drawingEvent) {
		currentPoint = this.listenerScope.createPointFromEvent(drawingEvent);
		currentPoint.setSpeed(pastPoint);
		currentStroke.addPoint(currentPoint);
		pastPoint = currentPoint;
		currentStroke.drawStroke(graphics);
	});

	/**
	 * Adds the point to the stroke, adds the stroke to the sketch container.
	 */
	inputListener.setDraggingEndListener(function(drawingEvent) {
		currentPoint = this.listenerScope.createPointFromEvent(drawingEvent);
		currentPoint.setSpeed(pastPoint);
		currentStroke.addPoint(currentPoint);
		sketchContainer.addObject(currentStroke);
		if (strokeCreationCallback)
			strokeCreationCallback(); // sends back the sketch to be recognized
		currentStroke = false;
		currentPoint = false;
	});

	/**
	 * Creates an {@link SRL_Point} from a drawing event.
	 */
	this.createPointFromEvent = function (drawingEvent) {
		var currentPoint = new SRL_Point(drawingEvent.x, drawingEvent.y);
		currentPoint.setTime(drawingEvent.time);
		currentPoint.setPressure(drawingEvent.pressure);
		currentPoint.setSize(drawingEvent.size);
		currentPoint.setUserCreated(true);
		return currentPoint;
	}
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
	this.addObject = function(srl_object) {
		objectList.push(srl_object);
		objectMap[srl_object.get]
	}
	
	this.getList = function() {
		return objectList;
	}
	
}
