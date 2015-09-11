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
     * @return {*} the object that was removed if it exist.
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
     * @return {*} the object that was removed if it exist.
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
     * @param {Object} map The map that you want to convert into a list. (Note this is an object and not ES6 Map
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
