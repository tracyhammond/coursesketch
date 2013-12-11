/**
 * Attempts to use data as a database, pulls data from the server if it does not exist
 * @param userId
 * @param SrlCourse
 * @param SrlAssignment
 * @param SrlProblem
 * @param connection
 */
function schoolDataManager(userId, SrlCourse, SrlAssignment, SrlProblem, connection) {
	var initalizedFunction;
	var version = 1;
	var database = new protoDatabase(userId, version, initalizedFunction);
	var userCourses = {};
	
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
	})();

	this.getCourse(courseId, courseCallback) {
		database.getFromCourses(courseId, function(e, request, result) {
			result.data; // protobuf blob
			courseCallback(SrlCourse.decode(result.data));
		});
	}

	this.setCourse(courseId, course, courseCallback) {
		database.addToCourses({id: course.id, data:course.toByteArray()}, function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
		userCourses[courseId] = course; // stored in memory
	}

	this.deleteCourse(courseId, couresCallback) {
		database.deleteFromCourses(courseId, function(e, request) {
			if (courseCallback) {
				courseCallback(e, request);
			}
		});
	}

	/**
	 * Returns a list of all of the courses in the current database.
	 *
	 * This does not attempt to pull all courses from the remote server.
	 */
	this.getAllLocalCourses() {
		var array = new Array();
		for(key in userCourses) {
			array.push(userCourses[key]);
		}
		return array;
	}

	/**
	 * Returns a list of all of the courses in database.
	 *
	 * This does not attempt to pull all courses from the remote server.
	 */
	this.getAllCourses() {
		var array = new Array();
		for(key in userCourses) {
			array.push(userCourses[key]);
		}
		return array;
	}


	connection.setSchoolDataListener(function(event, msg) {
	
	});
}