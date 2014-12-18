/**
 * Requires:
 * touch_event.js
 * SRL_Library.js
 * srl_display.js
 */

/**
 * Gets input events and creates sketch components out of the events.
 * @param externalInputListener {InputListener} an instanceOf InputListener this is where the input for the creator is grabbed.
 */
function SketchEventConverter(externalInputListener, strokeCreationCallback, graphics) {
	var inputListener = externalInputListener;
	var currentPoint;
	var pastPoint;
	var currentStroke;

	inputListener.listenerScope = this;

	/**
	 * Creates a new stroke and point and adds the point to the stroke.
	 */
	inputListener.setDraggingStartListener(function(drawingEvent) {
		currentPoint = this.createPointFromEvent(drawingEvent);
		currentStroke = new SRL_Stroke(currentPoint);
		currentStroke.setId(generateUUID());
		graphics.createNewPath(currentPoint);
		pastPoint = currentPoint;
	}.bind(this));

	/**
	 * Sets the drag function that creates a new stroke and adds a point to the stroke.
	 */
	inputListener.setInputDraggedListener(function(drawingEvent) {
		currentPoint = this.createPointFromEvent(drawingEvent);
		currentPoint.setSpeed(pastPoint);
		currentStroke.addPoint(currentPoint);
		graphics.updatePath(currentPoint);
		pastPoint = currentPoint;
	}.bind(this));

	/**
	 * Adds the point to the stroke, adds the stroke to the sketch container.
	 */
	inputListener.setDraggingEndListener(function(drawingEvent) {
		currentPoint = this.createPointFromEvent(drawingEvent);
		currentPoint.setSpeed(pastPoint);
		currentStroke.addPoint(currentPoint);
		currentStroke.setTime(currentPoint.getTime());
		currentStroke.finish();
		graphics.endPath(currentPoint);
		try {
			if (strokeCreationCallback)
				strokeCreationCallback(currentStroke); // Sends back the current stroke.
		} catch(err) {
			currentStroke = false;
			currentPoint = false;
			throw err;
		}
		currentStroke = false;
		currentPoint = false;
	}.bind(this));

	/**
	 * Creates an {@link SRL_Point} from a drawing event.
	 */
	this.createPointFromEvent = function (drawingEvent) {
		var currentPoint = new SRL_Point(drawingEvent.x, drawingEvent.y);
		//currentPoint.setId(generateUUID());
		currentPoint.setTime(drawingEvent.time);
		currentPoint.setPressure(drawingEvent.pressure);
		currentPoint.setSize(drawingEvent.size);
		currentPoint.setUserCreated(true);
		return currentPoint;
	}
}
