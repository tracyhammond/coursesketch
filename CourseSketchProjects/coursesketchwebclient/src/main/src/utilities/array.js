/* depends on base.js */

/**
 * ************************************************************* Array Functions
 * 
 * @author gigemjt *************************************************************
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
        throw new Error("attempt to remove invalid object");
    };
}

if (isUndefined(removeObjectFromArray)) {
    function removeObjectFromArray(array, object) {
        var index = array.indexOf(object);
        if (index != -1) {
            var result = array[index];
            array.splice(index, 1);
            return result;
        }
        throw new Error("attempt to remove invalid object");
    }
    ;
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
        throw new Error("attempt to remove at invalid index");
    };
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
 * Makes a map out of the list.
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
