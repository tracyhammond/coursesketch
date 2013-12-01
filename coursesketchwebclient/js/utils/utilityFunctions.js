/**
 **************************************************************
 *
 *
 * Utility Functions
 * @author gigemjt
 * 
 *
 *
 **************************************************************
 */

// Used to detirmine if the browser is touch capable
var is_touch = 'ontouchstart' in document.documentElement;

/*
 * Ironically check to see if a function that checks to see if object are undefined is undefined
 */
if (typeof isUndefined === 'undefined') {
	function isUndefined(object) {
		return typeof object === "undefined";
	}
}

/**
 **************************************************************
 * Inheritence Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * A method of function that is used to do prototyping.
 */
if (isUndefined(Function.prototype.Inherits)) {
	Function.prototype.Inherits = function(parent) {
		this.prototype =
			new parent();
		this.prototype.constructor = this;
	}
}

/**
 * Sets up a method that is called in the constructor (like super in java)
 */
if (isUndefined(Object.prototype.Inherits)) {
	Object.prototype.Inherits = function(parent) {
		if( arguments.length > 1 ) {
			parent.apply( this, Array.prototype.slice.call( arguments, 1 ) );
		}
		else {
			parent.call( this );
		}
	}
}

/**
 **************************************************************
 * Array Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * removes the object from an array.
 *
 * @return the object that was removed if it exist.
 */
if (isUndefined(Array.prototype.removeObject)) {
	Array.prototype.removeObject = function(object) {
		var index = this.indexOf(object);
		if (index != -1) {
			var result = this[index];
			this.splice(index, 1);
			return result;
		}
		throw "attempt to remove invalid object";
	};
}

/**
 * removes the object from an array.
 *
 * @return the object that was removed if it exist.
 */
if (isUndefined(Array.prototype.removeObjectByIndex)) {
	Array.prototype.removeObjectByIndex = function(index) {
		if (index != -1) {
			var result = this[index];
			this.splice(index, 1);
			return result;
		}
		throw "attempt to remove at invalid index";
	};
}

/**
 * Checks to see if an item is an instance of an array.
 *
 * returns true if it is an array, (hopefully).
 */
if (isUndefined(isArray)) {
	function isArray(object) {
		return object instanceof Array ||
		(Array.isArray && Array.isArray(object));
	};
}	

/**
 * Makes a map out of the list.
 *
 * @param map
 * @returns {Array}
 */
if (isUndefined(getMapAsList)) {
	function getMapAsList(map) {
		var list = new Array();
		for(key in map) {
			list.push(map[key]);
		}
		return list;
	}
}
/**
 **************************************************************
 * Sketch Function
 * @author gigemjt
 **************************************************************
 */

/**
 * Generates an rfc4122 version 4 compliant solution.
 *
 * found at http://stackoverflow.com/a/2117523/2187510
 * and further improved at
 * http://stackoverflow.com/a/8809472/2187510
 */
if (isUndefined(generateUUID)) {
	function generateUUID() {
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
	    });
	    return uuid;
	};
}

/**
 * Creates a number that represents the current time in milliseconds since jan 1st 1970.
 */
if (isUndefined(createTimeStamp)) {
	// Creates a time stamp every time this method is called.
	function createTimeStamp() {
		return new Date().getTime();
	}
}

/**
 **************************************************************
 * String Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * Replaces all instances of {@code find} with {@code replace}.
 */
if (isUndefined(String.prototype.replaceAll)) {
	String.prototype.replaceAll = function(find, replace) {
		return this.replace(new RegExp(find, 'g'), replace);
	};
}

if (isUndefined(replaceAll)) {
	 function replaceAll(find, replace, src) {
		 return src.replace(new RegExp(find, 'g'), replace);
	};
}

/**
 **************************************************************
 * Debug Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * prints all of the properties of a given object
 */
if (isUndefined(printProperties)) {
	 function printProperties(object) {
		for(obj in object) {
			console.log(obj + ' : ' + object[obj]);
		} 
	};
}

/*******************************
 *
 * Color Functions
 * @author gigemjt
 *
 *******************************
 */
if (isUndefined(convertRGBtoHex)) {
	function convertRGBtoHex(a, r, g, b) {
		return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
	}
}

if (isUndefined(convertHexToRgb)) {
	function convertHexToRgb(hex) {
	    // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
	    var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
	    hex = hex.replace(shorthandRegex, function(m, r, g, b) {
	        return r + r + g + g + b + b;
	    });
	
	    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
	    return result ? {
	        r: parseInt(result[1], 16),
	        g: parseInt(result[2], 16),
	        b: parseInt(result[3], 16)
	    } : null;
	}
}

/**
 **************************************************************
 * Placement utility Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * Takes the height and width of this element and expands it to fill the size of the screen.
 */
if (isUndefined(fillScreen)) {
	function fillScreen(id) {
		var body = document.body,
	    html = document.documentElement;
		var height = window.innerHeight - 4;
		var width = window.innerWidth - 4;
		var element = document.getElementById(id);
		element.height = height - element.offsetTop;
		element.width = width  - element.offsetLeft;
		element.style.width = element.width;
		element.style.height = element.height;
	}
}

/**
 * Takes the height of this element and expands it to fill the size of the screen.
 */
if (isUndefined(fillHeight)) {
	function fillHeight(id) {
		var body = document.body,
	    html = document.documentElement;
		var height = window.innerHeight - 4;
		var element = document.getElementById(id);
		element.height = height - element.offsetTop;
		element.style.height = element.height;
	}
}

/**
 * Takes the width of this element and expands it to fill the size of the screen.
 */
if (isUndefined(fillWidth)) {
	function fillWidth(id) {
		var body = document.body,
	    html = document.documentElement;
		var width = window.innerWidth - 4;
		var element = document.getElementById(id);
		element.width = width  - element.offsetLeft;
		element.style.width = element.width;
	}
}

/**
 **************************************************************
 * Utility utility Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * Checks to see if the given object is a function.
 *
 * returns true if the object is a function.
 */
if (isUndefined(isFunction)) {
	function isFunction(object) {
		return typeof(object) === 'function';
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
 * copies all of the utility functions from parent scope to child scope.
 */
if (isUndefined(copyParentUtilityFunctions)) {
	function copyParentUtilityFunctions(scope) {
		// needed values to continue this process!
		copyParentValues(scope, 'isUndefined');
		copyParentValues(scope, 'isFunction');
		copyParentValues(scope, 'copyParentValues', true);
		copyParentValues(scope, 'copyParentUtilityFunctions', true);

		copyParentValues(scope, 'replaceAll');

		copyParentValues(scope, 'isArray');
		copyParentValues(scope, 'getMapAsList');

		copyParentValues(scope, 'createTimeStamp');
		copyParentValues(scope, 'generateUUID');
		copyParentValues(scope, 'is_touch');

		copyParentValues(scope, 'convertHexToRgb');
		copyParentValues(scope, 'convertRGBtoHex');

		copyParentValues(scope, 'printProperties');
	}
}