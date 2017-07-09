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
            var totalNumber = nav.getCurrentTotalNumber();
            if (totalNumber) {
                this.shadowRoot.querySelector('#totalNumber').textContent = totalNumber;
            }
        }.bind(this));

        this.setUpButtons(this.itemNavigator);
        if (!this.itemNavigator.isDataLoaded() && !isUndefined(this.itemNavigator.getAssignmentId())) {
            this.itemNavigator.reloadAssignment();
        }
    };

    /**
     * @param {AssignmentNavigator} nav - Sets bindings and disables buttons if they can not do anything.
     * @instance
     * @function setUpButtons
     */
    this.setUpButtons = function(nav) {
        /* jscs:disable jsDoc */
        var hasNext = nav.hasNext();
        var nextButton = this.shadowRoot.querySelector('#buttonNext');
        nextButton.setAttribute('data-disabled', !hasNext);

        if (hasNext) {
            nextButton.onclick = function() {
                nav.gotoNext();
            };
        } else {
            nextButton.onclick = undefined;
        }

        var hasPrevious = nav.hasPrevious();
        var previousButton = this.shadowRoot.querySelector('#buttonPrev');
        previousButton.setAttribute('data-disabled', !hasPrevious);

        if (hasPrevious) {
            previousButton.onclick = function() {
                nav.gotoPrevious();
            };
        } else {
            previousButton.onclick = undefined;
        }

        /* jscs:enable jsDoc */
    };

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
        this.setUpNavigator();
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
