/**
 * Attempts to use data as a database, pulls data from the server if it does not
 * exist
 * 
 * @param userId
 *            The user that this database is associated with.
 * @param connection
 *            The connection to the server which will handle all connections
 *            relating to certain queries.
 * @param advanceDataListener
 *            An instance of {@link AdvanceDataListener} this is used for
 *            responses to queries made by the database server
 * @param Request
 *            The class representing the Request protobuf used to get the
 *            message type.
 * @param byteBuffer
 *            The static instance that is used for encoding and decoding data.
 */
function SchoolDataManager(userId, advanceDataListener, connection, Request, ByteBuffer, long) {
    var COURSE_LIST = "COURSE_LIST";
    var LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
    var localScope = this;
    var localUserId = userId;
    var stateMachine = {};
    var databaseFinishedLoading = false;

    var version = 6;
    var dataListener = advanceDataListener;

    var serverConnection = connection;

    var courseManager;
    var assignmentManager;
    var courseProblemManager;
    var submissionManager;
    var gradeManager;

    var dataSender = new Object();

    /*
     * END OF VARIABLE SETTING
     */

    /**
     * Returns true if the database is ready false otherwise.
     * 
     * it is placed this far up so that it can be called even before most of the
     * database is set up.
     */
    this.isDatabaseReady = function() {
        return databaseFinishedLoading;
    };

    /**
     * After the lower level database has been completely setup the higher level
     * specific databases can be called.
     */
    var initalizedFunction = function() {
        if (!localScope.start) {
            var intervalVar = setInterval(function() {
                if (localScope.start) {
                    console.log("Checking if higher database is truly ready!");
                    clearInterval(intervalVar);
                    localScope.start();
                }
            }, 100);
        } else {
            localScope.start();
        }
    };

    var database = new ProtoDatabase(localUserId, version, initalizedFunction);

    (function() {

        var addFunction = function(store, objectId, objectToAdd) {
            return store.put({
                "id" : objectId,
                "data" : objectToAdd
            });
        };

        var tables = new Array();
        tables.push(database.createTable("Courses", "id", addFunction));
        tables.push(database.createTable("Assignments", "id", addFunction));
        tables.push(database.createTable("CourseProblems", "id", addFunction));
        tables.push(database.createTable("BankProblems", "id", addFunction));
        tables.push(database.createTable("Submissions", "id", addFunction));
        tables.push(database.createTable("Grades", "id", addFunction));
        tables.push(database.createTable("Lectures", "id", addFunction));
        tables.push(database.createTable("Slides", "id", addFunction));
        tables.push(database.createTable("Other", "id", addFunction));

        database.setTables(tables);
        database.open();
    })();

    /**
     * Sends a request to retrive data from the server.
     */
    dataSender.sendDataRequest = function sendDataRequest(queryType, idList, advanceQuery) {
        var dataSend = CourseSketch.PROTOBUF_UTIL.DataRequest();
        dataSend.items = new Array();
        var itemRequest = CourseSketch.PROTOBUF_UTIL.ItemRequest();
        itemRequest.setQuery(queryType);

        if (!isUndefined(idList)) {
            itemRequest.setItemId(idList);
        }
        if (!isUndefined(advanceQuery)) {
            itemRequest.setAdvanceQuery(advanceQuery.toArrayBuffer());
        }
        dataSend.items.push(itemRequest);
        serverConnection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(dataSend, Request.MessageType.DATA_REQUEST));
    };

    /**
     * Inserts data into the server database.
     */
    dataSender.sendDataInsert = function sendDataInsert(queryType, data) {
        var dataSend = CourseSketch.PROTOBUF_UTIL.DataSend();
        dataSend.items = new Array();
        var itemSend = CourseSketch.PROTOBUF_UTIL.ItemSend();
        itemSend.setQuery(queryType);
        itemSend.setData(data);
        dataSend.items.push(itemSend);

        serverConnection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(dataSend, Request.MessageType.DATA_INSERT));
    };

    /**
     * Sends an update to the server for the data to be updated.
     */
    dataSender.sendDataUpdate = function sendDataUpdate(queryType, data) {
        var dataSend = CourseSketch.PROTOBUF_UTIL.DataSend();
        dataSend.items = new Array();
        var itemUpdate = CourseSketch.PROTOBUF_UTIL.ItemSend();
        itemUpdate.setQuery(queryType);
        itemUpdate.setData(data);
        dataSend.items.push(itemUpdate);
        serverConnection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(dataSend, Request.MessageType.DATA_UPDATE));
    };

    this.emptySchoolData = function() {
        database.emptySelf();
    };

    this.start = function() {
        // creates a manager for just courses.
        /* courseManager = */new CourseDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* assignmentManager = */new AssignmentDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* courseProblemManager = */new CourseProblemDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* submissionManager = */new SubmissionDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* lectureDataManager = */new LectureDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* slideDataManager = */new SlideDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        /* submissionManager = */// new GradeDataManager(this, dataListener,
                                // database, dataSender, Request, ByteBuffer);
        console.log("Database is ready for use! with user: " + userId);
        databaseFinishedLoading = true;
    };

    /**
     * retrieves all the assignments for a given course.
     * 
     * The callback is called with a list of assignment objects
     */
    this.getAllAssignmentsFromCourse = function(courseId, assignmentCallback) {
        var getAssignments = this.getAssignments;
        this.getCourse(courseId, function(course) {
            if (isUndefined(course)) {
                throw new Error("Course not defined");
            }
            if (course.assignmentList.length <= 0) {
                assignmentCallback([]);
            }
            getAssignments(course.assignmentList, assignmentCallback);
        });
    };

    /**
     * retrieves all the assignments for a given course.
     * 
     * The callback is called with a list of assignment objects
     */
    this.getAllProblemsFromAssignment = function(assignmentId, problemCallback) {
        var getCourseProblems = this.getCourseProblems;
        this.getAssignment(assignmentId, function(assignment) {
            if (isUndefined(assignment)) {
                throw new Error("Assignment not defined");
            }
            getCourseProblems(assignment.problemList, problemCallback);
        });
    };

    /**
     * Polls the server for updates, after all items
     */
    this.pollUpdates = function(callback) {
        database.getFromOther(LAST_UPDATE_TIME, function(e, request, result) {
            if (isUndefined(result) || isUndefined(result.data)) {
                dataSender.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.UPDATE);
            } else {
                var lastTime = result.data;
                dataSender.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.UPDATE, [ lastTime ]);
            }
        });
        var functionCalled = false;
        var timeout = setTimeout(function() {
            if (!functionCalled && callback) {
                functionCalled = true;
                callback();
            }
        }, 5000);

        advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.UPDATE, function(evt, item) {
            // to store for later recall
            database.putInOther(LAST_UPDATE_TIME, connection.getCurrentTime().toString());
            clearTimeout(timeout);
            var school = CourseSketch.PROTOBUF_UTIL.getSrlSchoolClass().decode(item.data);
            var courseList = school.courses;
            for (var i = 0; i < courseList.length; i++) {
                localScope.setCourse(courseList[i]);
            }

            var assignmentList = school.assignments;
            for (var i = 0; i < assignmentList.length; i++) {
                localScope.setAssignment(assignmentList[i]);
            }

            var problemList = school.problems;
            for (var i = 0; i < problemList.length; i++) {
                localScope.setCourseProblem(problemList[i]);
            }

            if (!functionCalled && callback) {
                functionCalled = true;
                callback();
            }
            ;
        });
    };

    /**
     * Adds the ability to set and remove state objects (for the use of
     * transitioning from one page to the next!)
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

    this.getCurrentTime = connection.getCurrentTime;
}
var nonExistantValue = "NONEXISTANT_VALUE";
var CURRENT_QUESTION = "CURRENT_QUESTION";
var CURRENT_ASSIGNMENT = "CURRENT_ASSIGNMENT";
