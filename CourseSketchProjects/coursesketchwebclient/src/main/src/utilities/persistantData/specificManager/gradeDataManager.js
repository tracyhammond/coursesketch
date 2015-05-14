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

    parent.setGrade = function(protoGrade, callback) {
        advanceDataListener.setListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_INSERT, CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE);
            callback(protoGrade);
        });
        sendData.sendDataInsert(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, protoGrade.toArrayBuffer());
    };

    parent.getGrade = function(protoGrade) {
        idList = [protoGrade.courseId, protoGrade.assignmentId, protoGrade.problemId, protoGrade.userId];

        var gradingQuery = CourseSketch.PROTOBUF_UTIL.GradingQuery();
        var PermissionLevel = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInsturctor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.ALL_GRADES);

        sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, idList, gradingQuery);
    };

    /**
     * Gets all of the student grades.
     * @param {String} courseId
     * @param {Function} callback
     */
    parent.getAllAssignmentGrades = function(courseId, callback) {
        var isInstructor = CourseSketch.connection.isInstructor;
        advanceDataListener.setListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, function(evt, item) {
            advanceDataListener.removeListener(Request.MessageType.DATA_REQUEST, CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE);
            // after listener is removed
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

        var gradingQuery = CourseSketch.PROTOBUF_UTIL.GradingQuery();
        var PermissionLevel = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.PROTOBUF_UTIL.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInsturctor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.ALL_GRADES);

        sendData.sendDataRequest(CourseSketch.PROTOBUF_UTIL.ItemQuery.GRADE, [courseId], gradingQuery);
    };
}
