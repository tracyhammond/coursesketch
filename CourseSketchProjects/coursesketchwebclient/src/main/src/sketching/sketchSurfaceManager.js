/* depends on objectandinheritance.js */

/**
 * This file contains all of the resources for
 */
function SketchSurfaceHandler() {

    var SKETCH_TAG = "sketch-surface";
    var SKETCH_CONTAINER_CLASS = "sketch-surface-container";
    var SKETCH_TEMPLATE = "sketch-surface-template";
    var sketchObjectList = {};
    var sketchingTemplate = undefined;

    /**
     * @param id
     *            {String}
     * @returns {SketchSurface}
     */
    this.getSketchSurface = function(id) {
        return sketchObjectList[id];
    };

    function initalizeTemplate() {
        sketchingTemplate = document.querySelector("#" + SKETCH_TEMPLATE);
    }

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
