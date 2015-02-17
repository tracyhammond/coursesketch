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
	/**
	 * straight through the timeline, the user is able to delete elements that were created
	 */
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

    /**
     * @param remove {function} the element is removed and calls this function during the process
     */
    this.setRemoveFunction = function(remove) {
        this.removeFunction = remove;
    };
    /**
     * For the user to see what text they have typed inside of the textboxes
     */
    this.setPreviewText = function (text) {
        this.shadowRoot.querySelector('#preview').textContent = text.substring(0, 10) + (text.length > 10 ? "..." : "");
    };
}
TimelineMarker.prototype = Object.create(HTMLElement.prototype);
TimelineMarker.prototype.showBox = undefined;
