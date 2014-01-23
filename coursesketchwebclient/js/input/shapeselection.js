/**
*IN PROGRESS
*
*/


/**
*Colorizes ('highlights') a currently selected/pinpointed sketch component blue ("#0000FF")
*/


/**
*Define case where shape is
*Shape isn't highlighted
*
*/


function Highlighter(externalInputListener, externalSketchContainer, highlightedCallback, graphics) {
	var inputListener = externalInputListener;
	var sketchContainer = externalSketchContainer;
	var currentPoint;
	var pastPoint;
	var currentStroke;
	var graphics = graphics;
	var pastObject = false;

	inputListener.setInputMovedListener(function(event) {
		if (pastObject) {
			pastObject.setIsHighlighted(false);
			sketch.drawEntireSketch();
			pastObject = false;
		}
		var object = sketchContainer.getIntersectingObject(event.x, event.y);
		if (object) {
			object.setIsHighlighted(true);
			object.setHighlightColorHex("#0000FF");
			pastObject = object;
			sketch.drawEntireSketch();
		}
	});

	SRL_Sketch.prototype.getIntersectingObject = function(x,y) {
		var list = this.getList();
		// make this use threading so that everything does not freeze.
		for (var i = 0; i <list.length; i++) {
			var object = list[i];
			if (object instanceof SRL_Stroke) {
				var result = object.getStrokeIntersector().isIntersecting(x,y);
				if (result)
					return object;
			}
		}
		return false;
	}
}