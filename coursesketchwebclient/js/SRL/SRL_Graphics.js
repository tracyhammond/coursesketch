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
		this.draw = drawShape;
		this.drawShape(graphics);
	} else if (this.check_type() == "SRL_Stroke") {
		this.draw = this.drawStroke;
		this.drawStroke(graphics);
	}
}

/**
 * Draws the {@link SRL_Stroke}.
 *
 * Sets the width and color if they are set.
 * This is the basic drawing of the stroke and does not do anything fancy.
 */
SRL_Stroke.prototype.drawStroke = function drawStroke(graphics) {
	if (this.strokeWidth) {
		graphics.lineWidth = strokeWidth;
	} else {
		graphics.lineWidth = 5;
	}

	if (this.strokeColor) {
		graphics.strokeStyle = strokeColor;
	} else {
		graphics.strokeStyle = 'black';
	}

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
	var list = this.getInterpretations();
	for(var i = 0; i< list.length; i++) {
		var inter = list[i];
		console.log("label: " + inter.label + " confidence: " + inter.confidence);
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