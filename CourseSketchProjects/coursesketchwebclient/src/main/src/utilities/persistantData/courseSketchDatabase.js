/**
 * @class SchoolDataManager
 * Attempts to use data as a database, pulls data from the server if it does not
 * exist
 *
 * @param {String} userId
 *            The user that this database is associated with.
 * @param {AdvanceDataListener} advanceDataListener
 *            An instance of {@link AdvanceDataListener} this is used for
 *            responses to queries made by the database server
 * @param {Connection} connection
 *            The connection to the server which will handle all connections
 *            relating to certain queries.
 * @param {Request} Request
 *            The class representing the Request protobuf used to get the
 *            message type.
 * @param {ByteBuffer} ByteBuffer
 *            The static instance that is used for encoding and decoding data.
 */
function SchoolDataManager(userId, advanceDataListener, connection, Request, ByteBuffer) {
    var LAST_UPDATE_TIME = 'LAST_UPDATE_TIME';
    var localScope = this;
    var localUserId = userId;
    var stateMachine = new Map();
    var databaseFinishedLoading = false;

    var version = 6;
    var dataListener = advanceDataListener;

    var serverConnection = connection;

    var courseManager;
    var assignmentManager;
    var courseProblemManager;
    var submissionManager;
    var lectureDataManager;
    var slideDataManager;

    var dataSender = {};

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
                    console.log('Checking if higher database is truly ready!');
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
        /**
         * Add function for adding elements to the database.
         * @returns {*}
         */
        var addFunction = function(store, objectId, objectToAdd) {
            return store.put({
                'id': objectId,
                'data': objectToAdd
            });
        };

        var tables = [];
        tables.push(database.createTable('Courses', 'id', addFunction));
        tables.push(database.createTable('Assignments', 'id', addFunction));
        tables.push(database.createTable('CourseProblems', 'id', addFunction));
        tables.push(database.createTable('BankProblems', 'id', addFunction));
        tables.push(database.createTable('Submissions', 'id', addFunction));
        tables.push(database.createTable('Grades', 'id', addFunction));
        tables.push(database.createTable('Lectures', 'id', addFunction));
        tables.push(database.createTable('Slides', 'id', addFunction));
        tables.push(database.createTable('Other', 'id', addFunction));

        database.setTables(tables);
        database.open();
    })();

    /**
     * Sends a request to retrive data from the server.
     */
    dataSender.sendDataRequest = function sendDataRequest(queryType, idList, advanceQuery) {
        var dataSend = CourseSketch.PROTOBUF_UTIL.DataRequest();
        dataSend.items = [];
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
        dataSend.items = [];
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
        dataSend.items = [];
        var itemUpdate = CourseSketch.PROTOBUF_UTIL.ItemSend();
        itemUpdate.setQuery(queryType);
        itemUpdate.setData(data);
        dataSend.items.push(itemUpdate);

        serverConnection.sendRequest(CourseSketch.PROTOBUF_UTIL.createRequestFromData(dataSend, Request.MessageType.DATA_UPDATE));
    };

    /**
     * This is supposed to clean out the database.
     *
     * Currently does not work.
     */
    this.emptySchoolData = function() {
        database.emptySelf();
    };

    /**
     * Creates the specific datamanagers.
     */
    this.start = function() {
        // creates a manager for just courses.
        courseManager = new CourseDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        assignmentManager = new AssignmentDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        courseProblemManager = new CourseProblemDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        submissionManager = new SubmissionDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        lectureDataManager = new LectureDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);
        slideDataManager = new SlideDataManager(this, dataListener, database, dataSender, Request, ByteBuffer);

        console.log('Database is ready for use! with user: ' + userId);
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
                throw new Error('Course not defined');
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
                throw new Error('Assignment not defined');
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
            // TODO: there used to be update code here that would update the local cache
            // When that code isbeing used again to optimize load times please add back the update function here!
        });
    };

    /**
     * Adds the ability to set and remove state objects (for the use of
     * transitioning from one page to the next!)
     */
    this.addState = function(key, value) {
        stateMachine.set(key, value);
    };

    /**
     * Returns the state at the given key.
     * @param {String} key
     */
    this.getState = function(key) {
        return stateMachine.get(key);
    };

    /**
     * Returns true if the state exists
     * @param {String} key
     */
    this.hasState = function(key) {
        return stateMachine.has(key);
    };

    /**
     * Empties all state data.
     */
    this.clearStates = function() {
        stateMachine = new Map();
    };

    /**
     * Returns the current id that is being used with the database
     */
    this.getCurrentId = function() {
        return localUserId;
    };

    this.getCurrentTime = connection.getCurrentTime;

    CourseSketch.DatabaseException = DatabaseException;

    /**
     * A helper function for testing that waits for the database to be loaded before calling a callback.
     * @param {Function} callback Called when the database is ready.
     */
    this.waitForDatabase = function waitForDatabase(callback) {
        var interval = setInterval(function() {
            if (localScope.isDatabaseReady()) {
                clearInterval(interval);
                callback();
            } // Endif
        }, 50);
    };
}
var CURRENT_QUESTION = 'CURRENT_QUESTION';
var CURRENT_ASSIGNMENT = 'CURRENT_ASSIGNMENT';
