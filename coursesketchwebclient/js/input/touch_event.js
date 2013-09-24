function inputListener() {

	/**************
		METHODS
	**************/

	this.initialize = function initialize() {
		canvas = document.getElementById('canvas');
		canvasBounds = canvas.getBoundingClientRect();
		this.registerListeners(canvas);
	 	context= canvas.getContext('2d');
	}

	// Registers all of the touch listeners
	this.registerListeners = function registerListeners(canvas) {
		canvas.addEventListener('mousemove', this.mouseMoved, false);
		canvas.addEventListener('mousedown', this.mouseDown, false);
		canvas.addEventListener('mouseup', this.mouseUp, false);
		canvas.addEventListener ("mouseout", this.mouseLeave, false);
		canvas.addEventListener('touchend', this.mouseUp);
		canvas.addEventListener('touchstart', this.mouseDown);
		canvas.addEventListener('touchmove', this.mouseMoved);
	}

	/*********************
	UTILITY METHODS
	*********************/
	// Creates a time stamp for every point.
	this.createTimeStamp = function createTimeStamp() {
		return new Date().getTime();
	}

	// gets the mouse position on the screen
	// subtracts the location of the box
	// subtracts how much the user has scrolled on the page
	this.getTouchPos = function getTouchPos(event) {
		canvasBounds = canvas.getBoundingClientRect();
		var scrollLeft = (window.pageXOffset !== undefined) ? window.pageXOffset : (document.documentElement || document.body.parentNode || document.body).scrollLeft;
		var scrollTop = (window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;
	        return {
	          x: event.pageX - canvasBounds.left - scrollLeft,
	          y: event.pageY - canvasBounds.top - scrollTop
	        };
	}

	this.getThickness = function getThickness(speed, size, pressure, index, lastStroke, type) {

	}

	/*******************
		MOUSE EVENTS
	*******************/

	this.mouseMoved = function mouseMoved(event) {
		if(!touchInBounds) {
			this.mouseEnter(event);
		}
		if(this.touchDown) {
			mouseDragged(event);
			return;
		}
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputMoved');
		if(inputListenter) {
			inputListener(newEvent);
		}
	}
	
	this.mouseDragged = function mouseDragged(event)  {
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputDragged');
		if(inputListenter) {
			inputListener(newEvent);
		}
	}
	
	this.mouseEnter = function mouseEnter(event) {
		this.touchInBounds = true;
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputEnter');
		if(inputListenter) {
			inputListener(newEvent);
		}
	}
	

	this.mouseDown = function mouseDown(event)  {
		this.touchDown = true;
		if(!this.touchInBounds) {
			this.mouseEnter(event);
		}
		var touchPos = getTouchPos(event);
		var time = createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputDown');
		if(inputListenter) {
			inputListener(newEvent);
		}
	}
	
	this.mouseUp = function mouseUp(event)  {
		if(this.touchInBounds) {
			this.touchDown = false;
			var touchPos = getTouchPos(event);
			var time = createTimeStamp();
			var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputUp');
			if(inputListenter) {
				inputListener(newEvent);
			}
		}
	}
	
	this.mouseLeave = function mouseLeave(event)  {
		if(this.touchDown) {
			mouseUp(event);
		}
		this.touchInBounds = false;
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputLeave');
		if(inputListenter) {
			inputListener(newEvent);
		}
	}

	/***********
		DATA
	***********/

	this.touchDown = false;
	this.touchInBounds = true;
	this.canvas; // The HTML5 element where sketching happens.
	this.canvasBounds;

	this.inputListener = false;
}

/**
 * Creates an object wich passed to caller events with the given values.
 * x: the x coordinate at which the point is created.
 * y: the y coordinate at which the point is created.
 * time: the time at which the point is created.
 * pressure: the pressure of the point, devices without pressure default to 0.5
 * 			This value is always between 0 and 1
 * size: the size of the input for the point, devices without size default to 0.5
 *  		This value is always between 0 and 1
 * type: the type is either inputLeave, inputUp, inputDown, inputMoved, inputEnter, inputDragged
 */
function inputEvent(x, y, time, pressure, size, type) {

}
