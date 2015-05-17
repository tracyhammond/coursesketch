/* Depends on base.js */
// jshint undef:false
// jshint latedef:false

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
 * @function Inherits
 * sets up inheritance for functions
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
        /**
         * Super constructor
         * @type {Function}
         */
        localConstructor = localScope.prototype.superConstructor = function() {
            // special setting
            this.superConstructor = parentConstructor;
            // console.log('Setting parent constructor' + parent);
            if (arguments.length >= 1) {
                Parent.apply(this, Array.prototype.slice.call(arguments, 0));
            } else {
                Parent.apply(this);
            }
            // console.log('Setting back to current constructor' +
            // localConstructor);
            this.superConstructor = localConstructor;
        };
    } else {
        /**
         * superConstructor
         */
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
    /**
     * Makes a value readonly
     * @param {Object} obj The object this is applying to
     * @param {String} property The property that is being defined as read only.
     * @param {*} value The value that is returned when the property is accessed.
     */
    function makeValueReadOnly(obj, property, value) {
        if (typeof property !== 'string') {
            throw new Error('property argument must be a string');
        }
        Object.defineProperty(obj, property, {
            value:      value,
            writable:   false
        });
    }
}
