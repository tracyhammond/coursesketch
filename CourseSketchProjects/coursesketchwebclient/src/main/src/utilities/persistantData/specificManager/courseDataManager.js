function CourseDataManager(parent, advanceDataListener, parentDatabase, sendData, Request, buffer) {
	const COURSE_LIST = "COURSE_LIST";
	var userCourses = {};
	var userCourseId = [];
	var userHasCourses = true;
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var sendDataRequest = sendData.sendDataRequest;
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	 * Looks at the course and gives it some state if the state values do not exist.
	 */
	function stateCallback(course, courseCallback) {
		var state = course.getState();
		var updateCourse = false;
		if (isUndefined(state) || state == null) {
			state = PROTOBUF_UTIL.State();
			updateCourse = true;
		}
		try {
			// do state stuff
			var access = course.getAccessDate().getMillisecond();
			var close = course.getCloseDate().getMillisecond();
			var current = parent.getCurrentTime();
			if (isUndefined(state.accessible) || state.accessible == null) {
				if (current.lessThan(access) || current.greaterThan(close)) {
					state.accessible = false;
				} else {
					state.accessible = true;
				}
				updateCourse = true;
			}

			if (isUndefined(state.pastDue) || state.pastDue == null) {
				if (current.greaterThan(close)) {
					state.pastDue = true;
				} else {
					state.pastDue = false;
				}
				updateCourse = true;
			}
		} catch(exception) {
			console.log(exception);
		}

		// so we do not have to perform this again!
		if (updateCourse) {
			course.sate = state;
			setCourse(course);
		}

		if (courseCallback) {
			courseCallback(course);
		}
	}
	
	/**
	 * Returns a course with the given couresId will ask the server if it does not exist locally
	 *
	 * If the server is pulled and the course still does not exist the Id is set with nonExistantValue
	 * and the database is never polled for this item for the life of the program again.
	 *
	 * @param courseId The id of the course we want to find.
	 * @param courseCallback The method to call when the course has been found. (this is asynchronous)
	 */
	function getCourse(courseId, courseCallback) {
		// quick and dirty this is in ram (not in local memory)
		if (!isUndefined(userCourses[courseId])) {
			if (userCourses[courseId] == nonExistantValue) {
				courseCallback(nonExistantValue);
				return;
			}
			var bytes = ByteBuffer.decode64(userCourses[courseId]);
			stateCallback(PROTOBUF_UTIL.getSrlCourseClass().decode(bytes), courseCallback);
			return;
		}
		database.getFromCourses(courseId, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				// the listener from the server of the request
				// it stores the course locally then cals the callback with the course
				advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
					var school = PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
					var course = school.courses[0];
					if (isUndefined(course)) {
						userCourses[courseId] = nonExistantValue;
						courseCallback(nonExistantValue);
						advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.COURSE);
						return;
					}
					localScope.setCourse(course);
					stateCallback(course, courseCallback);
					advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.COURSE);
				});
				// creates a request that is then sent to the server
				sendDataRequest(PROTOBUF_UTIL.ItemQuery.COURSE, [courseId]);
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				courseCallback(nonExistantValue);
				userCourses[courseId] = nonExistantValue;
			} else {
				// gets the data from the database and calls the callback
				userCourses[courseId] = result.data;
				console.log(ByteBuffer);
				var bytes = ByteBuffer.decode64(result.data);
				stateCallback(PROTOBUF_UTIL.getSrlCourseClass().decode(bytes), courseCallback);
			}
		});
	};
	parent.getCourse = getCourse;

	function setCourse(course, courseCallback) {
		database.putInCourses(course.id, course.toBase64(), function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
		userCourses[course.id] = course.toBase64(); // stored in memory
	};
	parent.setCourse = setCourse;

	function deleteCourse(courseId, couresCallback) {
		database.deleteFromCourses(courseId, function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
		userCourses[course.id] = undefined; // removing it from the local map
	};
	parent.deleteCourse = deleteCourse;

	function setCourseIdList(idList) {
		userCourseId = idList;
		database.putInCourses(COURSE_LIST, idList); // no call back needed!
	}

	/**
	 * Returns a list of all of the courses in database.
	 *
	 * This does attempt to pull courses from the server!
	 */
	function getAllCourses(courseCallback) {
		var localFunction = setCourseIdList;
		// there are no courses loaded onto this client!
		advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.SCHOOL, function(evt, item) {
			if (!isUndefined(item.returnText) && item.returnText != "" && item.returnText !="null" && item.returnText != null) {
				userHasCourses = false;
				console.log(item.returnText);
				alert(item.returnText);
				advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.SCHOOL);
				return;
			}
			var school = PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
			var courseList = school.courses;
			var idList = [];
			for (var i = 0; i < courseList.length; i++) {
				var course = courseList[i];
				localScope.setCourse(course); // no callback is needed
				idList.push(course.id);
			}
			courseCallback(courseList);
			setCourseIdList(idList);
			advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, PROTOBUF_UTIL.ItemQuery.SCHOOL);
		});
		if (userCourseId.length == 0 && userHasCourses) {
			sendDataRequest(PROTOBUF_UTIL.ItemQuery.SCHOOL, [""]);
		//	console.log("course list from server polled!");
		} else {
			// This calls the server for updates then creates a list from the local data to appear fast
			// then updates list after server polling and comparing the two list.
		//	console.log("course list from local place polled!");
			var barrier = userCourseId.length;
			var courseList = [];

			// ask server for course list
			sendDataRequest(PROTOBUF_UTIL.ItemQuery.SCHOOL, [""]);

			// create local course list so everything appears really fast!
			for (var i = 0; i < userCourseId.length; i++) {
				this.getCourse(userCourseId[i], function(course) {
					courseList.push(course);
					barrier -= 1;
					if (barrier == 0) {
						courseCallback(courseList);
					}
				});
			}
			// we ask the program for the list of courses by id then we compare and update!
		}
	};
	parent.getAllCourses = getAllCourses;

	/**
	 * Inserts a course into the database.  This course must not exist.
	 *
	 * If there is a problem courseCallback is called with an error code.
	 * TODO: create error code and call courseCallback.
	 * 
	 * @param course
	 * @param courseCallback is called after the insertion of course into the local database. (this can be used for instant refresh)
	 * @param serverCallback serverCallback is called after the insertion of course into the server and the return of the server with the correct courseId
	 */
	function insertCourse(course, courseCallback, serverCallback) {
		var courseId = generateUUID();
		course.id = courseId;
		setCourse(course); // sets the course into the local database;
		if (courseCallback) courseCallback(course); // temp for now!

		sendData.sendDataInsert(PROTOBUF_UTIL.ItemQuery.COURSE, course.toArrayBuffer());
		advanceDataListener.setListener(Request.MessageType.DATA_INSERT, PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
			var resultArray = item.getResponseText().split(":");
			var oldId = resultArray[1];
			var newId = resultArray[0];
			// we want to get the current course in the local database in case it has changed while the server was processing.
			getCourse(oldId, function(course2) {
				deleteCourse(oldId);
				course2.id = newId;
				setCourse(course2, function() {
					serverCallback(course2);
				});
			});
		});
	}
	parent.insertCourse = insertCourse;

	/**
	 * Updates an existing course into the database.  This course must already exist.
	 *
	 * If there is a problem, courseCallback is called with an error code
	 * TODO: create error code.
	 * @param course
	 * @param courseCallback
	 * @param serverCallback the people next to me on the bus are really annoying
	 */
	function updateCourse(course, courseCallback, serverCallback) {
		setCourse(course); // overrides the course into the local database;
		if (courseCallback) courseCallback(course);

		sendData.sendDataUpdate(PROTOBUF_UTIL.ItemQuery.COURSE, course.toArrayBuffer());
		advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, PROTOBUF_UTIL.ItemQuery.COURSE, function(evt, item) {
			serverCallback(item); // we do not need to make server changes we just need to make sure it was successful.
		});
	}

	/*
	 * gets the id's of all of the courses in the user's local client.
	 */
	database.getFromCourses(COURSE_LIST, function(e, request, result) {
		if (isUndefined(result) || isUndefined(result.data)) {
			return;
		}
		userCourseId = result.data;
	});
}