/* depends on objectandinheritance.js*/

/**
 * This file contains all of the resources for 
 */
(function(scope) {

	/**
	 * @Class
	 * Contains data that get
	 */
	makeValueReadOnly(scope, "SKETCHING_HANDLER", new (function() {

		var SKETCH_CONTAINER_CLASS = "sketch-surface-container";
		var SKETCH_TEMPLATE = "sketch-surface-template";
		var sketchObjectList = {};
		var sketchingTemplate;

		/**
		 * @param id {SketchSurface}
		 */
		function getSketchSurface(id) {
			return sketchObjectList[id];
		}

		function initalizeTemplate() {
			sketchingTemplate = document.querySelector("#" + SKETCH_TEMPLATE);
		}

		/**
		 * Initializes the list of sketches with the current element.
		 *
		 * @param element {Element}
		 */
		this.initialize = function(element) {
			var elements = element.querySelectorAll("." + SKETCH_CONTAINER_CLASS);
			for (var i = 0; i < elements.length; i++) {
				var element = elements[i]
				sketchObjectList[element] = new SketchSurface(element);
				
			}
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

	})());
})(this);
