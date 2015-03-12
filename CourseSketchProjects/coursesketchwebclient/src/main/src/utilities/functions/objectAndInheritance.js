/* Depends on base.js */

/**
 * *************************************************************
 *
 * Inheritance Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

/**
 * @Method sets up inheritance for functions
 *
 * this.Inherits(SuperClass); // super call inside object AND
 * SubClass.Inherits(SuperClass);
 */
Function.prototype.Inherits = function(Parent) {
    var localScope = this;
    localScope.prototype = new Parent();
    localScope.prototype.constructor = localScope;
    if (!isUndefined(Parent.prototype.superConstructor)) {
        var parentConstructor = Parent.prototype.superConstructor;
        var localConstructor = undefined;
        localConstructor = localScope.prototype.superConstructor = function() {
            // special setting
            this.superConstructor = parentConstructor;
            // console.log("Setting parent constructor" + parent);
            if (arguments.length >= 1) {
                Parent.apply(this, Array.prototype.slice.call(arguments, 0));
            } else {
                Parent.apply(this);
            }
            // console.log("Setting back to current constructor" +
            // localConstructor);
            this.superConstructor = localConstructor;
        };
    } else {
        localScope.prototype.superConstructor = function() {
            if (arguments.length >= 1) {
                Parent.apply(this, Array.prototype.slice.call(arguments, 0));
            } else {
                Parent.apply(this);
            }
        };
    }
};

/**
 * *************************************************************
 *
 * Object Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */
if (isUndefined(makeValueReadOnly)) {
    function makeValueReadOnly(obj, property, value) {
        if (typeof property !== "string") {
            throw new Error("property argument must be a string");
        }
        Object.defineProperty(obj, property, {
            value:      value,
            writable:   false
        });
    }
}
