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

    /**
     * Resets the data in the renderer to its initial value.
     */
    this.reset = function() {
        startWaiting;
        finishWaiting;
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

    function setupWaiting(callback) {
        var internalCallback = callback;
        if (!isUndefined(isRunning) && isRunning) {
            return internalCallback;
        }
        if (!isUndefined(startWaiting) && !isUndefined(finishWaiting)) {
            startWaiting();
            internalCallback = function() {
                finishWaiting();
                callback();
            }
        }
        return internalCallback;
    }

    /**
     * Renders the bank problem.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Function} callback - Called after the data is rendered.
     */
    this.renderBankProblem = function(bankProblem, callback) {
        copyQuestionData(bankProblem);
        var internalCallback = setupWaiting(callback);
        loadSpecificType(bankProblem, internalCallback);
    };

    /**
     * Renders the submission.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Submission} submission - The student submission data.
     * @param {Function} callback - Called after the data is rendered.
     */
    this.renderSubmission = function(bankProblem, submission, callback) {
        if (isUndefined(submission)) {
            throw new ProblemRenderException('Can not render and undefined submission');
        }
        var internalCallback = setupWaiting(callback);

        if (isUndefined(specialQuestionData)) {
            this.renderBankProblem(bankProblem, function() {
                renderSubmission(submission, internalCallback);
            });
        } else {
            renderSubmission(submission, internalCallback);
        }
    };

    function renderSubmission(submission, callback) {
        var submissionData = submission.submissionData;
        if (isUndefined(submissionData) || submissionData === null) {
            callback();
            return;
        }
        renderSubmissionSpecificType(submissionData, callback);
    }

    function renderSubmissionSpecificType(submission, callback) {
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            loadIntoSketchSurface(submission.sketchArea, problemPanel.querySelector('sketch-surface'), callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            loadIntoTyping(submission.freeResponse, problemPanel.querySelector('textArea'), callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            loadIntoMultipleChoice(submission.multipleChoice, problemPanel.querySelector('multi-choice'), true, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            throw new ProblemRenderException('Checkbox is not supported');
        }

    }

    /**
     * Loads the data for the {@link QuestionType}.
     *
     * @param {SrlBankProblem} bankProblem - The bank problem that is being rendered.
     * @param {Function} callback - Called after the data is rendered.
     */
    function loadSpecificType(bankProblem, callback) {
        problemPanel.emptyPanel();
        var type = bankProblem.questionType;
        currentType = type;
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            loadSketch(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            loadTyping(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            loadMultipleChoice(callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            loadCheckBox(callback);
        }
    }
    
    this.setStartWaitingFunction = function(startWaitingFunction) {
        startWaiting = function() {
            isRunning = true;
            startWaitingFunction();
        }
    };

    this.setFinishWaitingFunction = function(finishWaitingFunction) {
        finishWaiting = function() {
            isRunning = false;
            finishWaitingFunction();
        }
    };

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {Function} callback - Called after data is loaded.
     */
    function loadSketch(callback) {
        var sketchSurface = document.createElement('sketch-surface');
        sketchSurface.className = 'sub-panel submittable';
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = '100%';
        sketchSurface.setErrorListener(function(exception) {
            console.log(exception);
            alert(exception);
        });

        if (!hasValidQuestionData(specialQuestionData)) {
            document.getElementById('problemPanel').appendChild(sketchSurface);
            return;
        }

        var sketchArea = specialQuestionData.sketchArea;

        loadIntoSketchSurface(sketchArea, sketchSurface, callback);
    }

    function loadIntoSketchSurface(sketchArea, sketchSurface, callback) {
        if (isUndefined(sketchArea) || sketchArea === null
            || isUndefined(sketchArea.recordedSketch) || sketchArea.recordedSketch === null) {
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
     * @param {Function} callback - Called after data is loaded.
     */
    function loadTyping(callback) {
        var typingSurface = document.createElement('textarea');
        typingSurface.className = 'sub-panel card-panel';
        typingSurface.contentEditable = true;
        problemPanel.appendChild(typingSurface);
        if (!hasValidQuestionData(specialQuestionData)) {
            callback();
            return;
        }
        var freeResponse = specialQuestionData.freeResponse;
        loadIntoTyping(freeResponse, typingSurface, callback);
    }
    
    function loadIntoTyping(freeResponse, typingSurface, callback) {
        if (!isUndefined(freeResponse) && freeResponse !== null && !isUndefined(freeResponse.startingText)) {
            typingSurface.value = freeResponse.startingText;
        }
        callback();
    }

    /**
     * Loads the multiple choice from the {@link SrlBankProblem}
     *
     * @param {Function} callback - Called after data is loaded.
     */
    function loadMultipleChoice(callback) {
        var multiChoice = document.createElement('multi-choice');
        multiChoice.className = 'sub-panel card-panel submittable col offset-s3 s9';
        multiChoice.style.marginTop = '60px';

        problemPanel.appendChild(multiChoice);

        if (!hasValidQuestionData(specialQuestionData)) {
            callback();
            return;
        }
        loadIntoMultipleChoice(specialQuestionData.multipleChoice, multiChoice, false, callback);
    }

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
        if (!isSubmission) {
            multiChoiceElement.loadData(multipleChoice);
            callback();
            return;
        }
        var id = multipleChoice.correctId;
        if (!isUndefined(id) && id !== null) {
            multiChoiceElement.setSelected(id);
        }
        callback();
    }

    /**
     * Loads the checkbox from the {@link SrlBankProblem}
     *
     * @param {Function} callback - Called after data is loaded.
     */
    function loadCheckBox(callback) {
        callback();
        return;

        var question = document.createElement('question-element');
        var multiChoice = document.createElement('multi-choice');
        problemPanel.appendChild(question);
        question.addAnswerContent(multiChoice);

        if (!hasValidQuestionData(specialQuestionData)) {
            callback();
            return;
        }
        var checkBox = specialQuestionData.checkBox;
        if (!isUndefined(checkBox) && checkBox !== null) {
            question.loadData(checkBox);
        } else {
            // load in empty data
            question.loadData(CourseSketch.prutil.CheckBox());
        }
        callback();
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
