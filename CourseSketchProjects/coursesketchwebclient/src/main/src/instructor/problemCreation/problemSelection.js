/**
 * Only one of these can be on a page at a time.
 */
function ProblemSelectionPanel() {
    /**
     * Holds the list of ids of the selected bank problems.
     */
    var selectedBankProblems = [];
    var clickSelector = new ClickSelectionManager();

    /**
     * Loads the problems from the server.
     *
     * @param {String} courseId - the id of the course the problem is being requested for.
     * @param {String} assignmentId - the id of the assignment the problem is being requested for.
     * @param {Integer} page - to make it easier we do not grab every single bank problem instead we grab them in batches
     *              (this process is called pagination)
     */
    this.loadProblems = function(courseId, assignmentId, page) {
        var request = this.createRequest(courseId, assignmentId, page);
        CourseSketch.dataListener.setListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
                CourseSketch.PROTOBUF_UTIL.ItemQuery.BANK_PROBLEM, function(evt, item) {
            CourseSketch.dataListener.removeListener(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.DATA_REQUEST,
                                CourseSketch.PROTOBUF_UTIL.ItemQuery.BANK_PROBLEM);
            if (isUndefined(item.data) || item.data === null) {
                throw new Error('The data is null!');
            }
            var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
            var bankProblems = school.bankProblems;
            var builder = new SchoolItemBuilder().setList(bankProblems).setBoxClickFunction(function(schoolItem) {
                    clickSelector.toggleSelection(this);
                    if ($(this).hasClass(clickSelector.selectionClassName)) {
                        removeObjectFromArray(selectedBankProblems, this.id);
                    } else {
                        selectedBankProblems.push(this.id);
                    }
                })
                .build(this.shadowRoot.querySelector('#selectionContent'));
        }.bind(this));
        CourseSketch.connection.sendRequest(request);
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

        shadowRoot.querySelector('#cancel').onclick = function() {
            localScope.canceledCallback(selectedBankProblems);
        };
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
