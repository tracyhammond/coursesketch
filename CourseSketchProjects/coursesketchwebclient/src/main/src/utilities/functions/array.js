/* depends on base.js */
// jshint undef:false
// jshint latedef:false

/**
 * *************************************************************
 *
 * Array Functions
 *
 * @author gigemjt
 *
 ***************************************************************
 */

/**
 * Creates an ArrayException object that returns exception values.
 *
 * @constructor ArrayException
 * @extends BaseException
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} cause - The cause of the exception.
 */
function ArrayException(message, cause) {
    this.name = 'ArrayException';
    this.specificMessage = message;
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}
ArrayException.prototype = new BaseException();

if (isUndefined(removeObjectFromArray)) {

    /**
     * Removes the object from an array.
     *
     * @param {Array<*>} array - The array that the object is being removed from.
     * @param {*} object - The object that is being removed from the array.
     * @returns {*} the object that was removed if it exist.
     */
    function removeObjectFromArray(array, object) {
        var index = array.indexOf(object);
        if (index !== -1) {
            var result = array[index];
            array.splice(index, 1);
            return result;
        }
        throw new ArrayException('attempt to remove invalid object');
    }
}

if (isUndefined(removeObjectByIndex)) {

    /**
     * Removes the object from an array.
     *
     * @param {Array<*>} array - The array that the object is being removed from.
     * @param {Number} index - The index at which the item is being removed.
     * @returns {*} the object that was removed if it exist.
     */
    function removeObjectByIndex(array, index) {
        if (index !== -1) {
            var result = array[index];
            array.splice(index, 1);
            return result;
        }
        throw new ArrayException('attempt to remove at invalid index');
    }
}

if (isUndefined(isArray)) {

    /**
     * Checks to see if an item is an instance of an array.
     *
     * @param {Object} object - The item that is being checked.
     * @returns {Boolean} true if it is an array, (hopefully).
     */
    function isArray(object) {
        return object instanceof Array || (Array.isArray && Array.isArray(object));
    }
}

if (isUndefined(getMapAsList)) {

    /**
     * Makes a list out of an object map.
     *
     * @param {Object} map - The map that you want to convert into a list. (Note this is an object and not ES6 Map
     * @returns {Array} A list of the values in the object.
     */
    function getMapAsList(map) {
        var list = [];
        for (var key in map) {
            if (key !== 'Inherits') {
                list.push(map[key]);
            }
        }
        return list;
    }
}

// Taken from: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
// remove when es6 is supported basically everywhere
if (!Array.prototype.includes) {
    Array.prototype.includes = function(searchElement /*, fromIndex*/) {
        'use strict';
        var O = Object(this);
        var len = parseInt(O.length) || 0;
        if (len === 0) {
            return false;
        }
        var n = parseInt(arguments[1]) || 0;
        var k;
        if (n >= 0) {
            k = n;
        } else {
            k = len + n;
            if (k < 0) {k = 0;}
        }
        var currentElement;
        while (k < len) {
            currentElement = O[k];
            if (searchElement === currentElement ||
                (searchElement !== searchElement && currentElement !== currentElement)) { // NaN !== NaN
                return true;
            }
            k++;
        }
        return false;
    };
}
