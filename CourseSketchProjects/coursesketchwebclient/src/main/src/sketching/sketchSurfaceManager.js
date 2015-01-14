/* depends on objectandinheritance.js */

/**
 * This file contains all of the resources for managing sketches.
 */
function SketchSurfaceManager(sketchSurface) {
    var sketchMap = new Map();
    var parentSketch = undefined;
    var currentSketch = undefined;

    /**
     * sets the parent sketch.
     */
    this.setParentSketch = function(sketch) {
        parentSketch = sketch;
        if (isUndefined(currentSketch)) {
            currentSketch = sketch;
        }
    };

    /**
     * Sets the id of the parent sketch.
     * A sketch surface can contain multiple sketches but the first sketch object created is the parent sketch.
     */
    this.setParentSketchId = function(id) {
        parentSketch.id = id;
        this.setSketch(parentSketch);
    };

    /**
     * Adds the sketch with its Id to the list of sketches related to this sketch surface.
     * (replaces an old sketch with the same id if it already exist in the list).
     */
    this.setSketch = function(sketch) {
        if (isUndefined(sketch.id)) {
            // TODO: change to exception object
            throw "id must be defined to add it.";
        }
        sketchMap.set(sketch.id, sketch);
    };

    /**
     * Returns a sketch based off of its id.
     */
    this.getSketch = function(id) {
        return sketchMap.get(id);
    };

    /**
     * creates a new sketch with the given id.
     * NOTE: this does not change what the current sketch is pointed to.
     */
    this.createSketch = function(id, sketchData) {
        var sketch = new SRL_Sketch();
        sketch.id = id;
        this.setSketch(sketch);
    };

    /**
     * sets the current sketch for input and drawing to the one specified by the given id.
     */
    this.setCurrentSketch = function(id) {
        currentSketch = this.getSketch(id);
    };

    /**
     * returns the current sketch that is being used by this sketch surface.
     */
    this.getCurrentSketch = function() {
        return currentSketch;
    };

    /**
     * deletes the sketch from the list of possible sketches.
     */
    this.deleteSketch = function(id) {
        sketchMap.delete(id);
    };

}
