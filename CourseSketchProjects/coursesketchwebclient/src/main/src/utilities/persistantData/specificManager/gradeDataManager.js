/**
 * A manager for grades that talks with the remote server.
 *
 * Created by gigemjt on 5/12/15.
 *
 * @param {SchoolDataManager} parent - The database that will hold the methods of this instance.
 * @param {AdvanceDataListener} advanceDataListener - An object that makes sending data much easier.
 * @param {ProtoDatabase} parentDatabase - (Not used in this manager)
 * @param {ByteBuffer} ByteBuffer - Used in the case of longs for javascript.
 * @constructor
 */
function GradeDataManager(parent, advanceDataListener, parentDatabase, ByteBuffer) {

    /**
     * Adds a new grade change to the database.
     *
     * The protograde specifies how you are inserting a grade.
     * The userId says who the grade is affecting.
     *
     * @param {ProtoGrade} protoGrade - used to help create the query.  This should be similar to what you would expect it to return.
     * @param {Function} callback - called after the grade has been set.
     */
    parent.setGrade = function(protoGrade, callback) {
        advanceDataListener.sendDataInsert(CourseSketch.prutil.ItemQuery.GRADE, protoGrade.toArrayBuffer(), function(evt, item) {
            if (isException(item)) {
                CourseSketch.clientException(item);
            }
            callback(protoGrade);
        });
    };

    /**
     * Returns a grade from the database.
     *
     * @param {ProtoGrade} protoGrade - The grade in a similar format to what you want back.
     * @param {Function} callback - Called after the grade has been retrieved.
     */
    parent.getGrade = function(protoGrade, callback) {
        if (isUndefined(callback)) {
            throw new DatabaseException('Calling getGrade with an undefined callback');
        }

        var isInstructor = CourseSketch.connection.isInstructor;
        var idList = [ protoGrade.courseId, protoGrade.assignmentId, protoGrade.problemId, protoGrade.userId ];

        var gradingQuery = CourseSketch.prutil.GradingQuery();
        var PermissionLevel = CourseSketch.prutil.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.prutil.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInstructor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.SINGLE_GRADE);
        console.log('getting grade id list: ', idList);
        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.GRADE, idList, gradingQuery);

        advanceDataListener.sendDataRequest(itemRequest, function(evt, item) {
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

            var decodedGrade = CourseSketch.prutil.decodeProtobuf(item.data[0], CourseSketch.prutil.getProtoGradeClass());
            callback(decodedGrade);
        });
    };

    /**
     * Gets all of the student grades.
     *
     * @param {String} courseId - The id of the course where the grades are being retrieved from.
     * @param {Function} callback - Called after all of the grades are retrieved.
     */
    parent.getAllAssignmentGrades = function(courseId, callback) {
        if (isUndefined(callback)) {
            throw new DatabaseException('Calling getGrade with an undefined callback');
        }
        if (isUndefined(courseId)) {
            throw new DatabaseException('The given id is not assigned', 'getting Grade: ' + courseId);
        }

        var isInstructor = CourseSketch.connection.isInstructor;

        var gradingQuery = CourseSketch.prutil.GradingQuery();
        var PermissionLevel = CourseSketch.prutil.getGradingQueryClass().PermissionLevel;
        var SearchType = CourseSketch.prutil.getGradingQueryClass().SearchType;

        gradingQuery.setPermissionLevel(isInstructor ? PermissionLevel.INSTRUCTOR : PermissionLevel.STUDENT);
        gradingQuery.setSearchType(SearchType.ALL_GRADES);

        var itemRequest = CourseSketch.prutil.createItemRequest(CourseSketch.prutil.ItemQuery.GRADE, [ courseId ], gradingQuery);
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
                var decodedGrade = CourseSketch.prutil.decodeProtobuf(item.data[i], CourseSketch.prutil.getProtoGradeClass());
                protoGradeList.push(decodedGrade);
            }
            callback(protoGradeList);
        });
    };
}
