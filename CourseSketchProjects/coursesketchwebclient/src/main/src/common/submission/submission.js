/**
 * A class that handles submitting a problem to the database.
 * and listening for the result.
 * This class does not retrieve submissions.
 *
 * Assumptions made:
 * toolbar is a custom element that has two functions: setSaveCallback(callback), setSubmitCallback(callback)
 * the toolbar is set before the element is inserted
 * the sub-panel (submit panel) element can change at run time and may not be inserted when this element is inserted
 * you can set the problem object
 */
function Submission() {

    /**
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(templateClone) {
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
        this.setCallbacks();
    };

    this.setCallbacks = function() {
        var toolbar = this.shadowRoot.querySelector("#toolbar").getDistributedNodes();
        toolbar.setSaveCallback(this.sendDataToServer().bind(this));
    };

    this.sendDataToServer = function() {
        var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes();
        if (isUndefined(this.problem) || isUndefined(this.problemType)) {
            alert("Problem data is not sent correctly aborting save");
            return;
        }
        var submission = undefined;
        var QuestionType = CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType;
        switch(this.problemType) {
            case QuestionType.SKETCH:
                submission = createSketchSubmission(subPanel);
                break;
            case QuestionType.FREE_RESP:
                submission = createTextSubmission(subPanel);
                break;
        }
        if (isUndefined(submission)) {
            alert("submission type not supported, aborting submission");
            return;
        }
        if (isUndefined(this.wrapperFunction)) {
            // you need to set the wrapper function to either create an experiment or solution.
            alert("Wrapper function is not set, aborting submission");
            return;
        }

    };

    /**
     * @return {SrlSubmission} object that is ready to be sent to the server.
     */
    function createSketchSubmission(sketchSurface) {
        var protoObject = sketchSurface.getSrlUpdateListProto();
        var submission = createBaseSubmission();
        submission.setUpdateList(protoObject);
        return submission;
    }

    /**
     * @return {SrlSubmission} object that is ready to be sent to the server.
     */
    function createSketchSubmission(sketchSurface) {

    }

    function createBaseSubmission() {
        var submission = CourseSketch.PROTOBUF_UTIL.SrlSubmission();
        return submission;
    }

    /**
     * Sets the wrapperFunction, This function takes in a submission and wraps it as either the experiment or solution.
     * This wrapped value is returned from the function and then it is sent to the server internally.
     * @param wrapperFunction {Function} used to wrap the submission in its required data.
     */
    this.setWrapperFunction = function(wrapperFunction) {
        this.wrapperFunction = wrapperFunction;
    }
}

/**
 * @param problem {SrlProblem} sets the problem element
 */
Submission.prototype.setProblem = function(problem) {
    this.problem = problem;
};

/**
 * @param problem {SrlProblem} sets the problem element
 */
Submission.prototype.setProblemType = function(problemType) {
    this.problemType = problemType;
};
Submission.prototype = Object.create(HTMLElement.prototype);
