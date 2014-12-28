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

    this.setParentSketchId = function(id) {
        parentSketch.id = id;
        this.setSketch(parentSketch);
    };

    this.setSketch = function(sketch) {
        if (isUndefined(sketch.id)) {
            // TODO: change to exception object
            throw "id must be defined to add it.";
        }
        sketchMap.set(sketch.id, sketch);
    };

    this.getSketch = function(id) {
        return sketchMap.get(id);
    };

    /**
     * creates a new sketch with the given id.
     * NOTE: this does not change what the current sketch is pointed to.
     */
    this.createSketch = function(id) {
        var sketch = new SRL_Sketch();
        sketch.id = id;
        this.setSketch(sketch);
    };

    this.setCurrentSketch = function(id) {
        currentSketch = this.getSketch(id);
    };

    this.getCurrentSketch = function() {
        return currentSketch;
    }
}
