function SubmissionException(message) {
    this.name = "SubmissionException";
    this.setMessage(message);
    this.message = "";
    this.htmlMessage = "";
}

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
        toolbar.setSaveCallback(function() {
            this.sendDataToServerExceptionWrapped(false);
        }.bind(this));
        toolbar.setSubmitCallback(function() {
            this.sendDataToServerExceptionWrapped(true);
        }.bind(this));
    };

    this.sendDataToServerExceptionWrapped = function(isSubmitting) {
        try {
            this.sendDataToServer(isSubmitting);
        } catch(exception) {
            alert(exception.toString());
        }
    }

    this.sendDataToServer = function(isSubmitting) {
        var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes();
        if (isUndefined(this.problem) || isUndefined(this.problemType)) {
            throw new SubmissionException("Problem data is not sent correctly aborting save");
        }
        var submission = undefined;
        var QuestionType = CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType;
        switch(this.problemType) {
            case QuestionType.SKETCH:
                submission = createSketchSubmission(subPanel, isSubmitting);
                break;
            case QuestionType.FREE_RESP:
                submission = createTextSubmission(subPanel, isSubmitting);
                break;
        }
        if (isUndefined(submission)) {
            throw new SubmissionException("submission type not supported, aborting submission");
        }
        if (isUndefined(this.wrapperFunction)) {
            // you need to set the wrapper function to either create an experiment or solution.
            throw new SubmissionException("Wrapper function is not set, aborting submission");
        }
        var submittingValue = this.wrapperFunction(submission);
        var request = CourseSketch.PROTOBUF_UTIL.createRequestFromData(submittingValue,
                CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.SUBMISSION);
        var problemType = this.problemType;
        var problem = this.problem;
        CourseSketch.connection.setSubmissionListener(function(request) {
            CourseSketch.connection.setSubmissionListener(undefined);
            alert(request.responseText());
            if (problem == this.problem && this.problemType == CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.SKETCH) {
                var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes();
                // potential conflict if it was save multiple times in quick succession.
                subPanel.getUpdateManager().setLastSaveTime(request.getTime());
            };
        }.bind(this));
        CourseSketch.connection.sendRequest(request);
        QuestionType = undefined;
        submission = undefined;
        var subPanel = undefined;
    };

    /**
     * @return {SrlSubmission} object that is ready to be sent to the server.
     */
    function createSketchSubmission(sketchSurface, isSubmitting) {
        var updateManager = sketchSurface.getUpdateManager();

        if (isSubmitting && !updateManager.isValidForSubmission()) {
            throw new SubmissionException("must make changes to resubmit aborting submission");
        }
        if (!isSubmitting && !updateManager.isValidForSaving()) {
            throw new SubmissionException("must make changes to save again aborting saving");
        }

        var listLength = updateManager.getListLength();
        var MarkerType = CourseSketch.PROTOBUF_UTIL.getMarkerClass().MarkerType;
        var markerCommand = updateManager.createMarker(true, isSubmitting ? MarkerType.SUBMISSION : MarkerType.SAVE);
        var markerUpdate = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([markerCommand]);
        updateManager.addSynchronousUpdate(updateManager);

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
