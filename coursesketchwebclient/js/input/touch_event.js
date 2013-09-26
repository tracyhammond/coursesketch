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
		INPUT EVENTS
	*******************/

	this.draggingStart = function draggingStart(event)  {
		this.dragging = true;
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'draggingStart');
		if (this.inputDraggingStartListener) {
			this.inputDraggingStartListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
		}
	}

	this.draggingEnd = function draggingEnd(event)  {
		this.dragging = false;
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'draggingEnd');
		if (this.inputDraggingEndListener) {
			this.inputDraggingEndListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
		}
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
		if (this.inputMovedListener) {
			this.inputMovedListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
		}
	}

	this.mouseDragged = function mouseDragged(event)  {
		if(!this.dragging) {
			this.draggingStart(event);
			return;
		}
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputDragged');
		if (this.inputDraggedListener) {
			this.inputDraggedListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
		}
	}

	this.mouseEnter = function mouseEnter(event) {
		this.touchInBounds = true;
		var touchPos = this.getTouchPos(event);
		var time = this.createTimeStamp();
		var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputEnter');
		if (this.inputEnterListener) {
			this.inputEnterListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
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
		if (this.inputExitListener) {
			this.inputExitListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
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
		if (this.inputDownListener) {
			this.inputDownListener(newEvent);
		} else if(this.unifiedInputListener) {
			this.unifiedInputListener(newEvent);
		}
	}

	this.mouseUp = function mouseUp(event)  {
		if (this.dragging) {
			this.draggingEnd(event);
		}

		if(this.touchInBounds && this.touchDown) {
			this.touchDown = false;
			var touchPos = this.getTouchPos(event);
			var time = this.createTimeStamp();
			var newEvent = new inputEvent(touchPos.x, touchPos.y, time, 0.5, 0.5, 'inputUp');
			if (this.inputUpListener) {
				this.inputUpListener(newEvent);
			} else if(this.unifiedInputListener) {
				this.unifiedInputListener(newEvent);
			}
		}
	}

	/**
	 * Sets the default listener, this is only called if a unique listener is not set.
	 */
	this.setUnifiedListener = function(callBack) {
		this.unifiedInputListener = callBack;
	}

	/**
	 * Sets the listener for input moved.
	 * call this method with false to remove the listener.
	 */
	this.setInputMovedListener = function(listener) {
		this.inputMovedListener = listener
	}

	/**
	 * Sets the listener for input dragged.
	 * call this method with false to remove the listener.
	 */
	this.setInputDraggedListener = function(listener) {
		this.inputDraggedListener = listener
	}
	
	/**
	 * Sets the listener for the start of dragging.
	 * call this method with false to remove the listener.
	 */
	this.setDraggingStartListener = function(listener) {
		this.inputDraggingStartListener = listener
	}

	/**
	 * Sets the listener for the end of dragging.
	 * call this method with false to remove the listener.
	 */
	this.setDraggingEndListener = function(listener) {
		this.inputDraggingEndListener = listener
	}

	/**
	 * Sets the listener for input enter.
	 * call this method with false to remove the listener.
	 */
	this.setInputEnterListener = function(listener) {
		this.inputEnterListener = listener
	}

	/**
	 * Sets the listener for input exit.
	 * call this method with false to remove the listener.
	 */
	this.setInputExitListener = function(listener) {
		this.inputExitListener = listener
	}

	/**
	 * Sets the listener for input down.
	 * call this method with false to remove the listener.
	 */
	this.setInputDownListener = function(listener) {
		this.inputDownListener = listener
	}

	/**
	 * Sets the listener for input up.
	 * call this method with false to remove the listener.
	 */
	this.setInputUpListener = function(listener) {
		this.inputUpListener = listener
	}

	/***********
		DATA
	***********/

	var touchDown = false;
	var touchInBounds = false;
	var canvas = false; // The HTML5 element where sketching happens.
	var canvasBounds = false;
	var context = false;
	var inputElement = false; // The element that needs touch input.
	var dragging = false;
	
	var unifiedInputListener = false;
	var inputMovedListener = false;
	var inputDraggedListener = false;
	var inputEnterListener = false;
	var inputExitListener = false;
	var inputDownListener = false;
	var inputUpListener = false;
	var inputDraggingStartListener = false;
	var inputDraggingEndListener = false;
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
