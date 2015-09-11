/**
 * A manager for assignments that talks with the remote server.
 *
 * @param {CourseSketchDatabase} parent The database that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener A listener for the database.
 * @param {IndexedDB} parentDatabase The local database
 * @param {Function} sendData A function that makes sending data much easier
 * @param {SrlRequest} Request A shortcut to a request
 * @param {ByteBuffer} ByteBuffer Used in the case of longs for javascript.
 * @constructor
 */
function SubmissionDataManager(parent, advanceDataListener, parentDatabase, sendData, Request, ByteBuffer) {
    var database = parentDatabase;
    var localScope = parent;

    /**
     * Returns a submission.  but right now only treats it as an exerpiment.
     * TODO: have it be able to get solutions as well.
     * of Ids.
     *
     * This does attempt to pull experiment from the server!
     *
     * @param {String} problemId
     *            submission
     * @param {Function} submissionCallback
     *            called when experiment is grabbed from the database.
     *              This is only called once.  Either it exists in the local database or it is grabbed from the server database.
     */
    function getSubmission(problemId, submissionCallback) {
        database.getFromSubmissions(problemId, function(e, request, result) {
            // TODO: change it so the database can pull locally as well.
            if (isUndefined(result) || isUndefined(result.data) || true) {
                var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.EXPERIMENT, [ problemId ]);
                // the listener from the server of the request
                // it stores the course locally then cals the callback with the course
                advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
                    if (item.query === CourseSketch.prutil.ItemQuery.NO_OP) {
                        return;
                    }
                    if (isException(item)) {
                        submissionCallback(new DatabaseException('exception thrown while waiting for server response.'), item);
                        return;
                    }
                    if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                        submissionCallback(new DatabaseException(
                                item.returnText? item.returnText : 'The data sent back from the server does not exist.'));
                        return;
                    }
                    var experiment = CourseSketch.prutil.getSrlExperimentClass().decode(item.data[0]);
                    var sub = experiment.submission;
                    localScope.setSubmission(problemId, sub);
                    submissionCallback(sub);
                    sub = undefined;
                    experiment = undefined;
                    // The times parameter is 2 because it is called ones with NO_OP and once with the actual submission.
                }, undefined, 2);

            } else {
                // gets the data from the database and calls the callback
                var bytes = ByteBuffer.fromBase64(result.data);
                submissionCallback(CourseSketch.prutil.getSrlSubmissionClass().decode(bytes));
                bytes = null;
            }
        });
    }
    parent.getSubmission = getSubmission;

    /**
     * Attempts to get all experiments from the specific problem id.
     * @param {String} problemId the problem we are currently looking at.
     * @param {Function} submissionCallback called after the server responds with all experiments.
     */
    function getAllExperiments(problemId, submissionCallback) {

        // creates a request that is then sent to the server
        var advanceQuery = CourseSketch.prutil.ExperimentReview();
        advanceQuery.allowEditing = true;
        advanceQuery.showUserNames = false;
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.EXPERIMENT, [ problemId ], advanceQuery);

        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
            if (item.query === CourseSketch.prutil.ItemQuery.NO_OP) {
                return;
            }
            if (isException(item)) {
                submissionCallback(new DatabaseException('exception thrown while waiting for server response.'), item);
                return;
            }
            console.log('SERVER RESPONDED WITH EXPERIMENT');
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                submissionCallback(new DatabaseException('The data sent back from the server does not exist.'));
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
        }, undefined, 2);

    }
    parent.getAllExperiments = getAllExperiments;

    /**
     * @param {String} problemId the id to which this submission is being added.
     * @param {SrlSubmission} submission the submission that is being added.
     * @param {Function} submissionCallback called when the submission is saved.
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
     * Deletes a submissions from local database.
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
