/**
 * An exception that is thrown for the uses of submissions.
 *
 * @extends BaseException
 * @class SubmissionException
 */
function SubmissionException(message, cause) {
    this.name = 'SubmissionException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}
SubmissionException.prototype = new BaseException();

/**
 * A class that handles submitting a problem to the database and listening for the result.
 *
 * This class does not retrieve submissions.
 *
 * Assumptions made:
 * <ul>
 * <li>Toolbar is a custom element that has two functions: setSaveCallback(callback), setSubmitCallback(callback).</li>
 * <li>The toolbar is set before the element is inserted.  (or it will never be inserted).</li>
 * <li>The sub-panel (submit panel) element can change at run time and may not be inserted when this element is inserted.</li>
 * <li>You can set the problem object with the class "sub-panel".</li>
 * </ul>
 *
 * @class SubmissionPanel
 * @property {QuestionType}
 */
function SubmissionPanel() {

    /**
     * @param {Element} templateClone - An element representing the data inside tag, its
     *            content has already been imported and then added to this element.
     * @instance
     * @memberof SubmissionPanel
     */
    this.initializeElement = function(templateClone) {
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
        this.setCallbacks();
    };

    /**
     * Sets the callback for the toolbar buttons if the toolbar exists.
     *
     * @see Toolbar
     * @instance
     * @memberof SubmissionPanel
     */
    this.setCallbacks = function() {
        var toolbar = this.shadowRoot.querySelector('#toolbar').getDistributedNodes()[0];
        if (toolbar === null) {
            return; //Quit before infinite loop
        }
        // Toolbar may not be set up by the time this is called, so we wait till it is set up.
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

    /**
     * This sends data to the server but catches any exception.
     *
     * This method should only be used for testing purposes.
     *
     * @instance
     * @memberof SubmissionPanel
     * @param {Boolean} isSubmitting - True if the data is being submitted.
     * @param {Boolean} suppressAlert - True if the alert is being suppressed (used for testing purposes)
     * @see SubmissionPanel#sendDataToServer
     */
    this.sendDataToServerExceptionWrapped = function(isSubmitting, suppressAlert) {
        try {
            this.sendDataToServer(isSubmitting);
        } catch (exception) {
            if (!suppressAlert) {
                CourseSketch.clientException(exception);
            }
            console.log(exception);
        }
    };

    /**
     * Sends the submission to the server.
     *
     * @param {Boolean} isSubmitting - true if the data is a submission as opposed to just a normal save.
     * @throws {SubmissionException} - thrown if there is a problem
     * @instance
     * @memberof SubmissionPanel
     */
    this.sendDataToServer = function(isSubmitting) {
        var subPanel = this.querySelector('.submittable');
        if (isUndefined(subPanel)) {
            throw new SubmissionException('There is no element that contains submittable data');
        }
        if (isUndefined(this.problemType)) {
            throw new SubmissionException('Problem data is not set correctly aborting');
        }
        var submission = undefined;
        var QuestionType = CourseSketch.prutil.QuestionType;
        switch (this.problemType) {
            case QuestionType.SKETCH:
                submission = createSketchSubmission(subPanel, isSubmitting);
                break;
            case QuestionType.FREE_RESP:
                submission = createTextSubmission(subPanel, isSubmitting);
                break;
        }
        if (isUndefined(submission)) {
            throw new SubmissionException('submission type not supported, aborting');
        }
        if (isUndefined(this.wrapperFunction)) {
            // You need to set the wrapper function to either create an experiment or solution.
            throw new SubmissionException('Wrapper function is not set, aborting');
        }
        var submittingValue = this.wrapperFunction(submission);
        console.log(submittingValue);
        var submissionRequest = CourseSketch.prutil.createRequestFromData(submittingValue,
                CourseSketch.prutil.getRequestClass().MessageType.SUBMISSION);
        var problemType = this.problemType;
        var problemIndex = this.problemIndex;
        CourseSketch.connection.setSubmissionListener(function(event, request) {
            console.log(request);
            CourseSketch.connection.setSubmissionListener(undefined);
            alert(request.responseText);
            if (problemIndex === this.problemIndex && this.problemType === CourseSketch.prutil.QuestionType.SKETCH) {
                var sketchSurface = this.querySelector('.submittable');
                // Potential conflict if it was save multiple times in quick succession.
                sketchSurface.getUpdateManager().setLastSaveTime(request.getMessageTime());
                console.log('submission has been updated with the latest time', request.getMessageTime().toString());
            }
            problemType = undefined;
            problemIndex = undefined;
        }.bind(this));
        submissionRequest.setResponseText(this.isStudent ? 'student' : this.isGrader ? 'grader' : 'instructor');
        CourseSketch.connection.sendRequest(submissionRequest);
        QuestionType = undefined;
        submission = undefined;
        subPanel = undefined;
    };

    /**
     * Gets the text that has been typed.
     *
     * @return {SrlSubmission} object that is ready to be sent to the server.
     *
     * @param {Element} textArea - The element that contains the text answer
     * @param {Boolean} isSubmitting - Value Currently ignored but in the future it may be used.
     * @instance
     * @memberof SubmissionPanel
     */
    function createTextSubmission(textArea, isSubmitting) {
        var submission = createBaseSubmission();
        submission.textAnswer = textArea.value;
        return submission;
    }

    /**
     * Creates the submission object for the sketch surface.
     *
     * This also adds the submit or save marker to the update list.
     *
     * @param {SketchSurface} sketchSurface - The sketch surface that is being submitted.
     * @param {Boolean} isSubmitting - True if this is a submission instead of a save.
     * @return {SrlSubmission} object that is ready to be sent to the server.
     * @instance
     * @memberof SubmissionPanel
     */
    function createSketchSubmission(sketchSurface, isSubmitting) {
        var updateManager = sketchSurface.getUpdateManager();

        if (isSubmitting && !updateManager.isValidForSubmission()) {
            throw new SubmissionException('must make changes to resubmit.');
        }
        if (!isSubmitting && !updateManager.isValidForSaving()) {
            throw new SubmissionException('must make changes to save again.');
        }

        var MarkerType = CourseSketch.prutil.getMarkerClass().MarkerType;
        var markerCommand = updateManager.createMarker(true, isSubmitting ? MarkerType.SUBMISSION : MarkerType.SAVE);
        var markerUpdate = CourseSketch.prutil.createUpdateFromCommands([ markerCommand ]);
        updateManager.addSynchronousUpdate(markerUpdate);

        var protoObject = sketchSurface.getSrlUpdateListProto();
        var submission = createBaseSubmission();
        submission.setUpdateList(protoObject);
        return submission;
    }

    /**
     * @returns {SrlSubmission} a blank protobuf submission object.
     * @access private
     * @memberof SubmissionPanel
     */
    function createBaseSubmission() {
        var submission = CourseSketch.prutil.SrlSubmission();
        return submission;
    }

    /**
     * Sets the wrapperFunction.
     *
     * This function takes in a submission and wraps it as either the experiment or solution.
     * This wrapped value is returned from the function and then it is sent to the server internally.
     *
     * @param  {Function} wrapperFunction - used to wrap the submission in its required data.
     * @instance
     * @memberof SubmissionPanel
     */
    this.setWrapperFunction = function(wrapperFunction) {
        this.wrapperFunction = wrapperFunction;
    };

    /**
     * Called when the panel is removed from the DOM.
     *
     * @instance
     * @memberof SubmissionPanel
     */
    this.detachedCallback = function() {
        this.setWrapperFunction(undefined);
    };

    /**
     * This clears the toolbar and remakes the callbacks for the toolbar.
     *
     * @instance
     * @memberof SubmissionPanel
     */
    this.refreshPanel = function() {
        var subPanel = this.shadowRoot.querySelector('#sub-panel').getDistributedNodes()[0];
        var toolbar = this.shadowRoot.querySelector('#toolbar').getDistributedNodes()[0];
        toolbar.clearCallbacks();
        toolbar.innerHTML = '';
        this.setCallbacks();
        this.setSpecificCallbacks(this.problemType, subPanel, toolbar);
    };

    /**
     * Empties all .submittable and all .sub-panel from this submission panel.
     */
    this.emptyPanel = function() {
        [].forEach.call(this.querySelectorAll('.sub-panel'), function(item) {
            item.parentNode.removeChild(item);
        });
    };

    /**
     * Makes callbacks for the toolbar that depend on the type of problem.
     *
     * @param {QuestionType} problemType - The type of problem that is currently being submitted.
     * @param {Element} element - The element contained inside the submission panel.
     * @param {Toolbar} toolbar - The custom toolbar element that is contained inside the submission panel.
     * @instance
     * @memberof SubmissionPanel
     */
    this.setSpecificCallbacks = function(problemType, element, toolbar) {
        var QuestionType = CourseSketch.prutil.QuestionType;
        if (problemType === QuestionType.SKETCH) {
            var updateManager = element.getUpdateManager();
            var clearButton = toolbar.createButton('/images/toolbar/clear_button.svg', function() {
                var command = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.CLEAR, true);
                var update = CourseSketch.prutil.createUpdateFromCommands([ command ]);
                updateManager.addUpdate(update);
            });
            toolbar.appendChild(clearButton);

            toolbar.setUndoCallback(function() {
                var command = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.UNDO, true);
                var update = CourseSketch.prutil.createUpdateFromCommands([ command ]);
                updateManager.addUpdate(update);
            });

            toolbar.setRedoCallback(function() {
                var command = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.REDO, true);
                var update = CourseSketch.prutil.createUpdateFromCommands([ command ]);
                updateManager.addUpdate(update);
            });
        } else if (problemType === QuestionType.MULT_CHOICE) {
            throw new BaseException('Operation not supported');
            // add mult choice tools
        } else if (problemType === QuestionType.FREE_RESP) {
            // add free resp tools
            toolbar.setUndoCallback(function() {
                document.execCommand('undo', false, null);
            });
            toolbar.setRedoCallback(function() {
                document.execCommand('redo', false, null);
            });
        }
        element = undefined;
        toolbar = undefined;
        problemType = undefined;
    };
}

SubmissionPanel.prototype = Object.create(HTMLElement.prototype);

/**
 * Sets the problem type for the submission panel.
 *
 * The problem type is used to detirmine how to load and save the panel.
 *
 * @param {QuestionType} problemType - Sets the problem element.
 * @instance
 * @memberof SubmissionPanel
 */
SubmissionPanel.prototype.setProblemType = function(problemType) {
    this.problemType = problemType;
};
