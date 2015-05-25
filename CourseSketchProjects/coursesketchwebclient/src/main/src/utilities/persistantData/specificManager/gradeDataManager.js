/**
 * A manager for grades that talks with the remote server.
 *
 * Created by gigemjt on 5/12/15.
 *
 * @param {CourseSketchDatabase} parent
 * @param {AdvanceDataListener} advanceDataListener
 * @param {IndexedDB} parentDatabase (Not used in this manager)
 * @param {Function} sendData A function that makes sending data much easier
 * @param {SrlRequest} Request A shortcut to a request
 * @param {ByteBuffer} ByteBuffer Used in the case of longs for javascript.
 * @constructor
 */
function GradeDataManager(parent, advanceDataListener, parentDatabase, sendData, Request, ByteBuffer) {

    /**
     * Adds a new grade change to the database.
     *
     * The protograde specifies how you are inserting a grade.
     * The userId says who the grade is affecting.
     * @param {ProtoGrade} protoGrade used to help create the query.  This should be similar to what you would expect it to return.
     * @param {Function} callback called after the grade has been set.
     */
    parent.setGrade = function(protoGrade, callback) {
        advanceDataListener.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, protoGrade.toArrayBuffer(), function(evt, item) {
            if (isException(item)) {
                CourseSketch.clientException(item);
            }
            callback(protoGrade);
        });
    };

    /**
     * Returns a grade from the database.
     *
     * @param {ProtoGrade} protoGrade The grade in a similar format to what you want back.
     */
    parent.getGrade = function(protoGrade, callback) {
        if (isUndefined(callback)) {
            throw new DatabaseException('Calling getGrade with an undefined callback');
        }

        var isInstructor = CourseSketch.connection.isInstructor;
        var idList = [ protoGrade.courseId, protoGrade.assignmentId, protoGrade.problemId, protoGrade.userId ];

        var gradingQuery = CourseSketch.PROTOBUF_UTIL.GradingQuery();
        var PermissionLevel = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInstructor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.SINGLE_GRADE);
        console.log('getting grade id list: ', idList);
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, idList, gradingQuery);

        advanceDataListener.semdDataRequest(itemRequest, function(evt, item) {
            if (isException(item)) {
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                protoGrade), item);
                return;
            }
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                // not calling the state callback because this should skip that step.
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                protoGrade));
                return;
            }

            var decodedGrade = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(item.data[0], CourseSketch.PROTOBUF_UTIL.getProtoGradeClass());
            callback(decodedGrade);
        });
    };

    /**
     * Gets all of the student grades.
     * @param {String} courseId
     * @param {Function} callback
     */
    parent.getAllAssignmentGrades = function(courseId, callback) {
        if (isUndefined(callback)) {
            throw new DatabaseException('Calling getGrade with an undefined callback');
        }

        var isInstructor = CourseSketch.connection.isInstructor;

        var gradingQuery = CourseSketch.PROTOBUF_UTIL.GradingQuery();
        var PermissionLevel = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInstructor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.ALL_GRADES);

        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, [ courseId ], gradingQuery);
        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
            if (isException(item)) {
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                courseId), item);
                return;
            }
            if (isUndefined(item.data) || item.data === null || item.data.length <= 0) {
                // not calling the state callback because this should skip that step.
                callback(new DatabaseException('There are no grades for the course or the data does not exist ' +
                courseId));
                return;
            }

            var protoGradeList = [];
            for (var i = 0; i < item.data.length; i++) {
                var decodedGrade = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(item.data[i], CourseSketch.PROTOBUF_UTIL.getProtoGradeClass());
                protoGradeList.push(decodedGrade);
            }
            callback(protoGradeList);
        });
    };
}
