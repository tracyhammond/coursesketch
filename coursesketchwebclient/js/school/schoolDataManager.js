/**
 * Attempts to use data as a database, pulls data from the server if it does not exist
 *
 * @param userId The user that this database is associated with.
 * @param connection The connection to the server which will handle all connections relating to certain queries.
 * @param advanceDataListener An instance of {@link AdvanceDataListener} this is used for responses to queries made by the database server
 * @param schoolBuilder Used to create instances of protobuf school.
 * @param query Holds the protobuf builder for querying items.
 * @param byteBuffer The static instance that is used for encoding and decoding data.
 */
function schoolDataManager(userId, advanceDataListener, connection, schoolBuilder, query, request, byteBuffer) {
	const courseList = "COURSE_LIST";
	var localScope = this;
	var localUserId = userId;

	var useable = false;
	var version = 2;
	var userCourses = {};
	var userCourseId = [];
	var userHasCourses = true;
	var dataListener = advanceDataListener;

	var ByteBuffer = byteBuffer;
	var Request = request;

	var SchoolBuilder = schoolBuilder;
	var SrlCourse = SchoolBuilder.SrlCourse;
	var SrlAssignment = SchoolBuilder.SrlAssignment;
	var SrlProblem = SchoolBuilder.SrlProblem;
	var SrlBankProblem = SchoolBuilder.SrlBankProblem;

	var QueryBuilder = query;
	var serverConnection = connection;

	/*
	 * END OF VARIABLE SETTING
	 */

	var initalizedFunction = function() {
		console.log("database is ready for use!");
		useable = true;
		database.getFromCourses(courseList, function(e, request, result) {
			if (isUndefined(result) || isUndefined(result.data)) {
				return;
			}
			userCourseId = result.data;
		});
	};
	
	var database = new protoDatabase(localUserId, version, initalizedFunction);
	
	var addFunction = function(store, objectId, objectToAdd) {
		return store.put({"id" : objectId, "data" : objectToAdd});
	}
	var courseTable = database.createTable("Courses","id", addFunction);
	console.log(courseTable);
	var assignmentTable = database.createTable("Assignments","id", addFunction);
	var problemTable = database.createTable("Problems","id", addFunction);

	(function() {
		var tables = new Array();
		tables.push(courseTable);
		tables.push(assignmentTable);
		tables.push(problemTable);
		database.setTables(tables);
		database.open();
	})();

	/**
	 * Returns a course with the given couresId will ask the server if it does not exist locally
	 *
	 * If the server is pulled and the course still does not exist the Id is set with nonExistantValue
	 * and the database is never polled for this item for the life of the program again.
	 *
	 * @param courseId The id of the course we want to find.
	 * @param courseCallback The method to call when the course has been found. (this is asynchronous)
	 */
	this.getCourse = function getCourse(courseId, courseCallback) {
		// quick and dirty this is in ram (not in local memory)
		if (!isUndefined(userCourses[courseId])) {
			if (userCourses[courseId] == nonExistantValue) {
				courseCallback(nonExistantValue);
				return;
			}
			console.log(userCourses[courseId]);
			var bytes = ByteBuffer.decode64(userCourses[courseId]);
			courseCallback(SrlCourse.decode(bytes));
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
						return;
					}
					localScope.setCourse(course);
					courseCallback(course);
				});
				// creates a request that is then sent to the server
				var dataSend = new QueryBuilder.DataRequest();
				dataSend.items = new Array();
				dataSend.items.push(new QueryBuilder.ItemRequest([courseId], QueryBuilder.ItemQuery.COURSE));
				serverConnection.sendRequest(serverConnection.createRequestFromData(dataSend, Request.MessageType.DATA_REQUEST));
			} else if (result.data == nonExistantValue) {
				// the server holds this special value then it means the server does not have the value
				courseCallback(nonExistantValue);
				userCourses[courseId] = nonExistantValue;
			} else {
				// gets the data from the database and calls the callback
				userCourses[courseId] = result.data;
				var bytes = ByteBuffer.decode64(result.data);
				courseCallback(SrlCourse.decode(bytes));
			}
		});
	};

	this.setCourse = function(course, courseCallback) {
		database.putInCourses(course.id, course.toBase64(), function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
		userCourses[course.id] = course.toBase64(); // stored in memory
	};

	this.deleteCourse = function(courseId, couresCallback) {
		database.deleteFromCourses(courseId, function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
		userCourses[course.id] = undefined; // removing it from the local map
	};

	/**
	 * Returns a list of all of the courses in database.
	 *
	 * This does attempt to pull courses from the server!
	 */
	this.getAllCourses = function(courseCallback) {
		// there are no courses loaded onto this client!
		if (userCourseId.length == 0 && userHasCourses) {
			advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE, function(evt, item) {
				if (!isUndefined(item.returnText) && item.returnText != "") {
					alert(item.returnText);
					return;
				}
				var school = SchoolBuilder.SrlSchool.decode(item.data);
				var courseList = school.courses;
				var idList = [];
				for(var i = 0; i < courseList.length; i++) {
					var course = courseList[i];
					localScope.setCourse(course); // no callback is needed
					idList.push(course.id);
				}
				courseCallback(courseList);
				localScope.setCourseIdList(idList);
			});
		} else {
<<<<<<< HEAD:coursesketchwebclient/js/persistant_data/course_sketch_data_manager.js
			// This calls the server for updates then creates a list from the local data to appear fast
			// then updates list after server polling and comparing the two list.
			console.log("course list from local place polled!");
			var barrier = userCourseId.length;
			var courseList = [];
			// need to create an update function!
			/*
			advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, QueryBuilder.ItemQuery.COURSE_LIST, function(evt, item) {
				console.log("Course data!!!!!!!");
			});
			*/
			
			// ask server for course list
			sendDataRequest(QueryBuilder.ItemQuery.SCHOOL, [""]);

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
=======
			
>>>>>>> 42d542e1cdf2bdcdb5f65839bc9ec3c05dc77c4f:coursesketchwebclient/js/school/schoolDataManager.js
		}
	};

	function setCourseIdList(idList) {
		userCourseId = idList;
		this.putInCourses(COURSE_LIST, idList); // no call back needed!
	}

	this.emptySchoolData = function() {
		database.emptySelf();
	};
	/*
	connection.setSchoolDataListener(function(event, msg) {
	
	});
	*/
}
const nonExistantValue = "NONEXISTANT_VALUE";