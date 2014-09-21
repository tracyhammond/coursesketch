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
            console.log("ISSUE!");
        }
        var element = document.createElement("sketch-surface");
        if (!isUndefined(updateList)) {
            element.dataset.existingList = "";
            element.bindToUpdateList(updateList);
        }
        if (!isUndefined(id)) {
            element.dataset.customId = "";
            element.id = id;
        }
        sketchObjectList[id] = element;
        element.initializeSketch();
        return element.getSrlSketch();
    };

    /**
     * Removes the sketch and possibly removes it from the DOM
     */
    this.deleteSketch = function(id) {
        sketchObjectList[id] = undefined;
        var element = document.getElementById(id);
        if (!isUndefined(element) && element != null) {
            // implicitly calls finalize on the specific element
            element.parentNode.removeChild(element);
        }
    };

    /**
     * Initializes the list of sketches with the current element.
     * 
     * This method should not need to be called because it should automatically
     * register itself.
     * 
     * @param element
     *            {Element}
     */
    this.addFromElement = function(element) {
        var elements = element.querySelectorAll(SKETCH_TAG);
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            sketchObjectList[element.id] = element;
        }
    };

    /**
     * @param element
     *            {SketchSurface} adds an instance of SketchSurface to the
     *            sketch.
     */
    this.addElement = function(element) {
        if (!(getTypeName(element) === "sketch-surface" || element instanceof SketchSurface || Object.getPrototypeOf(element) == SketchSurface)) {
            throw new Error("Added element is not an instance of SketchSurface: " + getTypeName(element));
        }
        if (isUndefined(element.id)) {
            throw new Error("Element Id must be defined");
        }
        sketchObjectList[element.id] = element;
    };

    /**
     * This is used to force the addition of adding an element (typically not a good idea)
     * @param element
     *            {SketchSurface} adds an instance of SketchSurface to the
     *            sketch.
     */
    this.addElementForced = function(element) {
        sketchObjectList[element.id] = element;
    };

    /**
     * Resets this object so that it contains zero sketch objects.
     * 
     * NOTE: this does not delete any sketches from the dom.
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

    /**
     * @returns {Array}
     */
    this.getSketchIds = function() {
        var array = [];
        for ( var id in sketchObjectList) {
            array.push(id);
        }
        return array;
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
