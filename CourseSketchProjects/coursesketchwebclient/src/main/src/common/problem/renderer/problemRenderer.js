validateFirstRun(document.currentScript);

/**
 * @constructor ProblemRenderException
 * @extends BaseException
 *
 * @param {String} message - The message to show for the exception.
 * @param {BaseException} [cause] - The cause of the exception.
 */
function ProblemRenderException(message, cause) {
    this.name = 'ProblemRenderException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

ProblemRenderException.prototype = new BaseException();


/**
 * Renders problem data given a bank problem.
 *
 * @constructor ProblemRenderer
 * @param {Element} problemPanel - The element where all the data is being rendered.
 */
function ProblemRenderer(problemPanel) {

    var currentSaveListener;
    var currentType;
    var specialQuestionData;
    var startWaiting;
    var finishWaiting;
    var isRunning;
    var isStudent;
    var isReadOnly;
    var isFullScreen;
    var defaultErrorListener = function(error) {
        console.log(error);
    };
    var errorListener = defaultErrorListener;

    /**
     * Resets the data in the renderer to its initial value.
     */
    this.reset = function() {
        isRunning = undefined;
        isReadOnly = undefined;
        isStudent = undefined;
        startWaiting = undefined;
        finishWaiting = undefined;
        currentType = undefined;
        specialQuestionData = undefined;
        currentSaveListener = undefined;
        errorListener = defaultErrorListener;
    };

    /**
     * Copys {@link QuestionData} from the {@link SrlBankProblem} to the local instance.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     */
    function copyQuestionData(bankProblem) {
        currentType = bankProblem.questionType;
        if (!isUndefined(specialQuestionData)) {
            return;
        }
        if (hasValidQuestionData(bankProblem.specialQuestionData)) {
            specialQuestionData = CourseSketch.prutil.cleanProtobuf(bankProblem.specialQuestionData, 'QuestionData');
        } else {
            specialQuestionData = CourseSketch.prutil.QuestionData();
        }
        var pojo = {};
        for (var property in specialQuestionData) {
            if (specialQuestionData.hasOwnProperty(property)) {
                pojo[property] = specialQuestionData[property];
            }
        }
        specialQuestionData = pojo;
    }

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
     * Renders the bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Function} callback - Called after the data is rendered.
     * @param {Boolean} [stopWaiting] - If false the {@code finishWaiting} function will not be called.
     */
    this.renderBankProblem = function(bankProblem, callback, stopWaiting) {
        var internalCallback = setupWaiting(callback, stopWaiting);

        if (isUndefined(bankProblem)) {
            errorListener(new ProblemRenderException('Can not render an undefined bank problem'));
            internalCallback(bankProblem);
            return;
        }

        copyQuestionData(bankProblem);
        loadSpecificType(specialQuestionData, isStudent, false, internalCallback);
    };

    /**
     * Renders the submission.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {SrlSubmission} submission - The student submission data.
     * @param {Function} callback - Called after the data is rendered.
     * @param {Boolean} [stopWaiting] - If false the {@code finishWaiting} function will not be called.
     */
    this.renderSubmission = function(bankProblem, submission, callback, stopWaiting) {
        if (isUndefined(submission)) {
            errorListener(new ProblemRenderException('Can not render and undefined submission'));
            this.renderBankProblem(bankProblem, callback, stopWaiting);
            return;
        }
        var submissionData = submission.submissionData;
        if (isUndefined(submissionData) || submissionData === null) {
            this.renderBankProblem(bankProblem, callback, stopWaiting);
            return;
        }
        var internalCallback = setupWaiting(callback, stopWaiting);

        copyQuestionData(bankProblem);
        var cleanedSubmissionData = CourseSketch.prutil.cleanProtobuf(submissionData, 'QuestionData');
        if (bankProblem.questionType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            copyMultipleChoiceQuestionData(bankProblem.specialQuestionData, cleanedSubmissionData);
        }
        loadSpecificType(cleanedSubmissionData, isStudent, true, internalCallback);
    };

    /**
     * Copies multiple choice data from the bank problem to the submission.
     *
     * @param {QuestionData} questionData - The bank problem that is being rendered.
     * @param {QuestionData} submissionData - The student submission data.
     */
    function copyMultipleChoiceQuestionData(questionData, submissionData) {
        try {
            if (isUndefined(submissionData.multipleChoice) ||
                submissionData.multipleChoice === null) {
                submissionData.multipleChoice = CourseSketch.prutil.MultipleChoice();
            }
            submissionData.multipleChoice.answerChoices =
                questionData.multipleChoice.answerChoices;
        } catch (exception) {
            console.error(exception);
        }
    }

    /**
     * Loads the data for the {@link QuestionType}.
     *
     * @param {QuestionData} questionData - The questionData that is being rendered.
     * @param {Boolean} isSubmission - True if a submission is happening.
     * @param {Boolean} reuse - True if it should try and reuse an existing element.
     * @param {Function} callback - Called after the data is rendered.
     */
    function loadSpecificType(questionData, isSubmission, reuse, callback) {
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            loadSketch(questionData, reuse, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            loadTyping(questionData, reuse, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE ||
            currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            loadMultipleChoice(questionData, isSubmission, reuse,
                currentType === CourseSketch.prutil.QuestionType.CHECK_BOX, callback);
        } else {
            errorListener(new ProblemRenderException('invalid questionType when rendering submission: ' + currentType));
            callback();
        }
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {QuestionData} questionData - questionData
     * @param {Boolean} reuse - True if it should try and reuse an existing element.
     * @param {Function} callback - Called after data is loaded.
     */
    function loadSketch(questionData, reuse, callback) {
        problemPanel.emptyPanel();
        var sketchSurface = document.createElement('sketch-surface');
        if (!isUndefined(isReadOnly) && isReadOnly) {
            sketchSurface.setAttribute('read-only', '');
        }
        sketchSurface.className = 'sub-panel submittable';
        setFullScreen(sketchSurface);
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = '100%';
        if (!isUndefined(errorListener)) {
            sketchSurface.setErrorListener(errorListener);
        }

        var sketchArea = questionData.sketchArea;

        loadIntoSketchSurface(sketchArea, sketchSurface, callback);
    }

    /**
     * Loads data into the sketch surface.
     *
     * @param {SketchArea} sketchArea - The proto holding sketch data.
     * @param {SketchSurface} sketchSurface - The element that gets loaded with data.
     * @param {Function} callback - Called after the data is loaded.
     */
    function loadIntoSketchSurface(sketchArea, sketchSurface, callback) {
        if (isUndefined(sketchArea) || sketchArea === null ||
            isUndefined(sketchArea.recordedSketch) || sketchArea.recordedSketch === null) {
            if (!sketchSurface.isInitialized()) {
                problemPanel.appendChild(sketchSurface);
            }
            callback();
            return;
        }
        if (!sketchSurface.isInitialized()) {
            sketchSurface.dataset.existinglist = '';
            problemPanel.appendChild(sketchSurface);
            sketchSurface.refreshSketch();
        }

        sketchSurface.loadUpdateList(sketchArea.recordedSketch.getList(), undefined, function() {
            callback();
        });
    }

    /**
     * Loads the typing from the {@link SrlBankProblem}.
     *
     * @param {QuestionData} questionData - questionData
     * @param {Boolean} reuse - True if it should try and reuse an existing element.
     * @param {Function} callback - Called after data is loaded.
     */
    function loadTyping(questionData, reuse, callback) {
        var typingSurface = problemPanel.querySelector('textarea.sub-panel.card-panel.submittable');
        if (isUndefined(typingSurface) || typingSurface === null || isUndefined(reuse) || !reuse) {
            problemPanel.emptyPanel();
            typingSurface = document.createElement('textarea');
            typingSurface.className = 'sub-panel card-panel submittable';
            typingSurface.contentEditable = true;
            setFullScreen(typingSurface);
            problemPanel.appendChild(typingSurface);
        }
        if (isReadOnly) {
            typingSurface.setAttribute('disabled', '');
        }
        var freeResponse = questionData.freeResponse;
        loadIntoTyping(freeResponse, typingSurface, callback);
    }

    /**
     * Loads data into the text area.
     *
     * @param {FreeResponse} freeResponse - The proto holding the text data.
     * @param {Element} typingSurface - The element that gets loaded with data.
     * @param {Function} callback - Called after the data is loaded.
     */
    function loadIntoTyping(freeResponse, typingSurface, callback) {
        if (!isUndefined(freeResponse) && freeResponse !== null && !isUndefined(freeResponse.startingText)) {
            typingSurface.value = freeResponse.startingText;
        }
        callback();
    }

    /**
     * Loads the multiple choice from the {@link SrlBankProblem}
     *
     * @param {QuestionData} questionData - questionData
     * @param {Boolean} isSubmission - True if a submission is happening.
     * @param {Boolean} reuse - True if it should try and reuse an existing element.
     * @param {Boolean} isCheckbox - True if it should try and render the element as a checkbox.
     * @param {Function} callback - Called after data is loaded.
     */
    function loadMultipleChoice(questionData, isSubmission, reuse, isCheckbox, callback) {
        var multiChoice = problemPanel.querySelector('multi-choice.sub-panel.card-panel.submittable');
        if (isUndefined(multiChoice) || multiChoice === null || isUndefined(reuse) || !reuse) {
            problemPanel.emptyPanel();
            multiChoice = document.createElement('multi-choice');
            multiChoice.className = 'sub-panel card-panel submittable col offset-s3 s9';
            setFullScreen(multiChoice);
            multiChoice.style.marginTop = '60px';
            problemPanel.appendChild(multiChoice);
        }

        loadIntoMultipleChoice(questionData.multipleChoice, multiChoice, isSubmission, isCheckbox, callback);
    }

    /**
     * Loads data into the text area.
     *
     * @param {MultipleChoice} multipleChoice - The proto holding the multiple choice data.
     * @param {MultiChoice} multiChoiceElement - The element that gets loaded with data.
     * @param {Boolean} isSubmission - True if a submission is happening.
     * @param {Boolean} isCheckbox - True if it should try and render the element as a checkbox.
     * @param {Function} callback - Called after the data is loaded.
     */
    function loadIntoMultipleChoice(multipleChoice, multiChoiceElement, isSubmission, isCheckbox, callback) {
        if (isSubmission) {
            multiChoiceElement.turnOnStudentMode();
        }
        // eslint-disable-next-line require-jsdoc
        function newCallback() {
            if (isReadOnly) {
                multiChoiceElement.turnOnReadOnlyMode();
            }

            if (isCheckbox) {
                multiChoiceElement.turnOnCheckboxMode();
            }
            callback();
        }
        multiChoiceElement.setAttribute('data-mode', isSubmission ? 'student' : 'instructor');
        if (isUndefined(multipleChoice) || multipleChoice === null) {
            errorListener(new ProblemRenderException('Invalid multiple choice data occured for problem'));
            multiChoiceElement.loadData(CourseSketch.prutil.MultipleChoice());
            newCallback();
            return;
        }
        multiChoiceElement.loadData(multipleChoice);
        newCallback();
    }

    /**
     * Saves the current data in the renderer to the given bank problem.
     * Only allows a single type to be saved at a time.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Function} callback - Called after the data is saved.
     */
    this.saveData = function(bankProblem, callback) {
        saveToQuestionData(function() {
            var questionData = CourseSketch.prutil.QuestionData();
            if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
                questionData.sketchArea = specialQuestionData.sketchArea;
            } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
                questionData.freeResponse = specialQuestionData.freeResponse;
            } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE ||
                currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
                questionData.multipleChoice = specialQuestionData.multipleChoice;
            }
            bankProblem.specialQuestionData = questionData;
            callback();
        });
    };

    /**
     * Saves the data internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    this.stashData = function(callback) {
        saveToQuestionData(callback);
    };

    /**
     * Saves the data internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    function saveToQuestionData(callback) {
        // switch by current type
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            saveSketch(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            saveTyping(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE ||
            currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            saveMultipleChoice(callback);
        }
    }

    /**
     * Saves the sketch internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    function saveSketch(callback) {
        var sketchSurface = problemPanel.querySelector('sketch-surface');
        var sketchArea = CourseSketch.prutil.SketchArea();
        sketchArea.recordedSketch = sketchSurface.getSrlUpdateListProto();
        specialQuestionData.sketchArea = sketchArea;
        callback();
    }

    /**
     * Saves typing data internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    function saveTyping(callback) {
        var freeResponse = CourseSketch.prutil.FreeResponse();
        freeResponse.startingText = problemPanel.querySelector('textarea').value;
        specialQuestionData.freeResponse = freeResponse;
        callback();
    }

    /**
     * Saves multipleChoice data internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    function saveMultipleChoice(callback) {
        var multipleChoiceElement = problemPanel.querySelector('multi-choice');
        specialQuestionData.multipleChoice = multipleChoiceElement.saveData();
        callback();
    }

    /**
     * @param {QuestionData} questionData The data that is being checked if it exists.
     * @returns {Boolean} True if the question data exists.
     */
    function hasValidQuestionData(questionData) {
        return !isUndefined(questionData) && questionData !== null;
    }

    /**
     * @param {Element} element - The element that is being set with full screen.
     */
    function setFullScreen(element) {
        if (isFullScreen) {
            element.className += ' full-screen';
        }
    }


    /**
     * @param {Boolean} studentProblem True if what is being rendered should be treated as a student problem.
     */
    this.setIsStudentProblem = function(studentProblem) {
        isStudent = studentProblem;
    };

    /**
     * @param {Function} errorListenerFunction - Called if there is an error in the renderer.
     */
    this.setErrorListener = function(errorListenerFunction) {
        errorListener = errorListenerFunction;
    };

    /**
     * @param {Boolean} readOnly - True if the rendered data should be read only and not editable.
     */
    this.setReadOnly = function(readOnly) {
        isReadOnly = readOnly;
    };

    /**
     * @param {Boolean} fullScreen - True if the rendered data should be full screen.
     */
    this.setFullScreen = function(fullScreen) {
        isFullScreen = fullScreen;
    };

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
CourseSketch.ProblemRenderer = ProblemRenderer;
