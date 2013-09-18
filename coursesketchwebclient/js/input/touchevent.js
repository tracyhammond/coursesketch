/**************
	METHODS
**************/

function initialize() {
	sketchObject = new sketch();
	canvas = document.getElementById('canvas');
	canvasBounds = canvas.getBoundingClientRect();
	registerListeners(canvas);
 	context= canvas.getContext('2d');
	writeEvent();
}

// Registers all of the touch listeners
function registerListeners(canvas) {
	canvas.addEventListener('mousemove', mouseMoved, false);
	canvas.addEventListener('mousedown', mouseDown, false);
	canvas.addEventListener('mouseup', mouseUp, false);
	canvas.addEventListener ("mouseout", mouseLeave, false);
	canvas.addEventListener('touchend', mouseUp);
	canvas.addEventListener('touchstart', mouseDown);
	canvas.addEventListener('touchmove', mouseMoved);
}



function writeEvent() {
	var pointInfo = document.getElementById('pointInfo'),
	stateInfo = document.getElementById('stateInfo'),
	sketchInfo = document.getElementById('sketchInfo');
	if(previousPoint) {
  		pointInfo.innerHTML = 'X: ' + previousPoint.x + ' Y: ' + previousPoint.y + ' Time: ' + previousPoint.timeStamp + ' Speed: ' + previousPoint.speed;
	} else {
		pointInfo.innerHTML = 'No previous point';
	}
  	stateInfo.innerHTML = 'MouseState: ' + (touchDown?'Down':'Up') +', ' + (touchInBounds?'In Bounds':'Out of Bounds') + ' Bounds: ' + canvasBounds.left + ', ' + canvasBounds.top + ', ' + canvasBounds.right + ', ' + canvasBounds.bottom;
	sketchInfo.innerHTML = 'number of strokes: ' + sketchObject.size();
}

function printSketch() {
	var output = document.getElementById('output');
	//document.write();
	string+= '<p>'+JSON.stringify(sketchObject)+'</p>';
	output.innerHTML = string;
	//output.innerHTML = '<p>'+sketch.toJSON()+'</p>';
}

/*********************
SKETCH UTILITY METHODS
*********************/

// Creates a time stamp for every point.
function createTimeStamp() {
	return new Date().getTime();
}

// gets the mouse position on the screen
// subtracts the location of the box
// subtracts how much the user has scrolled on the page
function getTouchPos(event) {
	canvasBounds = canvas.getBoundingClientRect();
	var scrollLeft = (window.pageXOffset !== undefined) ? window.pageXOffset : (document.documentElement || document.body.parentNode || document.body).scrollLeft;
	var scrollTop = (window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;
        return {
          x: event.pageX - canvasBounds.left - scrollLeft,
          y: event.pageY - canvasBounds.top - scrollTop
        };
}

function getThickness(speed, size, pressure, index, lastStroke, type) {

}

/*******************
	MOUSE EVENTS
*******************/

function mouseMoved(event)  {
	if(touchDown && touchInBounds) {
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var speed = 0;
		var addPoint = true;
		if(previousPoint) {
			var distance = previousPoint.getDistance(touchPos.x, touchPos.y);
			if(distance < DISTANCE_THRESHOLD)
				addPoint = false;
			speed = distance / previousPoint.timeDifference(time);
		} else {
			speed = MAX_SPEED;
		}
		if(addPoint) {
			var currentPoint = new point(touchPos.x, touchPos.y, time, 0.5, 0.5, speed);
			currentStroke.addPoint(currentPoint);
			previousPoint = currentPoint;
		}
	}
	writeEvent();
	sketchObject.drawSketch();
}

function mouseDown(event)  {
	touchDown = true;
	touchInBounds = true;
	var touchPos = getTouchPos(event);
	var time = createTimeStamp();
	var currentPoint = new point(touchPos.x, touchPos.y, time, 0.5, 0.5, MAX_SPEED);
	currentStroke = new stroke(currentPoint);
	sketchObject.addStroke(currentStroke);
	previousPoint = currentPoint;
}

function mouseUp(event)  {
	if(touchInBounds) {
		touchDown = false;
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var currentPoint = new point(touchPos.x, touchPos.y, time, 0.5, 0.5, MAX_SPEED);
		currentStroke.addPoint(currentPoint);
		currentStroke = null;
		previousPoint = null;
	}
}

function mouseLeave(event)  {
	if(touchDown) {
		mouseUp(event);
	}
	touchInBounds = false;
	sketchObject.drawSketch();
}

/***********
	DATA
***********/

const MAX_SPEED = 5; // pixels per second.
const DISTANCE_THRESHOLD = 5; // in pixels.
var sketchObject;
var currentStroke;
var previousPoint;
var touchDown = false;
var touchInBounds = true;
var canvas; // The HTML5 element where sketching happens.
var canvasBounds;
var context;

/**************
	OBJECTS
**************/
function point(x, y, timeStamp, pressure, size, speed) {
	this.x = x;
	this.y = y;
	this.timeStamp = timeStamp;
	this.pressure = pressure;
	this.size = size;
	this.speed = speed;
	this.thickness = 0;

	this.getDistance = getDistance;
	function getDistance(x, y) {
		var dx = this.x - x;
		var dy = this.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// Returns a positive number if the given time has a larger value.
	this.timeDifference = timeDifference;
	function timeDifference(time) {
		return time - this.timeStamp;
	}

	function toSkf() {
		var skfString = 'p ';
		skfString+=this.x + ' ' + this.y;
		skfString+=this.timeStamp + ' ' + this.size + ' ' + this.pressure;
		return skfString;
	}
}

function stroke(point) {
	this.pointList = [point];

	this.addPoint = addPoint;
	function addPoint(point) {
		this.pointList.push(point);
	}

	this.size = size;
	function size() {
		return this.pointList.length;
	}

	this.drawStroke = drawStroke;
	function drawStroke() {
		context.beginPath();
		var pastPoint = this.pointList[0];
		context.moveTo(pastPoint.x, pastPoint.y);
		for(var k = 0; k < this.pointList.length; k++) {
			var currentPoint = this.pointList[k];
			context.lineTo(currentPoint.x, currentPoint.y);
		}
		context.lineWidth = 5;
     		context.strokeStyle = 'red';
      		context.stroke();
	}

	function toSkf() {
		var skfString = 's ';
		for(var k = 0; k < this.pointList.length; k++) {
			skfString+= this.pointList[k].toSkf()+ ' ';
		}
		return skfString;
	}
}

function sketch() {
	this.strokeList = [];

	this.addStroke = addStroke;
	function addStroke(stroke) {
		this.strokeList.push(stroke);
	}

	this.size = size;
	function size() {
		return this.strokeList.length;
	}

	this.drawSketch = drawSketch;
	function drawSketch() {
		context.clearRect(0, 0, canvas.width, canvas.height);
		for(var k = 0; k < this.strokeList.length; k++) {
			this.strokeList[k].drawStroke();
		}
	}

	this.toSkf = toSkf;
	function toSkf() {
		var skfString = "";
		for(var k = 0; k < this.strokeList.length; k++) {
			skfString += this.strokeList[k].toSkf()+ ' ';
		}
		return skfString;
	}

}

/*******************
	PATH OBJECT
*******************/

function path() {
	/**
     * Creates a vector, normalizes it and stores it in {@code point}.
     */
	this.createNormalizedVector = createNormalizedVector;
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
     * Returns the previous midpoint
     */
	this.makeThickCurvedPath = makeThickCurvedPath;
	function makeThickCurvedPath(p1x, p1y, r1, p2x, p2y, r2, p3x, p3y, r3, pastMidMidpoint) {
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

	        canvas.fillStyle = "black";
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

	this.midPoint = midPoint;
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
     * @param angle Tangent to the slope at this point.
     * @param points an array to be filled with the points
     * @param offset The location in the array to put the points, should be a
     *            multiple of 4.
     */
	this.calculatePerpendicularOffsets = calculatePerpendicularOffsets;
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
