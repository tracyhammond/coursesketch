/*
 * Ironically check to see if a function that checks to see if objects are undefined is undefined
 */
if (typeof isUndefined === 'undefined') {
	function isUndefined(object) {
		return typeof object === "undefined";
	}
}

/**
 * *************************************************************
 * 
 * Sketch Function
 * 
 * @author gigemjt
 * 
 * *************************************************************
 */

/**
 * Generates an rfc4122 version 4 compliant solution.
 * 
 * found at http://stackoverflow.com/a/2117523/2187510 and further improved at
 * http://stackoverflow.com/a/8809472/2187510
 */
if (isUndefined(generateUUID)) {
	function generateUUID() {
		var d = new Date().getTime();
		var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
				function(c) {
					var r = (d + Math.random() * 16) % 16 | 0;
					d = Math.floor(d / 16);
					return (c == 'x' ? r : (r & 0x7 | 0x8)).toString(16);
				});
		return uuid;
	}
	;
}

/**
 * Creates a number that represents the current time in milliseconds since jan
 * 1st 1970.
 */
if (isUndefined(createTimeStamp)) {
	// Creates a time stamp every time this method is called.
	function createTimeStamp() {
		return new Date().getTime();
	}
}

/**
 * *************************************************************
 * 
 * onLoad utility Functions
 * 
 * @author gigemjt
 * 
 * *************************************************************
 */

/**
 * Creates a recursive set of functions that are all called onload
 * 
 * The scope is the target for the onload
 */
function addScopedLoadEvent(scope, func) {
	var oldonload = scope.onload;
	if (typeof scope.onload != 'function') {
		scope.onload = func;
	} else {
		scope.onload = function() {
			if (oldonload) {
				oldonload();
			}
			func();
		};
	}
}

/**
 * *************************************************************
 * 
 * Utility utility Functions
 * 
 * @author gigemjt
 * 
 * *************************************************************
 */

/**
 * Checks to see if the given object is a function.
 * 
 * returns true if the object is a function.
 */
if (isUndefined(isFunction)) {
	function isFunction(object) {
		return typeof (object) === 'function';
	}
}

/**
 * copies a property from parent scope to child scope
 */
if (isUndefined(copyParentValues)) {
	function copyParentValues(scope, propertyName, bindFunction) {
		var object = this[propertyName];
		// 
		if (isFunction(object) && bindFunction) {
			scope[propertyName] = object.bind(scope);
		} else {
			scope[propertyName] = this[propertyName];
		}
	}
}

/**
 * *************************************************************
 * 
 * Date Functions
 * 
 * @author gigemjt
 * 
 * *************************************************************
 */
if (isUndefined(make2Digits)) {
	function make2Digits(num) {
		return ("0" + Number(num)).slice(-2);
	}
}

if (isUndefined(getMillitaryFormattedDateTime)) {
	function getMillitaryFormattedDateTime(dateTime) {
		var date = make2Digits(dateTime.getMonth() + 1) + "-"
				+ make2Digits(dateTime.getDate()) + "-"
				+ dateTime.getFullYear();
		var time = make2Digits(dateTime.getHours()) + ":"
				+ make2Digits(dateTime.getMinutes());
		return date + ' ' + time;
	}
}

if (isUndefined(getFormattedDateTime)) {
	function getFormattedDateTime(dateTime) {
		var date = make2Digits(dateTime.getMonth() + 1) + "-"
				+ make2Digits(dateTime.getDate()) + "-"
				+ dateTime.getFullYear();
		var hours = dateTime.getHours();
		var timeType = "AM";
		if (dateTime.getHours() > 12) {
			hours -= 12;
		}
		if (dateTime.getHours() >= 12) {
			timeType = "PM";
		}
		if (dateTime.getHours() == 0) {
			hours = 12;
		}
		var time = make2Digits(hours) + ":"
				+ make2Digits(dateTime.getMinutes()) + timeType;
		return date + ' ' + time;
	}
}