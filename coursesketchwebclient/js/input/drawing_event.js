/*********
	METHODS
**********/

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
