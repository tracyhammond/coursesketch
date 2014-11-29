/**
 * Creates the text box dialog
 * The dialog is moveable and allows the creator to enter text to be displayed
 */
function TimelineMarker() {
    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        addCross(shadowRoot.querySelector("#picture"), this);

    };

    function addCross(element, marker) {
		element.onclick = function() {
			$(element).addClass('cross');
			var oldClickFunction = element.onclick;
			var tim = setTimeout(function () { 
				$(element).removeClass('cross'); 
				element.onclick = oldClickFunction;
			}, 5000);
			element.onclick = function() {
				clearTimeout(tim);
				marker.parentNode.removeChild(marker);
				marker.removeFunction();
			};
		};
	}
	
	this.setRemoveFunction = function(remove) {
		this.removeFunction = remove;
	}
}
TimelineMarker.prototype = Object.create(HTMLElement.prototype);
