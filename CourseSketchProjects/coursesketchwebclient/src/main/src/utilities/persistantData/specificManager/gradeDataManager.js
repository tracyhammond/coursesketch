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


    parent.setGrade = function() {
        // DO NOTHING
    };

    parent.getGrade = function() {

    };

    /**
     * Gets all of the student grades.
     * @param {String} courseId
     * @param {Function} callback
     */
    parent.getStudentGrades = function(courseId, callback) {

    };
}
