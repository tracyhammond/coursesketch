/**
 * The custom element for navigating a problem.
 * @attribute loop {existence} If this property exist the navigator will loop.  (Setting the navigator overrides this property).
 * @attribute assignment_id {string} uses the given value as the assignment id inside the navigator.
 * @attribute index {number} if the value exist then this is the number used to define the current index.
 */
function NavigationPanel() {

    /**
     * Sets up the navigator so that the buttons will be b
     */
    function setUpNavigator() {
        this.navigator.addCallback(function(nav) {
            this.shadowRoot.querySelector("#selectionBoxNumber").textContent = nav.getCurrentNumber();
            // set span state
            setUpButtons(nav);
            var totalNumber = nav.getLength();
            if (totalNumber) {
                this.shadowRoot.querySelector("#totalNumber").textContent = "out of " + totalNumber;
            }
            // TODO: change this to strip out bad HTML code
            this.shadowRoot.querySelector("#problemPanel").innerHTML = '<p>' + parent.problemNavigator.getProblemText() + '</p>';
        }.bind(this));

        setUpButtons(this.navigator);
    }

    function setUpButtons(nav) {
        var button = this.shadowRoot.querySelector("#buttonNext");
        if (nav.hasNext()) {
            button.onclick = function() {nav.gotoNext();};
            button.disabled = false;
        } else {
            button.onclick = function() {};
            button.disabled = true;
        }
        button = scope.getElementById("buttonPrev");
        if (nav.hasPrevious()) {
            button.onclick = function() {nav.gotoPrevious();};
            button.disabled = false;
        } else {
            button.onclick = function() {};
            button.disabled = true;
        }
    }
        /*
        window.onresize = function() {
            var navWidth = document.getElementById("navPanel").offsetWidth;
            var navHeight = document.getElementById("navPanel").offsetHeight;
            var textWidth = document.getElementById("panelWrapper").offsetWidth - navWidth;
            document.getElementById("problemPanel").style.width = (textWidth - 15) +"px";
            document.getElementById("problemPanel").style.height = (navHeight -15) +"px";
        }
        */

    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        if (isUndefined(this.navigator)) {
            this.navigator = new ProblemNavigator(this.dataset.assignment_id, !isUndefined(this.dataset.loop), this.dataset.index);
        }
    }

    this.setNavigator = function(navPanel) {
        this.navigator = navPanel;
    }
}

NavigationPanel.prototype = Object.create(HTMLElement.prototype);
