validateFirstRun(document.currentScript);

/**
 * Renders problem data given a bank problem.
 */
function ProblemRenderer(problemPanel) {

    var currentSaveListener;
    var currentType;
    var specialQuestionData;

    this.reset = function() {
        currentType = undefined;
        specialQuestionData = undefined;
        currentSaveListener = undefined;
    };

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

    this.renderBankProblem = function(bankProblem, callback) {
        copyQuestionData(bankProblem);
        loadSpecificType(bankProblem, callback);
    };

    this.renderSubmission = function(bankProblem, submission, callback) {
        this.renderBankProblem(bankProblem, function() {
            
        });
    };

    function loadSpecificType(bankProblem, callback) {
        problemPanel.innerHTML = "";
        var type = bankProblem.questionType;
        currentType = type;
        if (currentType === CourseSketch.prutil.QuestionType.SKETCH) {
            loadSketch(specialQuestionData, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            loadTyping(specialQuestionData, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.MULT_CHOICE) {
            loadMultipleChoice(specialQuestionData, callback);
        } else if (currentType === CourseSketch.prutil.QuestionType.CHECK_BOX) {
            loadCheckBox(specialQuestionData, callback);
        }
    }

    function setupLoadingIcon() {
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        document.getElementById('percentBar').appendChild(element);
        element.startWaiting();
        var realWaiting = element.finishWaiting.bind(element);

        /**
         * Called when the sketch surface is done loading to remove the overlay.
         */
        element.finishWaiting = function() {
            realWaiting();
            sketchSurface.refreshSketch();
            CourseSketch.studentExperiment.removeWaitOverlay();
            sketchSurface = undefined;
            element = undefined;
        };
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {QuestionData} specialQuestionData - The special data
     */
    function loadSketch(specialQuestionData, callback) {
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

        if (isUndefined(sketchArea) || sketchArea === null || isUndefined(sketchArea.recordedSketch)) {
            document.getElementById('problemPanel').appendChild(sketchSurface);
            callback();
            return;
        }
        // tell the surface not to create its own sketch.
        sketchSurface.dataset.existinglist = '';

        // adding here because of issues
        problemPanel.appendChild(sketchSurface);

        // add after attributes are set.

        sketchSurface.refreshSketch();

        sketchSurface.loadUpdateList(sketchArea.recordedSketch.getList(), undefined, function() {
            callback();
        });
    }

    function hasValidQuestionData(specialQuestionData) {
        return !isUndefined(specialQuestionData) && specialQuestionData !== null
    }


    /**
     * Loads the typing from the submission.
     *
     * @param {SrlBankProblem} navigator - The assignment navigator.
     */
    function loadTyping(specialQuestionData, callback) {
        var typingSurface = document.createElement('textarea');
        typingSurface.className = 'sub-panel card-panel';
        typingSurface.contentEditable = true;
        problemPanel.appendChild(typingSurface);
        if (!hasValidQuestionData(specialQuestionData)) {
            callback();
            return;
        }
        var freeResponse = specialQuestionData.freeResponse;
        if (!isUndefined(freeResponse) && freeResponse !== null && !isUndefined(freeResponse.startingText)) {
            typingSurface.value = freeResponse.startingText;
        }
        callback();
    }

    function loadMultipleChoice(specialQuestionData, callback) {
        var multiChoice = document.createElement('multi-choice');
        multiChoice.className = 'sub-panel card-panel submittable col offset-s3 s9';
        multiChoice.style.marginTop = '60px';


        problemPanel.appendChild(multiChoice);

        if (!hasValidQuestionData(specialQuestionData)) {
            callback();
            return;
        }
        var multipleChoice = specialQuestionData.multipleChoice;
        if (!isUndefined(multipleChoice) && multipleChoice !== null) {
            multiChoice.loadData(multipleChoice);
        } else {
            // load in empty data
            multiChoice.loadData(CourseSketch.prutil.MultipleChoice());
        }
        callback();
    }

    function loadCheckBox(specialQuestionData, callback) {
        var question = document.createElement('question-element');
        var multiChoice = document.createElement('multi-choice');
        problemPanel.appendChild(question);
        question.addAnswerContent(multiChoice);
        question.setFinishedListener(questionSaveListener);

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
     * @param bankProblem
     * @param callback
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

    this.stashData = function(callback) {
        saveToQuestionData(callback);
    };

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

    function saveSketch(callback) {
        var sketchSurface = problemPanel.querySelector('sketch-surface');
        var sketchArea = CourseSketch.prutil.SketchArea();
        sketchArea.recordedSketch = sketchSurface.getSrlUpdateListProto();
        specialQuestionData.sketchArea = sketchArea;
        callback();
    }

    function saveTyping(callback) {
        var freeResponse = CourseSketch.prutil.FreeResponse();
        freeResponse.startingText = problemPanel.querySelector('textarea').value;
        specialQuestionData.freeResponse = freeResponse;
        callback();
    }

    function saveMultipleChoice(callback) {
        var multipleChoiceElement = problemPanel.querySelector('multi-choice');
        multipleChoiceElement.setFinishedListener(function(command, evnt, update, multiChoice) {
            specialQuestionData.multipleChoice = multiChoice;
            callback();
        });
        multipleChoiceElement.saveData();
    }

    function saveCheckbox(callback) {
        var multipleChoiceElement = problemPanel.querySelector('multi-choice');
        multipleChoiceElement.setFinishedListener(function(command, evnt, update, multiChoice) {
            specialQuestionData.checkBox = multiChoice;
            callback();
        });
    }
}
CourseSketch.ProblemRenderer = ProblemRenderer;