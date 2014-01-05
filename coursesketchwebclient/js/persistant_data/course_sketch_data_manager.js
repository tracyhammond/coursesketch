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
function SchoolDataManager(userId, advanceDataListener, connection, schoolBuilder, query, request, byteBuffer) {
	const COURSE_LIST = "COURSE_LIST";
	var localScope = this;
	var localUserId = userId;

	var useable = false;
	var version = 2;
	var dataListener = advanceDataListener;

	var ByteBuffer = byteBuffer;
	var Request = request;

	var SchoolBuilder = schoolBuilder;/*
	var SrlCourse = SchoolBuilder.SrlCourse;
	var SrlAssignment = SchoolBuilder.SrlAssignment;
	var SrlProblem = SchoolBuilder.SrlProblem;
	var SrlBankProblem = SchoolBuilder.SrlBankProblem;*/

	var QueryBuilder = query;
	var serverConnection = connection;

	var courseManager;
	var assignmentManager;

	/*
	 * END OF VARIABLE SETTING
	 */

	var initalizedFunction = function() {
		console.log("database is ready for use! with user: " + userId);
		useable = true;
		localScope.start();
	};
	
	var database = new protoDatabase(localUserId, version, initalizedFunction);

	var addFunction = function(store, objectId, objectToAdd) {
		return store.put({"id" : objectId, "data" : objectToAdd});
	}
	var courseTable = database.createTable("Courses","id", addFunction);
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

	function sendDataRequest(queryType, idList) {
		var dataSend = new QueryBuilder.DataRequest();
		dataSend.items = new Array();
		dataSend.items.push(new QueryBuilder.ItemRequest(idList, queryType));
		serverConnection.sendRequest(serverConnection.createRequestFromData(dataSend, Request.MessageType.DATA_REQUEST));
	}

	this.emptySchoolData = function() {
		database.emptySelf();
	};

	this.start = function() {
		// creates a manager for just courses.
		courseManager = new CourseDataManager(this, dataListener, database, sendDataRequest, [Request, QueryBuilder, SchoolBuilder], ByteBuffer);
		assignmentManager = new AssignmentDataManager(this, dataListener, database, sendDataRequest, [Request, QueryBuilder, SchoolBuilder], ByteBuffer);
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
			getAssignments(course.assignmentList, assignmentCallback);
		});
	}
	
	/**
	 * retrieves all the assignments for a given course.
	 *
	 * The callback is called with a list of assignment objects
	 */
	this.getAllProblemsFromAssignment = function(assignmentId, problemCallback) {
		this.getAssignment(assignmentId, function(assignment) {
			if (isUndefined(assignment)) {
				throw "Assignment not defined";
			}
			this.getProblems(assignment.problemList, problemCallback);
		});
	}
}
const nonExistantValue = "NONEXISTANT_VALUE";