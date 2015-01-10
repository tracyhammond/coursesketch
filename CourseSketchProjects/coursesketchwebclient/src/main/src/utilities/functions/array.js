/* depends on base.js */

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
function ArrayException(message) {
    this.name = "ArrayException";
    this.specificMessage = message;
    this.message = "";
    this.htmlMessage = "";
}
ArrayException.prototype = BaseException;

/**
 * removes the object from an array.
 *
 * @return the object that was removed if it exist.
 */
if (isUndefined(removeObjectFromArray)) {
    function removeObjectFromArray(array, object) {
        var index = array.indexOf(object);
        if (index != -1) {
            var result = array[index];
            array.splice(index, 1);
            return result;
        }
        throw new ArrayException("attempt to remove invalid object");
    }
}

/**
 * removes the object from an array.
 *
 * @return the object that was removed if it exist.
 */
if (isUndefined(removeObjectByIndex)) {
    function removeObjectByIndex(array, index) {
        if (index != -1) {
            var result = array[index];
            array.splice(index, 1);
            return result;
        }
        throw new ArrayException("attempt to remove at invalid index");
    }
}

/**
 * Checks to see if an item is an instance of an array.
 *
 * returns true if it is an array, (hopefully).
 */
if (isUndefined(isArray)) {
    function isArray(object) {
        return object instanceof Array || (Array.isArray && Array.isArray(object));
    };
}

/**
 * Makes a list out of an object map.
 *
 * @param map
 * @returns {Array}
 */
if (isUndefined(getMapAsList)) {
    function getMapAsList(map) {
        var list = new Array();
        for (key in map) {
            if (key != 'Inherits') {
                list.push(map[key]);
            }
        }
        return list;
    }
}
