function SubmissionDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var userCourses = {};
	var userCourseId = [];
	var userHasCourses = true;
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request;
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	 * Returns a submission.  but right now only treats it as an exerpiment.
	 * TODO: have it be able to get solutions as well.
	 * of Ids.
	 *
	 * This does attempt to pull experiment from the server!
	 *
	 * @param problemId
	 *            submission
	 * @param submissionCallback
	 *            {Function} called when experiment is grabbed from the database.
	 *			  This is only called once.  Either it exists in the local database or it is grabbed from the server database.
	 */
	function getSubmission(problemId, submissionCallback) {
		database.getFromSubmissions(problemId, function(e, request, result) {
			// TODO: change it so the database can pull locally as well.
			if (isUndefined(result) || isUndefined(result.data) || true) {

				// the listener from the server of the request
				// it stores the course locally then cals the callback with the course
				advanceDataListener.setListener(Request.MessageType.DATA_REQUEST,
						CourseSketch.PROTOBUF_UTIL.ItemQuery.EXPERIMENT, function(evt, item) {
					advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.EXPERIMENT);
					if (isUndefined(item.data) || item.data == null) {
						submissionCallback(new DatabaseException("The data sent back from the server does not exist."));
						return;
					}
					var experiment = CourseSketch.PROTOBUF_UTIL.getSrlExperimentClass().decode(item.data);
					var sub = experiment.submission;
					localScope.setSubmission(problemId, sub);
					submissionCallback(sub);
					sub = undefined;
					experiment = undefined;

				});
				// creates a request that is then sent to the server
				sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.EXPERIMENT, [problemId]);
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				submissionCallback(nonExistantValue);
			} else {
				// gets the data from the database and calls the callback
				var bytes = ByteBuffer.fromBase64(result.data);
				submissionCallback(CourseSketch.PROTOBUF_UTIL.getSrlSubmissionClass().decode(bytes));
				bytes = null;
			}
		});
	};
	parent.getSubmission = getSubmission;

	function getAllExperiments(problemId, submissionCallback) {
		advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.EXPERIMENT, function(evt, item) {
			if (isUndefined(item.data)) {
				submissionCallback("Undefined");
				return;
			}
			var list;
			try {
				list = CourseSketch.PROTOBUF_UTIL.getSrlExperimentListClass().decode(item.data);
			} catch(exception) {
				return;
			}
			console.log(list.experiments);
			submissionCallback(list.experiments);
			list = null;
		});

		// creates a request that is then sent to the server
		var advanceQuery = CourseSketch.PROTOBUF_UTIL.ExperimentReview();
		advanceQuery.allowEditing = true;
		advanceQuery.showUserNames = false;
		sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.EXPERIMENT, [problemId], advanceQuery);
	}
	parent.getAllExperiments = getAllExperiments;

	/**
	 * @param submission the submission that is being added
	 * @param id the id to which this submission is being added.
	 */
	function setSubmission(problemId, submission, submissionCallback) {
		database.putInSubmissions(problemId, submission.toBase64(), function(e, request) {
			if (submissionCallback) {
				submissionCallback(e, request);
			}
		});
	};
	parent.setSubmission = setSubmission;

	function deleteSubmission(problemId, couresCallback) {
		database.deleteFromSubmissions(problemId, function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
	};
	parent.deleteSubmission = deleteSubmission;
}
