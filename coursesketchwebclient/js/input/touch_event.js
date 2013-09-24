function inputListener() {

	/**************
		METHODS
	**************/

	this.initializeCanvas = function initializeCanvas(canvasId) {
		this.canvas = document.getElementById(canvasId);
		this.canvasBounds = this.canvas.getBoundingClientRect();
		this.registerListeners(this.canvas);
	 	this.context = this.canvas.getContext('2d');
	}

	this.initializeElement = function initializeElement(elementId) {
		this.inputElement = document.getElementById(elementId);
		this.registerListeners(this.inputElement);
	}

	// Registers all of the touch listeners
	this.registerListeners = function registerListeners(element) {
		element.addEventListener('mousemove',
			function(event){this.listenerScope.mouseMoved(event);}, false);
		element.addEventListener('mousedown',
		function(event){this.listenerScope.mouseDown(event);}, false);
		element.addEventListener('mouseup',
		function(event){this.listenerScope.mouseUp(event);}, false);
		element.addEventListener ("mouseout",
		function(event){this.listenerScope.mouseExit(event);}, false);
		element.addEventListener('touchend', 
		function(event){this.listenerScope.mouseUp(event);}, false);
		element.addEventListener('touchstart', 
		function(event){this.listenerScope.mouseDown(event);}, false);
		element.addEventListener('touchmove', 
		function(event){this.listenerScope.mouseMoved(event);}, false);
		element.listenerScope = this;
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
        if(this.canvas) {
        	/*
        	 * TODO: delete if browser compatibility is fixed
        	 */
     //   	var scrollLeft = (window.pageXOffset !== undefined) ? window.pageXOffset : (document.documentElement || document.body.parentNode || document.body).scrollLeft;
    //		var scrollTop = (window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop;
	  //      this.canvasBounds = this.canvas.getBoundingClientRect();
	        return {
 	          x: event.pageX - this.inputElement.offsetLeft,//this.canvasBounds.left - scrollLeft,
 	          y: event.pageY - this.inputElement.offsetTop//this.canvasBounds.top - scrollTop
 	        };
        } else {
        	return {
   	          x: event.pageX - this.inputElement.offsetLeft,
   	          y: event.pageY - this.inputElement.offsetTop
   	        };
        }
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
	this.touchInBounds = false;
	this.canvas = false; // The HTML5 element where sketching happens.
	this.canvasBounds = false;
	this.context = false;
	this.inputListener = false;
	this.inputElement = false; // The element that needs touch input.
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
