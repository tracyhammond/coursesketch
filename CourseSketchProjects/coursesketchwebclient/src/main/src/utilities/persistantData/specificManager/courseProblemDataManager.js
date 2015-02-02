function CourseProblemDataManager(parent, advanceDataListener, parentDatabase, sendData, request, buffer) {
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var Request = request
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

	/**
	 * Gets a courseProblem from the local database.
	 *
	 * @param courseProblemId
	 *                ID of the courseProblem to get
	 * @param courseProblemCallback
	 *                function to be called after getting is complete, parameter
	 *                is the courseProblem object, can be called with {@link DatabaseException} if an exception occurred getting the data.
	 */
	function getCourseProblemLocal(courseProblemId, courseProblemCallback) {
		if (isUndefined(courseProblemId) || courseProblemId == null) {
			courseProblemCallback(new DatabaseException("The given id is not assigned", "getting CourseProblem: " + courseProblemId));
		}
		database.getFromCourseProblems(courseProblemId, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				courseProblemCallback(new DatabaseException("The result is undefined", "getting CouseProblem: " + courseProblemId));
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				courseProblemCallback(new DatabaseException("The database does not hold this value", "getting CourseProblem: " + courseProblemId));
			} else {
				// gets the data from the database and calls the callback
				var bytes = ByteBuffer.fromBase64(result.data);
				courseProblemCallback(CourseSketch.PROTOBUF_UTIL.getSrlProblemClass().decode(bytes));
			}
		});
	}

    /**
     * Sets a courseproblem in server database.
     *
     * @param courseproblem
     *                courseproblem object to set
     * @param courseproblemCallback
     *                function to be called after courseproblem setting is done
     */
    function insertCourseProblemServer(courseProblem, courseProblemCallback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM);
            var resultArray = item.getReturnText().split(":");
            var oldId = resultArray[1].trim();
            var newId = resultArray[0].trim();
            // we want to get the current course in the local database in case
            // it has changed while the server was processing.
            getCourseProblemLocal(oldId, function(courseProblem2) {
                deleteCourseProblem(oldId);
                if (!isUndefined(courseProblem2) && !(courseProblem2 instanceof DatabaseException)) {
                    courseProblem2.id = newId;
                    setCourseProblem(courseproblem2, function() {
                        courseproblemCallback(courseproblem2);
                    });
                } else {
                    courseproblem.id = newId;
                    setCourseproblem(courseproblem, function(e, request) {
                        courseproblemCallback(courseproblem);
                    });
                }
            });
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM, courseProblem.toArrayBuffer());
    }

    /**
     * Adds a new courseProblem to both local and server databases. Also updates the
     * corresponding course given by the courseProblem's courseId.
     *
     * @param courseProblem
     *                courseProblem object to insert
     * @param localCallback
     *                function to be called after local insert is done
     * @param serverCallback
     *                function to be called after server insert is done
     */
    function insertCourseProblem(courseProblem, localCallback, serverCallback) {
        if (isUndefined(courseProblem.id) || courseProblem.id == null) {
            courseProblem.id = generateUUID();
        }

        // used locally in two places
        function insertingCourseProblem() {
        	setCourseproblem(courseProblem, function(e, request) {
				console.log("inserted locally :" + courseProblem.id)
				if (!isUndefined(localCallback)) {
					localCallback(courseProblem);
				}
				insertCourseProblemServer(courseProblem, function(courseProblemUpdated) {
					parent.getCourse(courseProblem.courseId, function(course) {
						var courseProblemList = course.courseProblemList;

						// remove old Id (if it exists)
						if (courseProblemList.indexOf(courseProblem.id) >= 0) {
							removeObjectFromArray(courseProblemList, courseProblem.id);
						}
						courseProblemList.push(courseProblemUpdated.id);
						parent.setCourse(course, function() {
							if (!isUndefined(serverCallback)) {
								serverCallback(courseProblemUpdated);
							}
						});
						// Course is set with its new courseProblem
					});
					// Finished with the course
				});
				// Finished with setting courseProblem
			});
        } // insertingCourseProblem

		// Inserts the bank problem first!
        if ((isUndefined(courseProblem.problemBankId) || courseProblem.problemBankId == null)
        	&& (!isUndefined(courseProblem.problemInfo) && courseProblem.problemInfo != null)) {
        	insertBankProblemServer(courseProblem.problemInfo, function(updateId) {
				courseProblem.problemBankId = updateId;
				insertingCourseProblem();
        	});
        } else {
        	insertingCourseProblem();
        }

        // Finished with local courseProblem
    }
    parent.insertCourseproblem = insertCourseproblem;

	/**
	 * Returns a list of all of the course problems from the local and server database for the given list
	 * of Ids.
	 *
	 * This does attempt to pull course problems from the server!
	 *
	 * @param CourseProblemIdList
	 *            list of IDs of the courseproblems to get
	 * @param courseProblemCallbackPartial
	 *            {Function} called when course problems are grabbed from the local
	 *            database only. This list may not be complete. This may also
	 *            not get called if there are no local course problems.
	 * @param courseProblemCallbackComplete
	 *            {Function} called when the complete list of course problems are
	 *            grabbed.
	 * TODO: implement the partial and complete system?
	 */
	function getCourseProblems(CourseProblemIdList, courseProblemCallback) {
		/*
		 * So what happens here might be a bit confusing to some new people so let me explain it.
		 * #1 there is a loop that goes through every item in the CourseProblemIdList (which is a list of courseProblem ids)
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

		// standard preventative checking
		if (isUndefined(CourseProblemIdList) || CourseProblemIdList == null || CourseProblemIdList.length == 0) {
			courseProblemCallback(new DatabaseException("The given list is not assigned", "getting Course Problem: " + CourseProblemIdList));
		}

		var barrier = CourseProblemIdList.length;
		var courseProblemList = [];
		var leftOverId = [];

		// create local courseProblem list so everything appears really fast!
		for (var i = 0; i < CourseProblemIdList.length; i++) {
			var courseProblemIdLoop = CourseProblemIdList[i];
			// the purpose of this function is purely to scope the courseProblemId so that it changes
			function loopContainer(courseProblemId) {
				getCourseProblemLocal(courseProblemId, function(courseProblem) {
					if (!isUndefined(courseProblem) && !(courseProblem instanceof DatabaseException)) {
						courseProblemList.push(courseProblem);
					} else {
						leftOverId.push(courseProblemId);
					}
					barrier -= 1;
					if (barrier == 0) {
						// after the entire list has been gone through pull the leftovers from the server
						if (leftOverId.length >= 1) {
							advanceDataListener.setListener(Request.MessageType.DATA_REQUEST,
									CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM, function(evt, item) {
								advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST,
										CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM);

								// after listener is removed
								if (isUndefined(item.data) || item.data == null) {
									courseProblemCallback(new DatabaseException("The data sent back from the server does not exist."));
									return;
								}
								var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
								var courseProblem = school.problems[0];
								if (isUndefined(courseProblem) || courseProblem instanceof DatabaseException) {
									var result = courseProblem;
									if (isUndefined(result)) {
										result = new DatabaseException("Nothing is in the server database!",
											"Grabbing courseProblem from server: " + leftOverId);
									}
									if (!isUndefined(courseProblemCallback)) {
										courseProblemCallback(result);
									}
									return;
								} // undefined course problem
								for (var i = 0; i < school.problems.length; i++) {
									localScope.setCourseProblem(school.problems[i]);
									courseProblemList.push(school.problems[i]);
								}
								courseProblemCallback(courseProblemList);
							});
							// creates a request that is then sent to the server
							sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.COURSE_PROBLEM, leftOverId);
						}

						// this calls actually before the response from the server is received!
						if (courseProblemList.length > 0) {
							courseProblemCallback(courseProblemList);
						}
					} // end of if(barrier == 0)
				}); // end of getting local courseProblem
			} // end of loopContainer
			loopContainer(courseProblemIdLoop);
		} // end of loop
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
