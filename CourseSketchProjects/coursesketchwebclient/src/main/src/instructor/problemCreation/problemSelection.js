/**
 * Only one of these can be on a page at a time.
 * [We assume only one of these elements can exist at a time]
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
     */
    this.loadProblems = function(courseId, assignmentId, page) {
        currentPage = page;
        currentCourse = courseId;
        currentAssignment = assignmentId;
        var request = this.createRequest(courseId, assignmentId, page);
        CourseSketch.dataListener.setListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
                CourseSketch.PROTOBUF_UTIL.ItemQuery.BANK_PROBLEM, function(evt, item) {
            CourseSketch.dataListener.removeListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
                                CourseSketch.PROTOBUF_UTIL.ItemQuery.BANK_PROBLEM);
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                throw new Error('The data is null!');
            }
            clickSelector.clearAllSelectedItems();

            var bankProblems = [];
            for (var i = 0; i < item.data.length; i++) {
                var decodedBankProblem = CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().decode(item.data[i]);
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
     * @param {Node} templateClone is a clone of the custom HTML Element for the text box.
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        shadowRoot.querySelector('#accept').onclick = function() {
            localScope.acceptCallback(selectedBankProblems);
        };

        // cancel options
        shadowRoot.querySelector('#cancel').onclick = function() {
            localScope.canceledCallback(selectedBankProblems);
        };

        shadowRoot.querySelector('.outer-dialog').onclick = function() {
            localScope.canceledCallback(selectedBankProblems);
        };

        shadowRoot.querySelector('.inner-dialog').onclick = function(event) {
            event.stopPropagation();
        };

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
     * @param {Array<Element>} listOfElements - the list of elements that have the function
     * @param {Function} func - the function that is being applied to
     */
    function applyOnClick(listOfElements, func) {
        for (var i = 0; i < listOfElements.length; i++) {
            listOfElements[i].onclick = func;
        }
    }

    /**
     * Returns the list of selected elements that are currently on the screen.
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
 * @param {String} courseId - the id of the course the problem is being requested for.
 * @param {String} assignmentId - the id of the assignment the problem is being requested for.
 * @param {Integer} page - to make it easier we do not grab every single bank problem instead we grab them in batches
 *              (this process is called pagination)
 * @returns {SrlRequest} the request that is ready to be sent to the server.
 */
ProblemSelectionPanel.prototype.createRequest = function(courseId, assignmentId, page) {
    var itemRequest = CourseSketch.PROTOBUF_UTIL.ItemRequest();
    itemRequest.page = page;
    itemRequest.query = CourseSketch.PROTOBUF_UTIL.ItemQuery.BANK_PROBLEM;
    itemRequest.itemId = [];
    itemRequest.itemId.push(courseId);
    itemRequest.itemId.push(assignmentId);

    var dataRequest = CourseSketch.PROTOBUF_UTIL.DataRequest();
    dataRequest.items = [ itemRequest ];
    var request = CourseSketch.PROTOBUF_UTIL.createRequestFromData(dataRequest,
            CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST);
    return request;
};

ProblemSelectionPanel.prototype.setCanceledCallback = function(callback) {
    this.canceledCallback = callback;
};

ProblemSelectionPanel.prototype.setAcceptedCallback = function(callback) {
    this.acceptCallback = callback;
};
