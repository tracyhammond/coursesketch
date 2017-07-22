/**
 * A manager for assignments that talks with the remote server.
 *
 * @param {SchoolDataManager} parent - The coursesketch.util.util that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener - A listener for the coursesketch.util.util.
 * @param {ProtoDatabase} parentDatabase - The local coursesketch.util.util
 * @param {ByteBuffer} ByteBuffer - Used in the case of longs for javascript.
 * @constructor
 */
function SubmissionDataManager(parent, advanceDataListener, parentDatabase, ByteBuffer) {
    var database = parentDatabase;
    var parentScope = parent;

    /**
     * Returns a submission.  But right now only treats it as an exerpiment.
     * TODO: have it be able to get solutions as well.
     *
     * This does attempt to pull experiment from the server!
     *
     * @param {List<String>} submissionIdentifier - The identifier for the submission
     * @param {Function} submissionCallback - called when experiment is grabbed from the coursesketch.util.util.
     *              This is only called once.  Either it exists in the local coursesketch.util.util or it is grabbed from the server coursesketch.util.util.
     * @param {Boolean} [isSolution] - True if it is a solution otherwise it is a problem
     */
    function getSubmission(submissionIdentifier, submissionCallback, isSolution) {
        var problemId = submissionIdentifier.join('') + (isSolution ? 's' : 'e');
        var decodingClass = isSolution ? CourseSketch.prutil.getSrlSolutionClass() :
            CourseSketch.prutil.getSrlExperimentClass();
        database.getFromSubmissions(problemId, function(e, request, result) {
            // TODO: change it so the util can pull locally as well.
            if (isUndefined(result) || isUndefined(result.data) || true) {
                var queryType = isSolution ? CourseSketch.prutil.ItemQuery.SOLUTION : CourseSketch.prutil.ItemQuery.EXPERIMENT;
                var itemRequest = CourseSketch.prutil.createItemRequest(queryType, submissionIdentifier);
                // the listener from the server of the request
                // it stores the course locally then cals the callback with the course
                advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
                    if (isException(item)) {
                        submissionCallback(new DatabaseException('exception thrown while waiting for server response.'), item);
                        return;
                    }
                    if (!parent.isItemValid(item)) {
                        submissionCallback(new DatabaseException(
                            item.returnText ? item.returnText : 'The data sent back from the server does not exist.'));
                        return;
                    }
                    var submissionWrapper = decodingClass.decode(item.data[0]);
                    parentScope.setSubmission(problemId, submissionWrapper);
                    submissionCallback(submissionWrapper);
                    submissionWrapper = undefined;
                });
            } else {
                // gets the data from the util and calls the callback
                var bytes = ByteBuffer.fromBase64(result.data);
                submissionCallback(decodingClass.decode(bytes));
                bytes = null;
            }
        });
    }

    /**
     * Returns an Experiment
     *
     * This does attempt to pull experiment from the server!
     *
     * @param {List<String>} submissionIdentifier - The identifier for the submission
     * @param {Function} submissionCallback - called when experiment is grabbed from the coursesketch.util.util.
     *              This is only called once.  Either it exists in the local coursesketch.util.util or it is grabbed from the server coursesketch.util.util.
     */
    function getExperiment(submissionIdentifier, submissionCallback) {
        getSubmission(submissionIdentifier, function(experiment) {
            if (!isUndefined(experiment.submission)) {
                submissionCallback(experiment.submission);
            } else {
                submissionCallback(experiment);
            }
        }, false);
    }

    parent.getExperiment = getExperiment;

    /**
     * Returns an Experiment
     *
     * This does attempt to pull experiment from the server!
     *
     * @param {List<String>} submissionIdentifier - The identifier for the submission
     * @param {Function} submissionCallback - called when experiment is grabbed from the coursesketch.util.util.
     *              This is only called once.  Either it exists in the local coursesketch.util.util or it is grabbed from the server coursesketch.util.util.
     */
    function getSolution(submissionIdentifier, submissionCallback) {
        getSubmission(submissionIdentifier, submissionCallback, true);
    }

    parent.getSolution = getSolution;

    /**
     * Attempts to get all experiments from the specific problem id.
     *
     * @param {List<String>} submissionIdentifier - The problem we are currently looking at.
     * @param {Function} submissionCallback - called after the server responds with all experiments.
     */
    function getAllExperiments(submissionIdentifier, submissionCallback) {

        // creates a request that is then sent to the server
        var advanceQuery = CourseSketch.prutil.ExperimentReview();
        advanceQuery.allowEditing = true;
        advanceQuery.showUserNames = false;
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.EXPERIMENT, submissionIdentifier, advanceQuery);

        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
            if (isException(item)) {
                submissionCallback(new DatabaseException('exception thrown while waiting for server response.'), item);
                return;
            }
            console.log('SERVER RESPONDED WITH EXPERIMENT');
            if (!parent.isItemValid(item)) {
                submissionCallback(new DatabaseException(
                    item.returnText ? item.returnText : 'The data sent back from the server does not exist.'));
                return;
            }
            var list = [];
            try {
                for (var i = 0; i < item.data.length; i++) {
                    list.push(CourseSketch.prutil.getSrlExperimentClass().decode(item.data[i]));
                }
            } catch (exception) {
                console.log(exception);
                submissionCallback(new DatabaseException('Exception decoding experiment data data: ' + exception.toString()));
                return;
            }
            submissionCallback(list);
            list = undefined;
            // The times parameter is 2 because it is called ones with NO_OP and once with the actual submission.
        });

    }

    parent.getAllExperiments = getAllExperiments;

    /**
     * @param {String} problemId - the id to which this submission is being added.
     * @param {SrlSubmission} submission - the submission that is being added.
     * @param {Function} [submissionCallback] - called when the submission is saved.
     */
    function setSubmission(problemId, submission, submissionCallback) {
        database.putInSubmissions(problemId, submission.toBase64(), function(e, request) {
            if (submissionCallback) {
                submissionCallback(e, request);
            }
        });
    }

    parent.setSubmission = setSubmission;

    /**
     * Deletes a submissions from local util.
     * This does not delete the id pointing to this item in the respective course.
     *
     * @param {String} problemId
     *                ID of the submissions to delete
     * @param {Function} submissionsCallback
     *                function to be called after the deletion is done
     */
    function deleteSubmission(problemId, submissionsCallback) {
        database.deleteFromSubmissions(problemId, function(e, request) {
            if (submissionsCallback) {
                submissionsCallback(e, request);
            }
        });
    }

    parent.deleteSubmission = deleteSubmission;
}
