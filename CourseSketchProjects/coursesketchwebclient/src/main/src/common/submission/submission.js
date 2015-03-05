function SubmissionException(message) {
    this.name = "SubmissionException";
    this.setMessage(message);
    this.message = "";
    this.htmlMessage = "";
}
SubmissionException.prototype = BaseException;

/**
 * A class that handles submitting a problem to the database.
 * and listening for the result.
 * This class does not retrieve submissions.
 *
 * Assumptions made:
 * toolbar is a custom element that has two functions: setSaveCallback(callback), setSubmitCallback(callback)
 *
 * the toolbar is set before the element is inserted.  (or it will never be inserted)
 *
 * the sub-panel (submit panel) element can change at run time and may not be inserted when this element is inserted
 *
 * you can set the problem object with the class "sub-panel"
 *
 */
function SubmissionPanel() {

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
        var toolbar = this.shadowRoot.querySelector("#toolbar").getDistributedNodes()[0];
        if (toolbar === null) {
            return; //quit before infinite loop
        }
        // toolbar may not be set up by the time this is called, so we wait till it is set up.
        var timeout = setInterval(function() {
            if (!isUndefined(toolbar.setSaveCallback)) {
                clearInterval(timeout);
                toolbar.setSaveCallback(function() {
                    this.sendDataToServerExceptionWrapped(false);
                }.bind(this));
                toolbar.setSubmitCallback(function() {
                    this.sendDataToServerExceptionWrapped(true);
                }.bind(this));
            }
        }.bind(this), 50);
    };

    this.sendDataToServerExceptionWrapped = function(isSubmitting, suppressAlert) {
        try {
            this.sendDataToServer(isSubmitting);
        } catch(exception) {
            if (!suppressAlert) {
                alert(exception.toString());
            }
            console.log(exception);
        }
    };

    this.sendDataToServer = function(isSubmitting) {
        var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes()[0];
        if (isUndefined(subPanel)) {
            throw new SubmissionException("There is no element that contains submittable data");
        }
        if (isUndefined(this.problemType)) {
            throw new SubmissionException("Problem data is not set correctly aborting");
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
            throw new SubmissionException("submission type not supported, aborting");
        }
        if (isUndefined(this.wrapperFunction)) {
            // you need to set the wrapper function to either create an experiment or solution.
            throw new SubmissionException("Wrapper function is not set, aborting");
        }
        var submittingValue = this.wrapperFunction(submission);
        console.log(submittingValue);
        var request = CourseSketch.PROTOBUF_UTIL.createRequestFromData(submittingValue,
                CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.SUBMISSION);
        var problemType = this.problemType;
        var problemIndex = this.problemIndex;
        CourseSketch.connection.setSubmissionListener(function(event, request) {
            console.log(request);
            CourseSketch.connection.setSubmissionListener(undefined);
            alert(request.getMessageTime());
            alert(request.responseText);
            if (problemIndex === this.problemIndex && this.problemType === CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType.SKETCH) {
                var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes()[0];
                // potential conflict if it was save multiple times in quick succession.
                subPanel.getUpdateManager().setLastSaveTime(request.getMessageTime());
                console.log("submission has been updated with the latest time", request.getMessageTime().toString());
            }
            problemType = undefined;
            problemIndex = undefined;
        }.bind(this));
        request.setResponseText(this.isStudent ? "student" : this.isGrader ? "grader" : "instructor");
        CourseSketch.connection.sendRequest(request);
        QuestionType = undefined;
        submission = undefined;
        subPanel = undefined;
    };

    /**
     * gets the text that has been typed.
     * @return {SrlSubmission} object that is ready to be sent to the server.
     *
     * @param textArea {element} The element that contains the text answer
     * @param isSubmtting {boolean} Currently ignored but in the future it may be used.
     */
    function createTextSubmission(textArea, isSubmitting) {
        var submission = createBaseSubmission();
        submission.textAnswer = textArea.value;
        return submission;
    }

    /**
     * Creates the submission object for the sketch surface.  This also adds the submit or save marker to the update list.
     * @return {SrlSubmission} object that is ready to be sent to the server.
     */
    function createSketchSubmission(sketchSurface, isSubmitting) {
        var updateManager = sketchSurface.getUpdateManager();

        if (isSubmitting && !updateManager.isValidForSubmission()) {
            throw new SubmissionException("must make changes to resubmit.");
        }
        if (!isSubmitting && !updateManager.isValidForSaving()) {
            throw new SubmissionException("must make changes to save again.");
        }

        var MarkerType = CourseSketch.PROTOBUF_UTIL.getMarkerClass().MarkerType;
        var markerCommand = updateManager.createMarker(true, isSubmitting ? MarkerType.SUBMISSION : MarkerType.SAVE);
        var markerUpdate = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([markerCommand]);
        updateManager.addSynchronousUpdate(markerUpdate);

        var protoObject = sketchSurface.getSrlUpdateListProto();
        var submission = createBaseSubmission();
        submission.setUpdateList(protoObject);
        return submission;
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
    };

    /**
     * called when the panel is removed from the DOM.
     */
    this.detachedCallback = function() {
        this.setWrapperFunction(undefined);
    };

    this.refreshPanel = function() {
        var subPanel = this.shadowRoot.querySelector("#sub-panel").getDistributedNodes()[0];
        var toolbar = this.shadowRoot.querySelector("#toolbar").getDistributedNodes()[0];
        toolbar.clearCallbacks();
        toolbar.innerHTML = "";
        this.setCallbacks();
        this.setSpecificCallbacks(this.problemType, subPanel, toolbar);
    };

    this.setSpecificCallbacks = function(problemType, element, toolbar) {
        var QuestionType = CourseSketch.PROTOBUF_UTIL.getSrlBankProblemClass().QuestionType;
        if (problemType === QuestionType.SKETCH) {
            var updateManager = element.getUpdateManager();
            var clearButton = toolbar.createButton("/images/toolbar/clear_button.svg", function() {
                var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CLEAR, true);
                var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([command]);
                updateManager.addUpdate(update);
            });
            toolbar.appendChild(clearButton);

            toolbar.setUndoCallback(function() {
                var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.UNDO, true);
                var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([command]);
                updateManager.addUpdate(update);
            });

            toolbar.setRedoCallback(function() {
                var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.REDO, true);
                var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([command]);
                updateManager.addUpdate(update);
            });
        }  else if (problemType === QuestionType.MULT_CHOICE) {
            // add mult choice tools
        }   else if (problemType === QuestionType.FREE_RESP) {
            // add free resp tools
            toolbar.setUndoCallback(function() {
                document.execCommand("undo", false, null);
            });
            toolbar.setRedoCallback(function() {
                document.execCommand("redo", false, null);
            });
        }
        element = undefined;
        toolbar = undefined;
        problemType = undefined;
    };
}

SubmissionPanel.prototype = Object.create(HTMLElement.prototype);

/**
 * @param problem {SrlProblem} sets the problem element
 */
SubmissionPanel.prototype.setProblemType = function(problemType) {
    this.problemType = problemType;
};
