validateFirstRun(document.currentScript);

/**
 * @constructor ProblemRenderException
 * @extends BaseException
 *
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} [cause] - The cause of the exception.
 */
function FeedbackRendererException(message, cause) {
    this.name = 'FeedbackRenderer';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

FeedbackRendererException.prototype = new BaseException();

/**
 * Renders feedback and listens to the server.
 *
 * @constructor FeedbackRenderer
 * @param {Element} problemPanel - The element where all the data is being rendered.
 * @param {Element} basicFeedbackPanel - The element where we display basic feedback.
 * @param {Element} backgroundColorPanel - The element where we set the color depending on the state.
 */
function FeedbackRenderer(problemPanel, basicFeedbackPanel, backgroundColorPanel) {

    var startWaiting;
    var finishWaiting;
    var isRunning;
    var currentProblemId = undefined;
    var currentType = undefined;
    var isDirty = false; // True if the user changes anything while the feedback is being grabbed.
    var unknownFeedback = CourseSketch.prutil.FeedbackData();
    var initialColor = backgroundColorPanel.style.backgroundColor;
    var defaultErrorListener = function(error) {
        console.log(error);
    };
    var errorListener = defaultErrorListener;

    /**
     * Resets the data in the renderer to its initial value.
     */
    this.reset = function() {
        isRunning = undefined;
        startWaiting = undefined;
        finishWaiting = undefined;
        currentType = undefined;
        currentProblemId = undefined;
        errorListener = defaultErrorListener;
        renderBackgroundColor(CourseSketch.prutil.FeedbackState.UNKNOWN);
        CourseSketch.connection.setAnswerCheckingListener(undefined);
    };

    /**
     * Initializes and starts the waiting function.
     *
     * @param {Function} callback - Called after rendering is finished
     * @param {Boolean} [stopWaiting] - True if ending waiting should be forced.  False is it should not end waiting
     *                                  Leave unset if it should behave normally.
     * @returns {Function} A modified version of callback that ends waiting.
     */
    function setupWaiting(callback, stopWaiting) {
        var internalCallback = callback;
        if (!isUndefined(isRunning) && isRunning && !(!isUndefined(stopWaiting) && stopWaiting)) {
            return internalCallback;
        }
        if (!isUndefined(startWaiting) && !isUndefined(finishWaiting)) {
            if (!(!isUndefined(isRunning) && isRunning)) {
                startWaiting();
            }
            internalCallback = function() {
                if ((!isUndefined(stopWaiting) && stopWaiting) || isUndefined(stopWaiting)) {
                    finishWaiting();
                }
                callback();
            };
        }
        return internalCallback;
    }

    /**
     * @param {Function} errorListenerFunction - Called if there is an error in the renderer.
     */
    this.setErrorListener = function(errorListenerFunction) {
        errorListener = errorListenerFunction;
    };

    /**
     * Called every time you switch problems.
     *
     * This lets the renderer listen for feedback.
     *
     * @param {SrlBankProblem} currentProblem - The bank problem that is being rendered.
     * @param {SrlSubmission} currentSubmission - The bank problem that is being rendered.
     * @param {Boolean} stopWaiting - If false the {@code finishWaiting} function will not be called.
     * @param {Function} callback - Called after the data is rendered.
     */
    this.listenToFeedback = function(currentProblem, currentSubmission, stopWaiting, callback) {
        currentType = currentProblem.questionType;
        currentProblemId = currentProblem.id;

        (function(localProblemId) {
            CourseSketch.connection.setAnswerCheckingListener(function(event, feedbackRequest) {
                if (currentProblemId !== localProblemId) {
                    // The user has changed problems while waiting for feedback.
                    return;
                }
                var feedback = CourseSketch.prutil.decodeProtobuf(feedbackRequest.otherData, 'SubmissionFeedback');
                renderFeedback(feedback, currentProblem, currentSubmission, stopWaiting, function() {
                    isDirty = false;
                    callback();
                });
            });
        })(currentProblemId);
    };

    /**
     * Renders the bank problem.
     *
     * @param {SubmissionFeedback} feedback - The feedback of the problem.
     * @param {SrlBankProblem} currentProblem - The bank problem that is being rendered.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Boolean} stopWaiting - If false the {@code finishWaiting} function will not be called.
     * @param {Function} callback - Called after the data is rendered.
     */
    function renderFeedback(feedback, currentProblem, currentSubmission, stopWaiting, callback) {
        var internalCallback = setupWaiting(callback, stopWaiting);

        if (isUndefined(currentProblem.specialQuestionData) || currentProblem.specialQuestionData === null) {
            errorListener(new ProblemRenderException('Can not render an undefined bank problem'));
            internalCallback(currentProblem);
            return;
        }

        loadSpecificType(feedback.getFeedbackData(), currentSubmission, internalCallback);
    }
    // So we can load the last feedback from an existing problem.
    this.renderFeedback = renderFeedback;

    /**
     * Loads the data for the {@link QuestionType}.
     *
     * @param {FeedbackData} feedback - The feedback of the problem.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Function} callback - Called after the data is rendered.
     */
    function loadSpecificType(feedback, currentSubmission, callback) {
        if (isUndefined(feedback) || feedback === null) {
            renderBasicFeedback(unknownFeedback);
            callback();
            return;
        }
        renderBasicFeedback(feedback);
        if (isDirty) {
            // We can't do specific feedback on dirty data.
            callback();
            return;
        }
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            renderSketchFeedback(feedback.sketchArea, currentSubmission, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            renderTypingFeedback(feedback.freeResponse, currentSubmission, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            renderMultipleChoiceFeedback(feedback.multipleChoice, currentSubmission, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            renderCheckboxFeedback(feedback.multipleChoice, currentSubmission, callback);
        } else {
            errorListener(new ProblemRenderException('invalid questionType when rendering submission: ' + currentType));
            callback();
        }
    }

    /**
     * Renders feedback that is not specific to any problem.
     *
     * @param {FeedbackData} feedback - The holder of basic feedback
     */
    function renderBasicFeedback(feedback) {
        var basicFeedback = feedback.basicFeedback;
        var state = feedback.feedbackState;
        renderBackgroundColor(state);
    }

    /**
     * Renders the background state.
     *
     * @param {FeedbackState} state - The state of the problem.
     */
    function renderBackgroundColor(state) {
        if (state === CourseSketch.prutil.FeedbackState.UNKNOWN) {
            backgroundColorPanel.style.backgroundColor = initialColor;
        } else if (state === CourseSketch.prutil.FeedbackState.CORRECT) {
            backgroundColorPanel.setAttribute('style', 'background-color: #a5d6a7 !important');
        } else if (state === CourseSketch.prutil.FeedbackState.INCORRECT) {
            backgroundColorPanel.setAttribute('style', 'background-color: #ef9a9a !important');
        }
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {SketchAreaFeedback} feedback - The feedback of the problem.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Function} callback - Called after feedback is rendered
     */
    function renderSketchFeedback(feedback, currentSubmission, callback) {
        callback();
    }

    /**
     * Loads the typing from the {@link SrlBankProblem}.
     *
     * @param {FreeResponseFeedback} feedback - The feedback of the problem.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Function} callback - Called after feedback is rendered
     */
    function renderTypingFeedback(feedback, currentSubmission, callback) {
        callback();
    }

    /**
     * renders the multiple choice feedback
     *
     * @param {QuestionData} feedback - The feedback of the problem.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Function} callback - Called after feedback is rendered
     */
    function renderMultipleChoiceFeedback(feedback, currentSubmission, callback) {
        callback();
    }

    /**
     * Renders the checkbox feedback
     *
     * @param {QuestionData} feedback - The feedback of the problem.
     * @param {SrlSubmission} currentSubmission - The submission the student submitted that is being rendered.
     * @param {Function} callback - Called after feedback is rendered
     */
    function renderCheckboxFeedback(feedback, currentSubmission, callback) {
        callback();
    }

    /**
     * Called to start a waiting screen or other things to happen when it should start waiting.
     */
    this.startWaiting = function() {
        startWaiting();
    };

    /**
     * Called to end a waiting screen or other things to happen when it should stop waiting.
     */
    this.finishWaiting = function() {
        finishWaiting();
    };

    /**
     * @param {Function} startWaitingFunction - Sets a function to be called when it should start waiting.
     */
    this.setStartWaitingFunction = function(startWaitingFunction) {
        startWaiting = function() {
            isRunning = true;
            startWaitingFunction();
        };
    };

    /**
     * @param {Function} finishWaitingFunction - Sets a function to be called when it should finish waiting.
     */
    this.setFinishWaitingFunction = function(finishWaitingFunction) {
        finishWaiting = function() {
            isRunning = false;
            finishWaitingFunction();
        };
    };

    /**
     * Called when trying to kill the element.
     */
    this.finalize = function() {
        this.reset();
        problemPanel = undefined;
    };
}
CourseSketch.FeedbackRenderer = FeedbackRenderer;
