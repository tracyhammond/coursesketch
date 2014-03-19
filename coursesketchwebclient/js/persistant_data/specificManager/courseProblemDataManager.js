function CourseProblemDataManager(parent, advanceDataListener, parentDatabase, sendData, builders, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = builders[0];
	var QueryBuilder = builders[1];
	var SchoolBuilder = builders[2];
	var localScope = parent;
	var ByteBuffer = buffer;

	function setCourseProblem(courseProblem, courseProblemCallback) {
		database.putInCourseProblems(courseProblem.id, courseProblem.toBase64(), function(e, request) {
			if (courseProblemCallback) {
				courseProblemCallback(e, request);
			}
		});
	};
	parent.setCourseProblem = setCourseProblem;

	function deleteCourseProblem(courseProblemId, couresCallback) {
		database.deleteFromCourseProblems(courseProblemId, function(e, request) {
			if (courseProblemCallback) {
				courseProblemCallback(e, request);
			}
		});
	};
	parent.deleteCourseProblem = deleteCourseProblem;

	function getCourseProblemLocal(courseProblemId, courseProblemCallback) {
		database.getFromCourseProblems(courseProblemId, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				courseProblemCallback(undefined);
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				courseProblemCallback(nonExistantValue);
			} else {
				// gets the data from the database and calls the callback
				var bytes = ByteBuffer.decode64(result.data);
				courseProblemCallback(SrlProblem.decode(bytes));
			}
		});
	}

	/**
	 * Returns a list of all of the courseProblems in database for the given list of Id
	 *
	 * This does attempt to pull courseProblems from the server!
	 */
	function getCourseProblems(userCourseProblemId, courseProblemCallback) {
		/*
		 * So what happens here might be a bit confusing to some new people so let me explain it.
		 * #1 there is a loop that goes through every item in the userCourseProblemId (which is a list of courseProblem ids)
		 * 
		 * #2 there is a function declaration inside the loop the reason for this is so that the courseProblemId is not overwritten
		 * when the callback is called.
		 * 
		 * #3 we call getCourseProblemLocal which then calls a callback about if it got an courseProblem or not if it didnt we add the id to a
		 * list of Id we need to get from the server
		 * 
		 * #4 after the entire list has been gone through (which terminates in the callback with barrier = 0)
		 * if there are any that need to be pulled from the server then that happens
		 * 
		 * #5 after talking to the server we get a response with a list of courseProblems, these are combined with the local courseProblems then the orignal callback is called.
		 * 
		 * #6 the function pattern terminates.
		 */
		var barrier = userCourseProblemId.length;
		var courseProblemList = [];
		var leftOverId = [];

		// create local courseProblem list so everything appears really fast!
		for (var i = 0; i < userCourseProblemId.length; i++) {
			var courseProblemIdLoop = userCourseProblemId[i];
			// the purpose of this function is purely to scope the courseProblemId so that it changes
			function loopContainer(courseProblemId) {
				getCourseProblemLocal(courseProblemId, function(courseProblem) {
					if (!isUndefined(courseProblem)) { 
						courseProblemList.push(courseProblem);
					} else {
						leftOverId.push(courseProblemId);
					}	
					barrier -= 1;
					if (barrier == 0) {

						// after the entire list has been gone through pull the leftovers from the server
						if (leftOverId.length >= 1) {
							advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE_PROBLEM, function(evt, item) {
								var school = SchoolBuilder.SrlSchool.decode(item.data);
								var courseProblem = school.problems[0];
								if (isUndefined(courseProblem)) {
									courseProblemCallback(nonExistantValue);
									return;
								}
								for(var i = 0; i < school.problems.length; i++) {
									localScope.setCourseProblem(school.problems[i]);
									courseProblemList.push(school.problems[i]);
								}
								courseProblemCallback(courseProblemList);
							});
							// creates a request that is then sent to the server
							sendData.sendDataRequest(QueryBuilder.ItemQuery.COURSE_PROBLEM, leftOverId);
						}

						// this calls actually before the response from the server is received!
						if (courseProblemList.length > 0) {
							courseProblemCallback(courseProblemList);
						}
					}// end of if(barrier == 0)
				});// end of getting local courseProblem
			} // end of loopContainer
			loopContainer(courseProblemIdLoop);
		}// end of loop
	};
	parent.getCourseProblems = getCourseProblems;
	
	/**
	 * Returns a courseProblem with the given couresId will ask the server if it does not exist locally
	 *
	 * If the server is pulled and the courseProblem still does not exist the Id is set with nonExistantValue
	 * and the database is never polled for this item for the life of the program again.
	 *
	 * @param courseProblemId The id of the courseProblem we want to find.
	 * @param courseProblemCallback The method to call when the courseProblem has been found. (this is asynchronous)
	 */
	function getCourseProblem(courseProblemId, courseProblemCallback) {
		getCourseProblems([courseProblemId], function(courseProblemList) {
			courseProblemCallback(courseProblemList[0]);
		});
	};
	parent.getCourseProblem = getCourseProblem;
}