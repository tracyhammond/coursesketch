/**
 * Only one of these can be on a page at a time.
 * [We assume only one of these elements can exist at a time].
 *
 * @class ProblemSelectionPanel
 */
function ProblemSelectionPanel() {
    /**
     * Holds the list of ids of the selected bank problems.
     */
    var selectedBankProblems = [];
    var clickSelector = new ClickSelectionManager();
    var currentPage = 0;
    var currentCourse = '';
    var currentAssignment = '';

    /**
     * Loads the problems from the server.
     *
     * @param {String} courseId - the id of the course the problem is being requested for.
     * @param {String} assignmentId - the id of the assignment the problem is being requested for.
     * @param {Integer} page - to make it easier we do not grab every single bank problem instead we grab them in batches
     *              (this process is called pagination)
     * @memberof ProblemSelectionPanel
     */
    this.loadProblems = function(courseId, assignmentId, page) {
        currentPage = page;
        currentCourse = courseId;
        currentAssignment = assignmentId;
        var request = this.createRequest(courseId, assignmentId, page);
        CourseSketch.dataListener.setListener(CourseSketch.prutil.getRequestClass().MessageType.DATA_REQUEST,
                CourseSketch.prutil.ItemQuery.BANK_PROBLEM, function(evt, item) {
                    CourseSketch.dataListener.removeListener(CourseSketch.prutil.getRequestClass().MessageType.DATA_REQUEST,
                                CourseSketch.prutil.ItemQuery.BANK_PROBLEM);
                    if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                        throw new Error('The data is null!');
                    }
                    clickSelector.clearAllSelectedItems();

                    var bankProblems = [];
                    for (var i = 0; i < item.data.length; i++) {
                        var decodedBankProblem = CourseSketch.prutil.getSrlBankProblemClass().decode(item.data[i]);
                        bankProblems.push(decodedBankProblem);
                    }
                    var builder = new SchoolItemBuilder().setList(bankProblems).setBoxClickFunction(function(schoolItem) {
                        clickSelector.toggleSelection(this);
                        if ($(this).hasClass(clickSelector.selectionClassName)) {
                            removeObjectFromArray(selectedBankProblems, this.id);
                        } else {
                            selectedBankProblems.push(this.id);
                        }
                    }).build(this.shadowRoot.querySelector('#selectionContent'));

                    clickSelector.applySelections(this.getListOfSelectedElements());
                }.bind(this));
        // end data request listener
        CourseSketch.connection.sendRequest(request);

        var pageList = this.shadowRoot.querySelectorAll('.currentPage');
        for (var i = 0; i < pageList.length; i++) {
            pageList[i].textContent = currentPage;
        }
    };

    /**
     * Makes the exit button close the box and enables dragging.
     *
     * @param {Node} templateClone - Is a clone of the custom HTML Element for the text box.
     * @memberof ProblemSelectionPanel
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        /**
         * Called when the user accepts the problems.
         */
        shadowRoot.querySelector('#accept').onclick = function() {
            localScope.acceptCallback(selectedBankProblems);
        };

        /**
         * Called when the user rejects the selected problems.
         */
        shadowRoot.querySelector('#cancel').onclick = function() {
            localScope.canceledCallback(selectedBankProblems);
        };

        /**
         * Called to signify the user rejecting the selected problems.
         */
        shadowRoot.querySelector('.outer-dialog').onclick = function() {
            localScope.canceledCallback(selectedBankProblems);
        };

        /**
         * Called to stop the event from going up to the outer-dialog onclick function.
         *
         * @param {Event} event - The event that is being stopped.
         */
        shadowRoot.querySelector('.inner-dialog').onclick = function(event) {
            event.stopPropagation();
        };

        /**
         * Called to signify the user rejecting the selected problems.
         *
         * @param {Event} event - The event that was created by clicking the element.
         */
        this.onclick = function(event) {
            localScope.canceledCallback(selectedBankProblems);
        };

        var nextList = shadowRoot.querySelectorAll('.next');
        applyOnClick(nextList, function() {
            currentPage += 1;
            localScope.loadProblems(currentCourse, currentAssignment, currentPage);
        });

        var previousList = shadowRoot.querySelectorAll('.previous');
        applyOnClick(previousList, function() {
            currentPage -= 1;
            if (currentPage < 0) {
                currentPage = 0;
                return;
            }
            localScope.loadProblems(currentCourse, currentAssignment, currentPage);
        });
    };

    /**
     * Applies a funciton to a list of elements.
     *
     * @param {Array<Element>} listOfElements - the list of elements that have the function
     * @param {Function} func - the function that is being applied to.
     * @memberof ProblemSelectionPanel
     */
    function applyOnClick(listOfElements, func) {
        for (var i = 0; i < listOfElements.length; i++) {
            listOfElements[i].onclick = func;
        }
    }

    /**
     * @returns {List<Element>} The list of selected bank problems that are currently on the screen.
     * @memberof ProblemSelectionPanel
     */
    this.getListOfSelectedElements = function() {
        var result = [];
        for (var i = 0; i < selectedBankProblems.length; i++) {
            var element = this.shadowRoot.querySelector(cssEscapeId(selectedBankProblems[i]));
            if (element !== null) {
                result.push(element);
            }
        }
        return result;
    };
}

ProblemSelectionPanel.prototype = Object.create(HTMLDialogElement.prototype);

/**
 * Creates a request for asking about bank problems.
 *
 * @param {String} courseId - The id of the course the problem is being requested for.
 * @param {String} assignmentId - The id of the assignment the problem is being requested for.
 * @param {Integer} page - To make it easier we do not grab every single bank problem instead we grab them in batches
 *              (this process is called pagination)
 * @returns {SrlRequest} The request that is ready to be sent to the server.
 * @memberof ProblemSelectionPanel
 */
ProblemSelectionPanel.prototype.createRequest = function(courseId, assignmentId, page) {
    var itemRequest = CourseSketch.prutil.ItemRequest();
    itemRequest.page = page;
    itemRequest.query = CourseSketch.prutil.ItemQuery.BANK_PROBLEM;
    itemRequest.itemId = [];
    itemRequest.itemId.push(courseId);
    itemRequest.itemId.push(assignmentId);

    var dataRequest = CourseSketch.prutil.DataRequest();
    dataRequest.items = [ itemRequest ];
    var request = CourseSketch.prutil.createRequestFromData(dataRequest,
            CourseSketch.prutil.getRequestClass().MessageType.DATA_REQUEST);
    return request;
};

/**
 * Sets the canceled callback.
 *
 * @param {Function} callback - The function that is called when an a canceled action is performed.
 * @memberof ProblemSelectionPanel
 */
ProblemSelectionPanel.prototype.setCanceledCallback = function(callback) {
    this.canceledCallback = callback;
};

/**
 * Sets the canceled callback.
 *
 * @param {Function} callback - The function that is called when an a canceled action is performed.
 * @memberof ProblemSelectionPanel
 */
ProblemSelectionPanel.prototype.setAcceptedCallback = function(callback) {
    this.acceptCallback = callback;
};
