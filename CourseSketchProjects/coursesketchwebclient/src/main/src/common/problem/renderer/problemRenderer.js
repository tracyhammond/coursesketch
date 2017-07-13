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

    /**
     * Resets the data in the renderer to its initial value.
     */
    this.reset = function() {
        isStudent = undefined;
        startWaiting = undefined;
        finishWaiting = undefined;
        currentType = undefined;
        specialQuestionData = undefined;
        currentSaveListener = undefined;
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
     * @param {Boolean} studentProblem True if what is being rendered should be treated as a student problem.
     */
    this.setIsStudentProblem = function(studentProblem) {
        isStudent = studentProblem;
    };

    /**
     * Renders the bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Function} callback - Called after the data is rendered.
     * @param {Boolean} stopWaiting - If false the {@code finishWaiting} function will not be called.
     */
    this.renderBankProblem = function(bankProblem, callback, stopWaiting) {
        var internalCallback = setupWaiting(callback, stopWaiting);

        if (isUndefined(bankProblem)) {
            console.error(new ProblemRenderException('Can not render an undefined bank problem'));
            internalCallback(bankProblem);
            return;
        }

        copyQuestionData(bankProblem);
        loadSpecificType(specialQuestionData, isStudent, internalCallback);
    };

    /**
     * Renders the submission.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {SrlSubmission} submission - The student submission data.
     * @param {Function} callback - Called after the data is rendered.
     * @param {Boolean} stopWaiting - If false the {@code finishWaiting} function will not be called.
     */
    this.renderSubmission = function(bankProblem, submission, callback, stopWaiting) {
        if (isUndefined(submission)) {
            console.error(new ProblemRenderException('Can not render and undefined submission'));
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
        loadSpecificType(submission.submissionData, isStudent, internalCallback);
    };

    /**
     * Loads the data for the {@link QuestionType}.
     *
     * @param {QuestionData} questionData - The questionData that is being rendered.
     * @param {Boolean} isSubmission - True if a submission is happening.
     * @param {Function} callback - Called after the data is rendered.
     */
    function loadSpecificType(questionData, isSubmission, callback) {
        problemPanel.emptyPanel();

        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            loadSketch(questionData, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            loadTyping(questionData, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            loadMultipleChoice(questionData, isSubmission, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            loadCheckBox(questionData, callback);
        } else {
            console.log(new ProblemRenderException('invalid questionType when rendering submission: ' + currentType));
            callback();
        }
    }

    this.startWaiting = function() {
        startWaiting();
    };

    this.setStartWaitingFunction = function(startWaitingFunction) {
        startWaiting = function() {
            isRunning = true;
            startWaitingFunction();
        };
    };

    this.setFinishWaitingFunction = function(finishWaitingFunction) {
        finishWaiting = function() {
            isRunning = false;
            finishWaitingFunction();
        };
    };

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {QuestionData} questionData - questionData
     * @param {Function} callback - Called after data is loaded.
     */
    function loadSketch(questionData, callback) {
        var sketchSurface = document.createElement('sketch-surface');
        sketchSurface.className = 'sub-panel submittable';
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = '100%';
        sketchSurface.setErrorListener(function(exception) {
            console.log(exception);
            alert(exception);
        });

        if (!hasValidQuestionData(questionData)) {
            document.getElementById('problemPanel').appendChild(sketchSurface);
            return;
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
     * @param {QuestionData} questionData The data that is being checked if it exists.
     * @returns {Boolean} True if the question data exists.
     */
    function hasValidQuestionData(questionData) {
        return !isUndefined(questionData) && questionData !== null;
    }


    /**
     * Loads the typing from the {@link SrlBankProblem}.
     *
     * @param {QuestionData} questionData - questionData
     * @param {Function} callback - Called after data is loaded.
     */
    function loadTyping(questionData, callback) {
        var typingSurface = document.createElement('textarea');
        typingSurface.className = 'sub-panel card-panel';
        typingSurface.contentEditable = true;
        problemPanel.appendChild(typingSurface);
        if (!hasValidQuestionData(questionData)) {
            callback();
            return;
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
     * @param {Function} callback - Called after data is loaded.
     */
    function loadMultipleChoice(questionData, isSubmission, callback) {
        var multiChoice = document.createElement('multi-choice');
        multiChoice.className = 'sub-panel card-panel submittable col offset-s3 s9';
        multiChoice.style.marginTop = '60px';

        problemPanel.appendChild(multiChoice);

        if (!hasValidQuestionData(questionData)) {
            callback();
            return;
        }
        loadIntoMultipleChoice(questionData.multipleChoice, multiChoice, isSubmission, callback);
    }

    /**
     * Loads data into the text area.
     *
     * @param {MultipleChoice} multipleChoice - The proto holding the multiple choice data.
     * @param {MultiChoice} multiChoiceElement - The element that gets loaded with data.
     * @param {Boolean} isSubmission - True if a submission is happening.
     * @param {Function} callback - Called after the data is loaded.
     */
    function loadIntoMultipleChoice(multipleChoice, multiChoiceElement, isSubmission, callback) {
        if (isSubmission) {
            multiChoiceElement.turnOnStudentMode();
        }
        if (isUndefined(multipleChoice) || multipleChoice === null) {
            if (!isSubmission) {
                multiChoiceElement.loadData(CourseSketch.prutil.MultipleChoice());
            }
            callback();
            return;
        }
        multiChoiceElement.loadData(multipleChoice);

        if (isSubmission) {
            var id = multipleChoice.correctId;
            if (!isUndefined(id) && id !== null) {
                multiChoiceElement.setSelected(id);
            }
        }
        callback();
    }

    /**
     * Loads the checkbox from the {@link SrlBankProblem}
     *
     * @param {QuestionData} questionData - questionData
     * @param {Function} callback - Called after data is loaded.
     */
    function loadCheckBox(questionData, callback) {
        callback();

        /*
        return;
        var question = document.createElement('question-element');
        var multiChoice = document.createElement('multi-choice');
        problemPanel.appendChild(question);
        question.addAnswerContent(multiChoice);

        if (!hasValidQuestionData(questionData)) {
            callback();
            return;
        }
        var checkBox = questionData.checkBox;
        if (!isUndefined(checkBox) && checkBox !== null) {
            question.loadData(checkBox);
        } else {
            // load in empty data
            question.loadData(CourseSketch.prutil.CheckBox());
        }
        callback();
        */
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
            } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
                questionData.multipleChoice = specialQuestionData.multipleChoice;
            } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
                questionData.checkBox = specialQuestionData.checkBox;
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
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            saveMultipleChoice(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            saveCheckbox(callback);
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
        multipleChoiceElement.setFinishedListener(function(command, evnt, update, multiChoice) {
            specialQuestionData.multipleChoice = multiChoice;
            callback();
        });
        multipleChoiceElement.saveData();
    }

    /**
     * Saves checkbox data internally.
     *
     * @param {Function} callback - Called after data is saved.
     */
    function saveCheckbox(callback) {
        var multipleChoiceElement = problemPanel.querySelector('multi-choice');
        multipleChoiceElement.setFinishedListener(function(command, evnt, update, multiChoice) {
            specialQuestionData.checkBox = multiChoice;
            callback();
        });
    }
}
CourseSketch.ProblemRenderer = ProblemRenderer;
