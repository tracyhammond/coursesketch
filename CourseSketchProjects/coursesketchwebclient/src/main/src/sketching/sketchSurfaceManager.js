/* depends on objectandinheritance.js */

/**
 * @class SketchSurfaceException
 * @extends BaseException
 *
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} [cause] - The cause of the exception.
 */
function SketchSurfaceException(message, cause) {
    this.name = 'SketchSurfaceException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

SketchSurfaceException.prototype = new BaseException();

/**
 * This file contains all of the resources for managing sketches.
 *
 * @class
 */
function SketchSurfaceManager() {
    var TEMPORARY_ID = 'TEMPORARY_ID';
    var sketchMap = new Map();
    var parentSketch = undefined;
    var parentSketchId = undefined;

    /**
     * @type {SRL_Sketch}
     */
    var currentSketch = undefined;

    /**
     * Gets the parent sketch.
     *
     * @return {SRL_Sketch} sketch - The top level sketch.
     */
    this.getParentSketch = function() {
        return this.getSketch(parentSketchId);
    };

    /**
     * Sets the parent sketch.
     *
     * Also adds this sketch to the map, sets the parent sketch id and makes this sketch the current one.
     *
     * @param {SRL_Sketch} sketch - The top level sketch.
     */
    this.setParentSketch = function(sketch) {
        if (!isUndefined(parentSketch)) {
            throw new SketchSurfaceException('Can not have more than one parent sketch at a time');
        }
        if (isUndefined(sketch.id)) {
            sketch.id = TEMPORARY_ID;
        }
        this.setSketch(sketch);
        this.setCurrentSketch(sketch.id);
        parentSketch = sketch;
        this.setParentSketchId(sketch.id);
    };

    /**
     * @returns {Array<String>} all the ids of the sketches managed by this manager in no order.
     */
    this.getSketchIds = function() {
        var keyList = [];
        var iter = sketchMap.keys();
        for (var i = 0; i < sketchMap.size; i++) {
            keyList.push(iter.next().value);
        }
        return keyList;
    };

    /**
     * Sets the id of the parent sketch.
     * A sketch surface can contain multiple sketches but the first sketch object created is the parent sketch.
     *
     * @param {UUID} id - The id of the parent sketch.
     */
    this.setParentSketchId = function(id) {
        if (parentSketchId === TEMPORARY_ID) {
            parentSketch.id = id;
            sketchMap.delete(TEMPORARY_ID);
            this.setSketch(parentSketch);
        }        else if (!isUndefined(parentSketchId)) {
            throw new SketchSurfaceException('Can not change the parent sketch id if the parent sketch still exists');
        }

        parentSketchId = id;
    };

    /**
     * Adds the sketch with its Id to the list of sketches related to this sketch surface.
     * (replaces an old sketch with the same id if it already exist in the list).
     *
     * @param {SRL_Sketch} sketch - The sketch that is being set.
     */
    this.setSketch = function(sketch) {
        if (isUndefined(sketch.id)) {
            // TODO: change to exception object
            throw new SketchSurfaceException('id must be defined to add it.');
        }
        sketchMap.set(sketch.id, sketch);
    };

    /**
     * @param {UUID} id - The id of the sketch that is being grabbed.
     * @returns {SRL_Sketch} a sketch based off of its id.
     */
    this.getSketch = function(id) {
        return sketchMap.get(id);
    };

    /**
     * Creates a new sketch with the given id.
     * NOTE: this does not change what the current sketch is pointed to.
     *
     * @param {UUID} id - The of the new sketch.
     * @param {SrlSketch} [sketchData] - currently ignored.
     */
    this.createSketch = function(id, sketchData) {
        var sketch = new SRL_Sketch();
        sketch.id = id;
        this.setSketch(sketch);
    };

    /**
     * Sets the current sketch for input and drawing to the one specified by the given id.
     *
     * @param {UUID} id - the id of the sketch to take input and drawing.
     */
    this.setCurrentSketch = function(id) {
        currentSketch = this.getSketch(id);
    };

    /**
     * Clears the current sketch so no sketch is the current one.
     */
    this.clearCurrentSketch = function() {
        currentSketch = undefined;
    };

    /**
     * @returns {SRL_Sketch} The current sketch that is being used by this sketch surface.
     */
    this.getCurrentSketch = function() {
        return currentSketch;
    };

    /**
     * Deletes the sketch from the list of possible sketches.
     *
     * @param {UUID} id - the id of the sketch to be removed.
     */
    this.deleteSketch = function(id) {
        // You can not delete the parent sketch if there are sub sketches.
        if ((parentSketch && id === parentSketch.id) || parentSketchId === id) {
            if (sketchMap.size > 1) {
                throw new SketchSurfaceException('You can not delete the parent sketch if there are sub sketches.');
            }
            parentSketch = undefined;
            parentSketchId = undefined
        }
        if (this.getCurrentSketch() && this.getCurrentSketch().id === id) {
            if (sketchMap.size > 1) {
                throw new SketchSurfaceException('You can not delete the current sketch');
            }
            this.clearCurrentSketch();
        }
        sketchMap.delete(id);
    };

    /**
     * Clears all sketches from the {@link SketchSurfaceManager}.
     */
    this.clearAllSketches = function() {
        var ids = this.getSketchIds();
        for (var i = 0; i < ids.length; i++) {
            try {
                this.deleteSketch(ids[i]);
            } catch (exception) {
                // In case the parent sketch or current sketch is not the last sketch
                if (exception instanceof SketchSurfaceException) {
                    sketchMap.delete(ids[i]);
                } else {
                    throw exception;
                }
            }
        }
    }
}
