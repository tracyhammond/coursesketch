/* Depends on base.js */

/**
 **************************************************************
 * Inheritence Functions
 * @author gigemjt
 **************************************************************
 */

/**
 * @Method
 * sets up inheritance for functions
 * 
 * this.Inherits(SuperClass); // super call inside object
 * AND
 * SubClass.Inherits(SuperClass);
 */
Function.prototype.Inherits = function(parent) {
	this.prototype = new parent();
	this.prototype.constructor = this;
	this.prototype.superConstructor = function() {
		if (arguments.length >= 1) {
			parent.apply(this, Array.prototype.slice.call(arguments, 0));
		}
		else {
			parent.call(this);
			console.log(this);
		}
	};
};

/**
 **************************************************************
 * Object Functions
 * @author gigemjt
 **************************************************************
 */
if (isUndefined(makeValueReadOnly)) {
	function makeValueReadOnly(obj, property, value) {
		if (typeof property != "string") {
			throw new Error("property argument must be a string");
		}
		Object.defineProperty(obj, property, {
		    value: value,
		    writable: false
		});
	}
}