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
SRL_Object.prototype.draw = function draw(graphics, forcedColor) {
	if (this.check_type() == "SRL_Shape") {
		this.draw = this.drawShape;
		this.drawShape(graphics, forcedColor);
	} else if (this.check_type() == "SRL_Stroke") {
		this.draw = this.drawStroke;
		this.drawStroke(graphics, forcedColor);
	}
};

/**
 * A color is given to a stroke as a HEX string
 */
SRL_Object.prototype.color = false;
	

/**
 * Must be a valid hex string.
 */
SRL_Object.prototype.setColorHex = function(hex) {
	if (hex[0] != '#') {
		hex = '#' + hex;
	}
	this.color = hex;
};

SRL_Object.prototype.isHighlighted = false;
SRL_Object.prototype.setIsHighlighted = function(value) {
	this.isHighlighted = value;
};
SRL_Object.prototype.getIsHighlighted = function() {
	return this.isHighlighted;
};

SRL_Object.prototype.highlightColor = false;
SRL_Object.prototype.setHighlightColorHex = function(hex) {
	if (hex[0] != '#') {
		hex = '#' + hex;
	}
	this.highlightColor = hex;
};

SRL_Object.prototype.getHighlightColor = function() {
	return this.highlightColor;
};
/**
 * Draws the {@link SRL_Stroke}.
 *
 * Sets the width and color if they are set.
 * This is the basic drawing of the stroke and does not do anything fancy.
 */
SRL_Stroke.prototype.drawStroke = function drawStroke(graphics, forcedColor) {
	if (forcedColor) {
		graphics.strokeStyle = this.forceColor;
	} else if (this.color) {
		graphics.strokeStyle = this.color;
	} else {
		graphics.strokeStyle = 'black';
	}

	if (this.getIsHighlighted()) {
		graphics.strokeStyle = this.getHighlightColor();
	}
	

	if (this.strokeWidth) {
		graphics.lineWidth = strokeWidth;
	} else {
		graphics.lineWidth = 5;
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
	//this.getBoundingBox().drawBounds(graphics);
	//this.getStrokeIntersector().drawBounds(graphics);
};

SRL_BoundingBox.prototype.drawBounds = function drawBounds(graphics) {
	var rectangle = this.getRectangle();
	graphics.rect(rectangle.x,rectangle.y,rectangle.width,rectangle.height);
	graphics.stroke();
};

SRL_IntersectionHandler.prototype.drawBounds = function(graphics) {
	var list = this.getSubBounds();
	for(var i = 0; i < list.length; i++) {
		list[i].drawBounds(graphics);
	}
};

/**
 * Draws the {@link SRL_Shape}.
 *
 * Does nothing by default.
 */
SRL_Shape.prototype.drawShape = function drawShape(graphics, forcedColor) {

	if (this.getIsHighlighted()) {
		forcedColor = this.getHighlightColor();
	}

	var subShapes = this.getSubObjects();
	for (var i = 0; i <subShapes.length; i++) {
		subShapes[i].draw(graphics, forcedColor);
	}
	//var rectangle = this.getBoundingBox().drawBounds(graphics);
};

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
};

SRL_Sketch.prototype.canvasContext = false;

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

// FANCY DRAWING
/*******************
	PATH OBJECT
*******************/

function path() {
	/**
     * Creates a vector, normalizes it and stores it in {@code point}.
     */
    function createNormalizedVector(p1x, p1y, p2x, p2y) {
        dx = p2x - p1x;
        dy = p2y - p1y;
        normal = 1.0 / Math.sqrt(dx * dx + dy * dy);
        return {
	        x: dx * normal,
	        y: dy * normal
	      };
    }

    /**
     * Returns the previous midpoint.
     */
	this.makeThickCurvedPath = function(p1x, p1y, r1, p2x, p2y, r2, p3x, p3y, r3, pastMidMidpoint, color) {
	        // Description of algorithm:
	        // Chooses tangents the same way Catmull-Rom does, using the vector from the prior control
	        // point to the next control point as the direction vector (ignoring the current point).
	        // This uses the quadratic midpoints (computed by De Casteljau's method effectively
	        // splitting the quadratic beziers defined by the input points).

	        // Creates a mid point from the actual input points.
	        midpoint1 = midPoint(p1x, p1y, p2x, p2y);
	        midpoint2 = midPoint(p2x, p2y, p3x, p3y);
	        midMidPoint = midPoint(midpoint1.x, midpoint1.y, midpoint2.x, midpoint2.y);

	        // Creates the enter and exit and midpoint vector.
	        enterVector = createNormalizedVector(midpoint1.x, midpoint1.y, pastMidMidpoint.x,
	                pastMidMidpoint.y);
	        midpointVector = createNormalizedVector(midMidpoint.x, midMidpoint.y, pastMidMidpoint.x,
	                pastMidMidpoint.y, mMidpointVector);
	        exitVector = createNormalizedVector(midMidpoint.x, midMidpoint.y, midpoint1.x,
	                midpoint1.y);

	        // Approx of radius between points.
	        // TODO: Calculate better approximation.
	        midRadius = (r1 + r2) * 0.5;

	        // Creates points at distance r from the spine.
	        pointSet1 = calculatePerpendicularOffsets(mPastMidMidpoint.x, mPastMidMidpoint.y, r1,
	                mEnterVector);
	        pointSet2 = calculatePerpendicularOffsets(mMidpoint1.x, mMidpoint1.y, midRadius,
	                mMidpointVector);
	        pointSet3 = calculatePerpendicularOffsets(mMidMidpoint.x, mMidMidpoint.y, r2, mExitVector);

	        canvas.fillStyle = color;
	        canvas.beginPath();

	        // Starting location.
	        canvas.moveTo(pointSet1.p1.x, pointSet1.p1.y);
	        canvas.quadTo(pointSet2.p1.x,pointSet2.p1.y,pointSet3.p1.x,pointSet3.p1.y);
	        // Ending arc.
	        extendedX = midMidpoint.x - r1 * exitVector.x;
	        extendedY = midMidpoint.y - r1 * exitVector.y;
	        canvas.quadTo(extendedX, extendedY, pointSet3.p2.x, pointSet3.p2.y);
	        // Reverse quad.
	        canvas.quadTo(pointSet2.p2.x, pointSet2.p2.y, pointset1.p2.x, pointset1.p2.y);
	        canvas.lineTo(pointSet1.p1.x, pointSet1.p1.y);
	        canvas.fill();

	        return midMidpoint;  // the new past point.
	    }

	/**
	 * Returns the midpoint fo the 2 given points.
	 */
	function midPoint(p1x, p1y, p2x, p2y) {
		return {
	        x: (p1x + p2x) * 0.5,
	        y: (p1y + p2y) * 0.5
	      };
	}

	/**
     * Calculates the point's offset from the spine with the given radius.
     *
     * There are 4 values, 2 points (x,y), created from the input.
     *
     * @param x The X location of the point.
     * @param y The Y location of the point.
     * @param r The radius of the given point. This will be the distance between
     *            the input point and the output points
     * @param vector Tangent to the slope at this point.
     */
    function calculatePerpendicularOffsets(inputX, inputY, r, vector) {
        // First point.
    	return {
    			p1: {
    				x: inputX - r * vector.y,
    				y: inputY + r * vector.x
    			},
    			p2:	{
    				x: inputX + r * vector.y,
    				y: inputY - r * vector.x
    			}
    		};
    }
}