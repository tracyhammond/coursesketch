function GradeDataManager(parent, advanceDataListener, parentDatabase, sendData, builders, buffer) {
	/*const COURSE_LIST = "COURSE_LIST";
	var userCourses = {};
	var userCourseId = [];
	var userHasCourses = true;*/
	//var ;
	var dataListener = advanceDataListener;
	var database = parentDatabase;
	var sendDataRequest = sendData.sendDataRequest;
	var Request = builders[0];
	var QueryBuilder = builders[1];
	var SchoolBuilder = builders[2];
	var localScope = parent;
	var ByteBuffer = buffer;

	/**
	 * Looks at the grade(s) and gives it some state if the state values do not exist.
	 */
	function stateCallback(grade, gradeCallback) {
		var state = grade.getState();
		var updateGrade = false;
		if (isUndefined(state) || state == null) {
			state = new SchoolBuilder.State();
			updateGrade = true;
		}
		try {
			// do state stuff
			
			//will we need date info.?
			//If so, relate to grading penalties and the like?
			//referencing protobuf?
			
			/*var access = assignment.getAccessDate().getMillisecond();
			var close = assignment.getCloseDate().getMillisecond();
			var due = assignment.getDueDate().getMillisecond();*/

			var current = parent.getCurrentTime();
			if (isUndefined(state.accessible) || state.accessible == null) {
				if (current.lessThan(access) || current.greaterThan(close)) {
					state.accessible = false;
				} else {
					state.accessible = true;
				}
				updateGrade = true;
			}

			if (isUndefined(state.pastDue) || state.pastDue == null) {
				if (current.greaterThan(due)) {
					state.pastDue = true;
				} else {
					state.pastDue = false;
				}
				updateGrade = true;
			}
		} catch(exception) {
			console.log(exception);
		}

		// so we do not have to perform this again!
		if (updateGrade) {
			grade.state = state;
			setGrade(grade);
		}

		if (gradeCallback) {
			gradeCallback(grade);
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
	/*function getGrade(gradeId, gradeCallback) {
		// quick and dirty this is in ram (not in local memory)
		if (!isUndefined(userCourses[courseId])) {
			if (userCourses[courseId] == nonExistantValue) {
				courseCallback(nonExistantValue);
				return;
			}
			var bytes = ByteBuffer.decode64(userCourses[courseId]);
			stateCallback(SrlCourse.decode(bytes), courseCallback);
			return;
		}
		database.getFromCourses(courseId, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				// the listener from the server of the request
				// it stores the course locally then cals the callback with the course
				advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE, function(evt, item) {
					var school = SchoolBuilder.SrlSchool.decode(item.data);
					var course = school.courses[0];
					if (isUndefined(course)) {
						userCourses[courseId] = nonExistantValue;
						courseCallback(nonExistantValue);
						advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE);
						return;
					}
					localScope.setCourse(course);
					stateCallback(course, courseCallback);
					advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE);
				});
				// creates a request that is then sent to the server
				sendDataRequest(QueryBuilder.ItemQuery.COURSE, [courseId]);
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				courseCallback(nonExistantValue);
				userCourses[courseId] = nonExistantValue;
			} else {
				// gets the data from the database and calls the callback
				userCourses[courseId] = result.data;
				var bytes = ByteBuffer.decode64(result.data);
				stateCallback(SrlCourse.decode(bytes), courseCallback);
			}
		});
	};*/
	//parent.getGrade = getGrade;

	/*function setCourse(course, courseCallback) {
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
	}*/

	/*
	 * Returns a list of all of the grades in database.
	 */
	function getAllGrades(gradeCallback) {
		var localFunction = setGradeIdList;
		// there are no grades loaded onto this client!
		advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.SCHOOL, function(evt, item) {
			if (!isUndefined(item.returnText) && item.returnText != "" && item.returnText !="null" && item.returnText != null) {
				userHasCourses = false;
				console.log(item.returnText);
				alert(item.returnText);
				advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.SCHOOL);
				return;
			}
			var school = SchoolBuilder.SrlSchool.decode(item.data);
			var gradeList = school.grades;
			var idList = [];
			for (var i = 0; i < gradeList.length; i++) {
				var grade = gradeList[i];
				localScope.setGrade(grade); // no callback is needed
				idList.push(grade.id);
			}
			gradeCallback(gradeList);
			setGradeIdList(idList);
			advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.SCHOOL);
		});
		if (userGradeId.length == 0 && userHasGrades) {
			sendDataRequest(QueryBuilder.ItemQuery.SCHOOL, [""]);
		//	console.log("course list from server polled!");
		} else {
			// This calls the server for updates then creates a list from the local data to appear fast
			// then updates list after server polling and comparing the two list.
		//	console.log("course list from local place polled!");
			var barrier = userGradeId.length;
			var gradeList = [];

			// ask server for course list
			sendDataRequest(QueryBuilder.ItemQuery.SCHOOL, [""]);

			// create local course list so everything appears really fast!
			for (var i = 0; i < userGradeId.length; i++) {
				this.getGrade(userGradeId[i], function(grade) {
					gradeList.push(grade);
					barrier -= 1;
					if (barrier == 0) {
						courseCallback(gradeList);
					}
				});
			}
			// we ask the program for the list of courses by id then we compare and update!
		}
	};
	parent.getAllGrades = getAllGrades;

	/**
	 * Inserts a grade into the database.  This course must not exist.
	 *
	 * If there is a problem courseCallback is called with an error code.
	 * TODO: create error code and call courseCallback.
	 * 
	 * @param course
	 * @param courseCallback is called after the insertion of course into the local database. (this can be used for instant refresh)
	 * @param serverCallback serverCallback is called after the insertion of course into the server and the return of the server with the correct courseId
	 */
	/*function insertGrade(grade, gradeCallback, serverCallback) {
		var gradeId = generateUUID();
		grade.id = gradeId;
		setGrade(grade); // sets the course into the local database;
		if (gradeCallback) gradeCallback(grade); // temp for now!

		sendData.sendDataInsert(QueryBuilder.ItemQuery.GRADE, Itgrade.toArrayBuffer());
		advanceDataListener.setListener(Request.MessageType.DATA_INSERT, QueryBuilder.ItemQuery.COURSE, function(evt, item) {
			var resultArray = item.getResponseText().split(":");
			var oldId = resultArray[1];
			var newId = resultArray[0];
			// we want to get the current course in the local database in case it has changed while the server was processing.
			getGrade(oldId, function(grade2) {
				deleteGrade(oldId);
				grade2.id = newId;
				setGrade(grade2, function() {
					serverCallback(grade2);
				});
			});
		});
	}
	parent.insertGrade = insertGrade;*/

	/**
	 * Updates an existing grade into the database.  This course must already exist.
	 *
	 * If there is a problem, gradeCallback is called with an error code
	 * TODO: create error code.
	 * @param grade
	 * @param gradeCallback
	 * @param serverCallback don't watch anything in public without headphones -- people WILL hate you
	 */
	/*function updateGrade(grade, gradeCallback, serverCallback) {
		setGrade(grade); // overrides the course into the local database;
		if (gradeCallback) gradeCallback(grade);

		sendData.sendDataUpdate(QueryBuilder.ItemQuery.GRADE, Itgrade.toArrayBuffer());
		advanceDataListener.setListener(Request.MessageType.DATA_UPDATE, QueryBuilder.ItemQuery.GRADE, function(evt, item) {
			serverCallback(item); // we do not need to make server changes we just need to make sure it was successful.
		});
	}*/

	/*
	 * gets the id's of all of the courses in the user's local client.
	 */
	/*database.getFromGrades(GRADE_LIST, function(e, request, result) {
		if (isUndefined(result) || isUndefined(result.data)) {
			return;
		}
		userGradeId = result.data;
	});*/
}