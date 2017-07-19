validateFirstRun(document.currentScript);

(function() {
    CourseSketch.studentExperiment.waitScreenManager = new WaitScreenManager();
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector('navigation-panel');
            var navigator = panel.getNavigator();
            var assignmentId = CourseSketch.dataManager.getState('currentAssignment');
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            var addCallback = isUndefined(panel.dataset.callbackset);

            CourseSketch.dataManager.clearStates();

            if (addCallback) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
            }

            if (!isUndefined(assignmentId)) {
                navigator.resetNavigation(assignmentId, parseInt(problemIndex, 10));
            } else if (addCallback) {
                navigator.refresh();
            }
        });
    });

    /**
     * Loads the problem, called every time a user navigates to a different problem.
     *
     * @param {AssignmentNavigator} navigator - The navigator used to navigate assignments.
     */
    function loadProblem(navigator) {
        var bankProblem = navigator.getCurrentInfo();
        var problemType = bankProblem.getQuestionType();
        // todo: better way of removing elements
        var parentPanel = document.getElementById('problemPanel');
        console.log(parentPanel);
        var oldElement = parentPanel.querySelector('.sub-panel');
        if (oldElement instanceof Node) {
            parentPanel.removeChild(oldElement);
        }
        if (problemType === CourseSketch.prutil.QuestionType.SKETCH) {
            console.log('Loading sketch problem');
            loadSketch(navigator);
        } else if (problemType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            console.log('Loading typing problem');
            loadTyping(navigator);
        }

        parentPanel.problemIndex = navigator.getCurrentNumber();
        parentPanel.setProblemType(problemType);
        parentPanel.refreshPanel();
        parentPanel.isStudent = true;
        parentPanel.isGrader = false;

        parentPanel.setWrapperFunction(function(submission) {
            var studentExperiment = CourseSketch.prutil.SrlExperiment();
            navigator.setSubmissionInformation(studentExperiment, true);
            console.log('student experiment data set', studentExperiment);
            studentExperiment.submission = submission;
            return studentExperiment;
        });
    }

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    CourseSketch.studentExperiment.addWaitOverlay = function() {
        CourseSketch.studentExperiment.waitScreenManager.buildOverlay(document.querySelector('body'));
        CourseSketch.studentExperiment.waitScreenManager.buildWaitIcon(document.getElementById('overlay'));
        document.getElementById('overlay').querySelector('.waitingIcon').classList.add('centered');
    };

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    CourseSketch.studentExperiment.removeWaitOverlay = function() {
        if (!isUndefined(document.getElementById('overlay')) && document.getElementById('overlay') !== null) {
            document.querySelector('body').removeChild(document.getElementById('overlay'));
        }
    };

    /**
     * Loads the typing from the submission.
     *
     * @param {AssignmentNavigator} navigator - The navigator used to navigate assignments.
     */
    function loadTyping(navigator) {
        var typingSurface = document.createElement('textarea');
        typingSurface.className = 'sub-panel';
        typingSurface.style.width = '100%';
        typingSurface.style.height = 'calc(100% - 110px)';
        typingSurface.contentEditable = true;
        CourseSketch.studentExperiment.addWaitOverlay();
        document.getElementById('problemPanel').appendChild(typingSurface);
        CourseSketch.dataManager.getSubmission(navigator.getSubmissionIdentifier(), function(submission) {
            if (isUndefined(submission) || submission instanceof CourseSketch.DatabaseException || isUndefined(submission.getTextAnswer())) {
                CourseSketch.studentExperiment.removeWaitOverlay();
                return;
            }
            typingSurface.value = submission.getTextAnswer();
            CourseSketch.studentExperiment.removeWaitOverlay();
            typingSurface = undefined;
            console.log(performance.memory);
        });
    }


    var saveScript = document.querySelector('button.save');

    /**
     * Saves script in order to display it on the problem when opened.
     *
     * Saves to problem navigator.
     */
    saveScript.onclick = function() {
        //Create bank problem (proto object)
        var bankProblem = CourseSketch.prutil.SrlBankProblem();

        //Get problem ID
        bankProblem.id = navigator.getCurrentProblemId();
        console.log(bankProblem.id);

        //Get sketch surface
        bankProblem.baseSketch = document.querySelector('.submittable').getUpdateList();

        //Set script and update list
        bankProblem.script = document.getElementById('scriptBox').value;

        //Update bank problem
        CourseSketch.dataManager.updateBankProblem(bankProblem);
    };

    var exitButton = document.querySelector('button.exit');

    /**
     * Called to leave the script editor.
     *
     * TODO: implement exiting the script editor.
     */
    exitButton.onclick = function() {
        alert('Not yet implemented!');
    };

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {AssignmentNavigator} navigator - The navigator used to navigate assignments.
     */
    function loadSketch(navigator) {
        var sketchSurface = document.createElement('sketch-surface');
        sketchSurface.className = 'wide_rule sub-panel submittable';
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = 'calc(100% - 110px)';
        sketchSurface.setErrorListener(function(exception) {
            console.log(exception);
            alert(exception);
        });
        var element = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
        CourseSketch.studentExperiment.addWaitOverlay();
        document.getElementById('percentBar').appendChild(element);
        element.startWaiting();
        var realWaiting = element.finishWaiting.bind(element);
        /**
         * Called when the sketch is done loading.
         */
        element.finishWaiting = function() {
            realWaiting();
            sketchSurface.refreshSketch();
            CourseSketch.studentExperiment.removeWaitOverlay();
            sketchSurface = undefined;
            element = undefined;
        };

        // adding here because of issues
        document.getElementById('problemPanel').appendChild(sketchSurface);
        var problem = navigator.getCurrentInfo();

        CourseSketch.dataManager.getSubmission(navigator.getGroupId(), function(submission) {
            var problemScript = problem.getScript();
            if (isUndefined(submission) || submission instanceof CourseSketch.DatabaseException || isUndefined(submission.getUpdateList())) {
                executeScript(problemScript, document.getElementById('problemPanel'), function() {
                    console.log('script executed - worker disconnect');
                    if (element.isRunning()) {
                        element.finishWaiting();
                        CourseSketch.studentExperiment.removeWaitOverlay();
                    }
                });
                return;
            }

            // tell the surface not to create its own sketch.
            sketchSurface.dataset.existinglist = '';

            // add after attributes are set.

            sketchSurface.refreshSketch();

            //loads and runs the script
            executeScript(problemScript, document.getElementById('problemPanel'), function() {
                console.log('script executed - worker disconnect');
                console.log(submission);
                var updateList = submission.getUpdateList();
                //console.log(updateList);
                sketchSurface.loadUpdateList(updateList.getList(), element);
                updateList = null;
                element = null;
                //console.log(submission);
            });
        });
        //end of getSubmission
    }
})();
