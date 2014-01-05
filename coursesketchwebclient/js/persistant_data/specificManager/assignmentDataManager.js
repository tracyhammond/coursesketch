function AssignmentDataManager(parent, advanceDataListener, parentDatabase, sendData, builders, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var sendDataRequest = sendData;
	var Request = builders[0];
	var QueryBuilder = builders[1];
	var SchoolBuilder = builders[2];
	var localScope = parent;
	var ByteBuffer = buffer;

	function setAssignment(assignment, assignmentCallback) {
		database.putInAssignments(assignment.id, assignment.toBase64(), function(e, request) {
			if (assignmentCallback) {
				assignmentCallback(e, request);
			}
		});
	};
	parent.setAssignment = setAssignment;

	function deleteAssignment(assignmentId, couresCallback) {
		database.deleteFromAssignments(assignmentId, function(e, request) {
			if (assignmentCallback) {
				assignmentCallback(e, request);
			}
		});
	};
	parent.deleteAssignment = deleteAssignment;

	function getAssignmentLocal(assignmentId, assignmentCallback) {
		database.getFromAssignments(assignmentId, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				assignmentCallback(undefined);
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				assignmentCallback(nonExistantValue);
			} else {
				// gets the data from the database and calls the callback
				var bytes = ByteBuffer.decode64(result.data);
				assignmentCallback(SrlAssignment.decode(bytes));
			}
		});
	}

	/**
	 * Returns a list of all of the assignments in database for the given list of Id
	 *
	 * This does attempt to pull assignments from the server!
	 */
	function getAssignments(userAssignmentId, assignmentCallback) {
		/*
		 * So what happens here might be a bit confusing to some new people so let me explain it.
		 * #1 there is a loop that goes through every item in the userAssignmentId (which is a list of assignment ids)
		 * 
		 * #2 there is a function declaration inside the loop the reason for this is so that the assignmentId is not overwritten
		 * when the callback is called.
		 * 
		 * #3 we call getAssignmentLocal which then calls a callback about if it got an assignment or not if it didnt we add the id to a
		 * list of Id we need to get from the server
		 * 
		 * #4 after the entire list has been gone through (which terminates in the callback with barrier = 0)
		 * if there are any that need to be pulled from the server then that happens
		 * 
		 * #5 after talking to the server we get a response with a list of assignments, these are combined with the local assignments then the orignal callback is called.
		 * 
		 * #6 the function pattern terminates.
		 */
		var barrier = userAssignmentId.length;
		var assignmentList = [];
		var leftOverId = [];

		// create local assignment list so everything appears really fast!
		for (var i = 0; i < userAssignmentId.length; i++) {
			var assignmentIdLoop = userAssignmentId[i];
			// the purpose of this function is purely to scope the assignmentId so that it changes
			function loopContainer(assignmentId) {
				getAssignmentLocal(assignmentId, function(assignment) {
					if (!isUndefined(assignment)) { 
						assignmentList.push(assignment);
					} else {
						leftOverId.push(assignmentId);
						console.log(leftOverId);
					}	
					barrier -= 1;
					if (barrier == 0) {

						// after the entire list has been gone through pull the leftovers from the server
						if (leftOverId.length > 1) {
							advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.ASSIGNMENT, function(evt, item) {
								var school = SchoolBuilder.SrlSchool.decode(item.data);
								var assignment = school.assignments[0];
								if (isUndefined(assignment)) {
									assignmentCallback(nonExistantValue);
									return;
								}
								for(var i = 0; i < school.assignments.length; i++) {
									localScope.setAssignment(school.assignments[i]);
									assignmentList.push(school.assignments[i]);
								}
								assignmentCallback(assignmentList);
							});
							// creates a request that is then sent to the server
							alert(leftOverId);
							console.log(leftOverId);
							sendDataRequest(QueryBuilder.ItemQuery.ASSIGNMENT, leftOverId);
						}

						// this calls actually before the response from the server is received!
						if (assignmentList.length > 0) {
							console.log("local list complete");
							console.log(assignmentList);
							assignmentCallback(assignmentList);
						}
					}// end of if(barrier == 0)
				});// end of getting local assignment
			} // end of loopContainer
			loopContainer(assignmentIdLoop);
		}// end of loop
	};
	parent.getAssignments = getAssignments;
	
	/**
	 * Returns a assignment with the given couresId will ask the server if it does not exist locally
	 *
	 * If the server is pulled and the assignment still does not exist the Id is set with nonExistantValue
	 * and the database is never polled for this item for the life of the program again.
	 *
	 * @param assignmentId The id of the assignment we want to find.
	 * @param assignmentCallback The method to call when the assignment has been found. (this is asynchronous)
	 */
	function getAssignment(assignmentId, assignmentCallback) {
		getAssignments([assignmentId], function(assignmentList) {
			assignmentCallback(assignmentList[0]);
		});
	};
	parent.getAssignment = getAssignment;
}