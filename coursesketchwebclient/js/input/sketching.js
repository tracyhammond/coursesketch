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
		currentStroke.setId(generateUUID());
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
		currentStroke.setTime(currentPoint.getTime());
		sketchContainer.addObject(currentStroke);
		//try {
			if (strokeCreationCallback)
				strokeCreationCallback(currentStroke); // Sends back the current stroke.
		/*} catch(err) {
			console.error(err.message);
		}*/
		currentStroke = false;
		currentPoint = false;
	});

	/**
	 * Creates an {@link SRL_Point} from a drawing event.
	 */
	this.createPointFromEvent = function (drawingEvent) {
		var currentPoint = new SRL_Point(drawingEvent.x, drawingEvent.y);
		currentPoint.setId(generateUUID());
		currentPoint.setTime(drawingEvent.time);
		currentPoint.setPressure(drawingEvent.pressure);
		currentPoint.setSize(drawingEvent.size);
		currentPoint.setUserCreated(true);
		return currentPoint;
	}
}