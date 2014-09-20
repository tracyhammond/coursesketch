/* depends on objectandinheritance.js */

/**
 * This file contains all of the resources for
 */
function SketchSurfaceHandler() {

    var SKETCH_TAG = "sketch-surface";
    var SKETCH_CONTAINER_CLASS = "sketch-surface-container";
    var SKETCH_TEMPLATE = "sketch-surface-template";
    var sketchObjectList = {};

    /**
     * @param id
     *            {String}
     * @returns {SketchSurface}
     */
    this.getSketchSurface = function(id) {
        return sketchObjectList[id];
    };

    /**
     * Returns a sketch with the specific ID
     * 
     * @param id
     *            {String}
     * @returns {SRL_Sketch}
     */
    this.getSketch = function(id) {
        return sketchObjectList[id].getSrlSketch();
    };

    /**
     * Creates a new sketch. The sketch is invisible by default and has its
     * style set to none.
     * 
     * @param id
     *            {String}
     * @param updateList
     *            {UpdateManager} An optional argument that creates a sketch
     *            with an existing update list.
     * @returns {SRL_Sketch}
     */
    this.createSketch = function(id, updateList) {
        try {
            var sketch = this.getSketch(id);
            if (!isUndefined(sketch)) {
                return sketch; // sketch is already created with this ID.
                // prevents infinite loops
            }
        } catch (exception) {

        }
        var element = document.createElement("sketch-surface");
        if (!isUndefined(updateList)) {
            element.dataset.existingList = "";
            element.bindToUpdateList(updateList);
        }
        element.dataset.customId = "";
        element.id = id;
        sketchObjectList[id] = element;
        return element.getSrlSketch();
    };

    /**
     * Removes the sketch and possibly removes it from the DOM
     */
    this.deleteSketch = function(id) {
        sketchObjectList[id] = undefined;
        var element = document.getElementById("element-id");
        if (!isUndefined(element) && element != null) {
            // implicitly calls finalize on the specific element
            element.parentNode.removeChild(element);
        }
    };

    /**
     * Initializes the list of sketches with the current element.
     * 
     * @param element
     *            {Element}
     */
    this.initialize = function(element) {
        var elements = element.querySelectorAll(SKETCH_TAG);
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            sketchObjectList[element.id] = element;
        }
    };

    this.addElement = function(element) {
        if (!element instanceof SketchSurface) {
            throw new Error("Place holder error please correct");
        }
        sketchObjectList[element.id] = element;
    };

    /**
     * Resets this object so that it contains zero sketch objects.
     */
    this.reset = function() {
        sketchObjectList = {};
    };

    this.getSketchContainerClass = function() {
        return SKETCH_CONTAINER_CLASS;
    };

    this.getSketchTemplateId = function() {
        return SKETCH_TEMPLATE;
    };
}

(function(scope) {
    if (!isUndefined(scope.SKETCHING_SURFACE_HANDLER)) {
        return;
    }
    /**
     * @Class Contains data that get
     */
    makeValueReadOnly(scope, "SKETCHING_SURFACE_HANDLER", new SketchSurfaceHandler());
})(this);
