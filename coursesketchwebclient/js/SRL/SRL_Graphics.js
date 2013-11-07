 /*******************************
 *
 *
 * Overload data class
 * @author gigemjt
 *
 *
 *
 *******************************
 */

 /**
  * The general draw function, the first time it is called it goes through types
  */
SRL_Object.prototype.draw = function draw(graphics) {
	if (this.check_type() == "SRL_Shape") {
		this.draw = this.drawShape;
		this.drawShape(graphics);
	} else if (this.check_type() == "SRL_Stroke") {
		this.draw = this.drawStroke;
		this.drawStroke(graphics);
	}
}

/**
 * Must be a valid hex string.
 */
SRL_Object.prototype.setColorHex = function(hex) {
	if (hex[0] != '#') {
		hex = '#' + hex;
	}
	this.color = hex;
}

/**
 * A color is given to a stroke as a HEX string
 */
SRL_Object.prototype.color = false;

/**
 * Draws the {@link SRL_Stroke}.
 *
 * Sets the width and color if they are set.
 * This is the basic drawing of the stroke and does not do anything fancy.
 */
SRL_Stroke.prototype.drawStroke = function drawStroke(graphics) {
	if (this.color) {
		graphics.strokeStyle = this.color;
	} else {
		graphics.strokeStyle = 'black';
	}
	
	if (this.strokeWidth) {
		graphics.lineWidth = strokeWidth;
	} else {
		graphics.lineWidth = 5;
	}
	/*
	if (this.strokeColor) {
		graphics.strokeStyle = strokeColor;
	} else {
		graphics.strokeStyle = 'black';
	}
	*/

	var length = this.getNumPoints();
	if (!length) {
		return;
	}

	var oldPoint = this.getPoint(0);
	graphics.beginPath();
	graphics.moveTo(oldPoint.getX(),oldPoint.getY());
	for(var j = 1; j < length; j++) {
		var point = this.getPoint(j);
		graphics.lineTo(point.getX(), point.getY());
		oldPoint = point;
	}
	graphics.stroke();
}

/**
 * Draws the {@link SRL_Shape}.
 *
 * Does nothing by default.
 */
SRL_Shape.prototype.drawShape = function drawShape(graphics) {
	// Does nothing.
	console.log("drawing shape!");
	var list = this.getInterpretations();
	for(var i = 0; i< list.length; i++) {
		var inter = list[i];
		console.log("label: " + inter.label + " confidence: " + inter.confidence);
	}
	var subShapes = this.getSubObjects();
	for (var i = 0; i <subShapes.length; i++) {
		subShapes[i].draw(graphics);
	}
}

SRL_Sketch.prototype.drawEntireSketch = function() {
	if (this.clearCanvas) {
		this.clearCanvas(this.canvasContext);
	}

	var list = this.getList();
	if (list && this.canvasContext) {
		for(var i = 0; i < list.length; i++) {
			var object = list[i];
			object.draw(this.canvasContext);
		}
	}
}

 /*******************************
 *
 * TEST METHODS BELOW
 *
 * @author gigemjt
 *
 *******************************
 */

new SRL_Object().draw(false);
new SRL_Stroke().draw(false);