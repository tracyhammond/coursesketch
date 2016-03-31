/**
 * The custom element for navigating a problem.
 *
 * @class NavigationPanel
 * @attribute loop {Existence} If this property exist the navigator will loop.  (Setting the navigator overrides this property).
 * @attribute assignment_id {String} uses the given value as the assignment id inside the navigator.
 * @attribute index {Number} if the value exist then this is the number used to define the current index.
 *
 */
function NavigationPanel() {

    /**
     * Sets up the navigator callback and binds the buttons.
     *
     * @instance
     * @function setUpNavigator
     */
    this.setUpNavigator = function() {
        this.itemNavigator.addCallback(function(nav) {
            this.shadowRoot.querySelector('#selectionBoxNumber').textContent = nav.getCurrentNumber();
            // set span state
            this.setUpButtons(nav);
            var totalNumber = nav.getLength();
            if (totalNumber) {
                this.shadowRoot.querySelector('#totalNumber').textContent = totalNumber;
            }

        }.bind(this));

        this.setUpButtons(this.itemNavigator);
        this.itemNavigator.reloadAssignment();
    };

    /**
     * @param {AssignmentNavigator} nav - Sets bindings and disables buttons if they can not do anything.
     * @instance
     * @function setUpButtons
     */
    this.setUpButtons = function(nav) {
        var button = this.shadowRoot.querySelector('#buttonNext');

        /* jscs:disable jsDoc */
        button.onclick = function() {
            nav.gotoNext();
        };
        button.disabled = !nav.hasNext();
        button = this.shadowRoot.querySelector('#buttonPrev');
        button.onclick = function() {
            nav.gotoPrevious();
        };
        button.disabled = !nav.hasPrevious();
        /* jscs:enable jsDoc */
    };

    /*
    Window.onresize = function() {
        var navWidth = document.getElementById('navPanel').offsetWidth;
        var navHeight = document.getElementById('navPanel').offsetHeight;
        var textWidth = document.getElementById('panelWrapper').offsetWidth - navWidth;
        document.getElementById('problemPanel').style.width = (textWidth - 15) +'px';
        document.getElementById('problemPanel').style.height = (navHeight -15) +'px';
    }
    */

    /**
     * @param {node} templateClone - Is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     * @instance
     * @function intializeElement
     */
    this.initializeElement = function(templateClone) {
        this.shadowRoot = this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);

        if (isUndefined(this.itemNavigator)) {
            this.itemNavigator = new AssignmentNavigator(this.dataset.assignment_id, this.dataset.index, true);
        }
        this.itemNavigator.setUiLoaded(true);
        this.itemNavigator.reloadAssignment();
    };

    /**
     * Sets the navigator if one is to be used.
     *
     * @param {AssignmentNavigator} navPanel - The nav panel that is being used.
     * @instance
     * @function setNavigator
     */
    this.setNavigator = function(navPanel) {
        this.itemNavigator = navPanel;
    };

    /**
     * @return {AssignmentNavigator}.
     * @instance
     * @function getNavigator
     */
    this.getNavigator = function() {
        return this.itemNavigator;
    };
}

NavigationPanel.prototype = Object.create(HTMLElement.prototype);
