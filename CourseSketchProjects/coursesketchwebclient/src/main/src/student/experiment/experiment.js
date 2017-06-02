validateFirstRun(document.currentScript);

(function() {
    CourseSketch.studentExperiment.waitScreenManager = new WaitScreenManager();
    $(document).ready(function() {
        CourseSketch.dataManager.waitForDatabase(function() {
            var panel = document.querySelector('navigation-panel');
            var navigator = panel.getNavigator();
            var assignment = CourseSketch.dataManager.getState('currentAssignment');
            if (!isUndefined(assignment)) {
                navigator.setAssignmentId(assignment);
            }
            var problemIndex = CourseSketch.dataManager.getState('currentProblemIndex');
            if (!isUndefined(problemIndex)) {
                navigator.setPreferredIndex(parseInt(problemIndex, 10));
            }
            CourseSketch.dataManager.clearStates();

            if (isUndefined(panel.dataset.callbackset)) {
                panel.dataset.callbackset = '';
                navigator.addCallback(loadProblem);
                navigator.reloadProblems();
            }
        });
    });

    /**
     * Loads the problem, called every time a user navigates to a different problem.
     *
     * @param {Navigator} navigator - The assignment navigator.
     */
    function loadProblem(navigator) {
        var problemType = navigator.getProblemType();
        // todo: better way of removing elements
        var parentPanel = document.getElementById('problemPanel');
        console.log(parentPanel);
        parentPanel.emptyPanel();
        if (problemType === CourseSketch.prutil.QuestionType.SKETCH) {
            console.log('Loading sketch problem');
            loadSketch(navigator);
        } else if (problemType === CourseSketch.prutil.QuestionType.FREE_RESP) {
            console.log('Loading typing problem');
            loadTyping(navigator);
        }

        var questionText = document.querySelector('problem-text-panel');
        questionText.setProblemText(navigator.getProblemText());
        console.log('a problem has been loaded with question text', navigator.getProblemText());

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
     * @param {Navigator} navigator - The assignment navigator.
     */
    function loadTyping(navigator) {
        var typingSurface = document.createElement('textarea');
        typingSurface.className = 'sub-panel card-panel';
        typingSurface.contentEditable = true;
        CourseSketch.studentExperiment.addWaitOverlay();
        document.getElementById('problemPanel').appendChild(typingSurface);
        CourseSketch.dataManager.getSubmission(navigator.getCurrentProblemId(), function(submission) {
            if (isException(submission)) {
                CourseSketch.clientException(submission);
            }
            if (isUndefined(submission) || CourseSketch.isException(submission) || isUndefined(submission.getTextAnswer())) {
                CourseSketch.studentExperiment.removeWaitOverlay();
                return;
            }
            typingSurface.value = submission.getTextAnswer();
            CourseSketch.studentExperiment.removeWaitOverlay();
            typingSurface = undefined;
            console.log(performance.memory);
        });
    }

    /**
     * Loads the update list on to a sketch surface and prevents editing until it is completely loaded.
     *
     * @param {Navigator} navigator - The assignment navigator.
     */
    function loadSketch(navigator) {
        var sketchSurface = document.createElement('sketch-surface');
        sketchSurface.className = 'sub-panel submittable';
        sketchSurface.style.width = '100%';
        sketchSurface.style.height = '100%';
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
         * Called when the sketch surface is done loading to remove the overlay.
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

        CourseSketch.dataManager.getSubmission(navigator.getCurrentProblemId(), function(submission) {
            var problemScript = navigator.getProblemScript();
            if (CourseSketch.isException(submission)) {
                CourseSketch.clientException(submission);
            }
            if (isUndefined(submission) || CourseSketch.isException(submission) || isUndefined(submission.getUpdateList())) {
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
                var updateList = submission.getUpdateList();
                sketchSurface.loadUpdateList(updateList.getList(), element);
                updateList = null;
                element = null;
            });
        });
        //end of getSubmission
    }
})();
