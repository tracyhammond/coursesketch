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
function SchoolDataManager(userId, advanceDataListener, connection, schoolBuilder, query, request, byteBuffer, long) {
	const COURSE_LIST = "COURSE_LIST";
	var localScope = this;
	var localUserId = userId;
	var stateMachine = {};
	var databaseFinishedLoading = false;

	var useable = false;
	var version = 3;
	var dataListener = advanceDataListener;

	var ByteBuffer = byteBuffer;
	this.ByteBuffer = ByteBuffer;
	var Request = request;

	var SchoolBuilder = schoolBuilder;

	var QueryBuilder = query;
	var serverConnection = connection;

	var courseManager;
	var assignmentManager;
	var courseProblemManager;
	var submissionManager;
	
	var dataSender = new Object();

	/*
	 * END OF VARIABLE SETTING
	 */

	/**
	 * Returns true if the database is ready false otherwise.
	 *
	 * it is placed this far up so that it can be called even before most of the database is set up.
	 */
	this.isDatabaseReady = function() {
		return databaseFinishedLoading;
	}
	
	/**
	 * After the lower level database has been completely setup the higher level specific databases can be called.
	 */
	var initalizedFunction = function() {
		useable = true;
		if (!localScope.start) {
			var intervalVar = setInterval(function() {
				if (localScope.start) {
					console.log("Checking if higher database is truely ready!");
					clearInterval(intervalVar);
					localScope.start();
				}
			}, 100);
		} else {
			localScope.start();
		}
	};
	
	var database = new protoDatabase(localUserId, version, initalizedFunction);

	var addFunction = function(store, objectId, objectToAdd) {
		return store.put({"id" : objectId, "data" : objectToAdd});
	}

	var courseTable = database.createTable("Courses","id", addFunction);
	var assignmentTable = database.createTable("Assignments","id", addFunction);
	var problemTable = database.createTable("CourseProblems","id", addFunction);
	var bankProblemTable = database.createTable("BankProblems","id", addFunction);
	var submissionTable = database.createTable("Submissions","id", addFunction);

	(function() {
		var tables = new Array();
		tables.push(courseTable);
		tables.push(assignmentTable);
		tables.push(problemTable);
		tables.push(bankProblemTable);
		tables.push(submissionTable);
		database.setTables(tables);
		database.open();
	})();

	dataSender.sendDataRequest = function sendDataRequest(queryType, idList, advanceQuery) {
		var dataSend = new QueryBuilder.DataRequest();
		dataSend.items = new Array();
		var itemRequest = new QueryBuilder.ItemRequest(queryType);
		if (!isUndefined(idList)) {
			itemRequest.setItemId(idList);
		}
		if (!isUndefined(advanceQuery)) {
			itemRequest.setAdvanceQuery(advanceQuery);
		}
		dataSend.items.push(itemRequest);
		console.log("SENDING QUERY!");
		console.log(dataSend);
		serverConnection.sendRequest(serverConnection.createRequestFromData(dataSend, Request.MessageType.DATA_REQUEST));
	}

	dataSender.sendDataInsert = function sendDataInsert(queryType, data) {
		var dataSend = new QueryBuilder.DataSend();
		dataSend.items = new Array();
		dataSend.items.push(new QueryBuilder.ItemSend(queryType, data));
		serverConnection.sendRequest(serverConnection.createRequestFromData(dataSend, Request.MessageType.DATA_INSERT));
	}

	dataSender.sendDataUpdate = function sendDataUpdate(queryType, data) {
		var dataSend = new QueryBuilder.DataSend();
		dataSend.items = new Array();
		dataSend.items.push(new QueryBuilder.ItemRequest(queryType, data));
		serverConnection.sendRequest(serverConnection.createRequestFromData(dataSend, Request.MessageType.DATA_UPDATE));
	}

	this.emptySchoolData = function() {
		database.emptySelf();
	};

	this.start = function() {
		// creates a manager for just courses.
		courseManager = new CourseDataManager(this, dataListener, database, dataSender, [Request, QueryBuilder, SchoolBuilder], ByteBuffer);
		assignmentManager = new AssignmentDataManager(this, dataListener, database, dataSender, [Request, QueryBuilder, SchoolBuilder], ByteBuffer);
		courseProblemManager = new CourseProblemDataManager(this, dataListener, database, dataSender, [Request, QueryBuilder, SchoolBuilder], ByteBuffer);
		submissionManager = new SubmissionDataManager(this, dataListener, database, dataSender, [Request, QueryBuilder, ProtoSubmissionBuilder], ByteBuffer);
		console.log("database is ready for use! with user: " + userId);
		databaseFinishedLoading = true;
	}

	/**
	 * retrieves all the assignments for a given course.
	 *
	 * The callback is called with a list of assignment objects
	 */
	this.getAllAssignmentsFromCourse = function(courseId, assignmentCallback) {
		var getAssignments = this.getAssignments;
		this.getCourse(courseId, function(course) {
			if (isUndefined(course)) {
				throw "Course not defined";
			}
			if (course.assignmentList.length <= 0) {
				assignmentCallback([]);
			}
			getAssignments(course.assignmentList, assignmentCallback);
		});
	}

	/**
	 * retrieves all the assignments for a given course.
	 *
	 * The callback is called with a list of assignment objects
	 */
	this.getAllProblemsFromAssignment = function(assignmentId, problemCallback) {
		var getCourseProblems = this.getCourseProblems;
		this.getAssignment(assignmentId, function(assignment) {
			if (isUndefined(assignment)) {
				throw "Assignment not defined";
			}
			getCourseProblems(assignment.problemList, problemCallback);
		});
	}

	this.pollUpdates = function() {
		sendDataRequest(QueryBuilder.ItemQuery.UPDATE);
	};

	/**
	 * Adds the ability to set and remove state objects (for the use of transitioning from one page to the next!)
	 */
	this.addState = function(key, value) {
		stateMachine[key] = value;
	}

	this.getState = function(key) {
		return stateMachine[key];
	}

	this.hasState = function(key) {
		return !isUndefined(stateMachine[key]);
	}

	this.clearStates = function() {
		stateMachine = {};
	}

	/**
	 * Returns the current id that is being used with the database
	 */
	this.getCurrentId = function() {
		return localUserId;
	};
}
var nonExistantValue = "NONEXISTANT_VALUE";
var CURRENT_QUESTION = "CURRENT_QUESTION";
var CURRENT_ASSIGNMENT = "CURRENT_ASSIGNMENT";