function inputListener() {

	/**************
		METHODS
	**************/

	this.initialize = function initialize(canvasId) {
		this.canvas = document.getElementById(canvasId);
		this.canvasBounds = this.canvas.getBoundingClientRect();
		this.registerListeners(this.canvas);
	 	this.context = this.canvas.getContext('2d');
	}

	// Registers all of the touch listeners
	this.registerListeners = function registerListeners(canvas) {
		canvas.addEventListener('mousemove',
			function(event){this.listenerScope.mouseMoved(event);}, false);
		canvas.addEventListener('mousedown',
		function(event){this.listenerScope.mouseDown(event);}, false);
		canvas.addEventListener('mouseup',
		function(event){this.listenerScope.mouseUp(event);}, false);
		canvas.addEventListener ("mouseout",
		function(event){this.listenerScope.mouseExit(event);}, false);
		canvas.addEventListener('touchend', 
		function(event){this.listenerScope.mouseUp(event);}, false);
		canvas.addEventListener('touchstart', 
		function(event){this.listenerScope.mouseDown(event);}, false);
		canvas.addEventListener('touchmove', 
		function(event){this.listenerScope.mouseMoved(event);}, false);
		canvas.listenerScope = this;
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
		/*
		var xCoord = -1;
		var yCoord = -1;
		if (event.x != undefined && event.y != undefined)
		{
			xCoord = event.x;
			yCoord = event.y;
		}
		else // Firefox method to get the position
		{
			xCoord = event.clientX + document.body.scrollLeft +
					document.documentElement.scrollLeft;
			yCoord = event.clientY + document.body.scrollTop +
					document.documentElement.scrollTop;
		}
		xCoord -= this.canvas.offsetLeft;
		yCoord -= this.canvas.offsetTop;
        return {
          x: xCoord,
          y: yCoord
        };
        */
        
        this.canvasBounds = this.canvas.getBoundingClientRect();
		var scrollLeft = (window.pageXOffset !== undefined) ? window.pageXOffset : (document.documentElement || document.body.parentNode || document.body).scrollLeft;
		var scrollTop = (window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;
	        return {
 	          x: event.pageX - this.canvasBounds.left - scrollLeft,
 	          y: event.pageY - this.canvasBounds.top - scrollTop
 	        };
	}

	this.getThickness = function getThickness(speed, size, pressure, index, lastStroke, type) {

	}

	/*******************
		MOUSE EVENTS
	*******************/

	this.mouseMoved = function mouseMoved(event) {
		if(!this.touchInBounds) {
			this.mouseEnter(event);
			return;
		}
		if(this.touchDown) {
			this.mouseDragged(event);
			return;
		}
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputMoved');
		if(this.inputListener) {
			this.inputListener(newEvent);
		}
	}
	
	this.mouseDragged = function mouseDragged(event)  {
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputDragged');
		if(this.inputListener) {
			this.inputListener(newEvent);
		}
	}
	
	this.mouseEnter = function mouseEnter(event) {
		this.touchInBounds = true;
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputEnter');
		if(this.inputListener) {
			this.inputListener(newEvent);
		}
	}
	

	this.mouseDown = function mouseDown(event)  {
		this.touchDown = true;
		if(!this.touchInBounds) {
			this.mouseEnter(event);
		}
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputDown');
		if(this.inputListener) {
			this.inputListener(newEvent);
		}
	}
	
	this.mouseUp = function mouseUp(event)  {
		if(this.touchInBounds) {
			this.touchDown = false;
			var touchPos = this.getTouchPos(event);
			var time = this.createTimeStamp();
			var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputUp');
			if(this.inputListener) {
				this.inputListener(newEvent);
			}
		}
	}
	
	this.mouseExit = function mouseExit(event)  {
		if(this.touchDown) {
			this.mouseUp(event);
		}
		this.touchInBounds = false;
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputExit');
		if(this.inputListener) {
			this.inputListener(newEvent);
		}
	}

	this.setListener = function (callBack) {
		this.inputListener = callBack;
	}
	
	/***********
		DATA
	***********/

	this.touchDown = false;
	this.touchInBounds = true;
	this.canvas; // The HTML5 element where sketching happens.
	this.canvasBounds;
	this.context;
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
 * type: the type is either inputExit, inputUp, inputDown, inputMoved, inputEnter, inputDragged
 */
function inputEvent(x, y, time, pressure, size, inputType) {
	this.x = x;
	this.y = y;
	this.time = time;
	this.pressure = pressure;
	this.size = size;
	this.inputType = inputType;
}
