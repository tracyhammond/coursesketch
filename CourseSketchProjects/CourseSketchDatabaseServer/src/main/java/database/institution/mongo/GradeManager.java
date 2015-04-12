package database.institution.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.grading.Grading.ProtoGrade;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ASSIGNMENT_COLLECTION;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COMMENT;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_COLLECTION;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.CURRENT_GRADE;
import static database.DatabaseStringConstants.EXISTS;
import static database.DatabaseStringConstants.EXTERNAL_GRADE;
import static database.DatabaseStringConstants.GRADED_DATE;
import static database.DatabaseStringConstants.GRADE_COLLECTION;
import static database.DatabaseStringConstants.GRADE_HISTORY;
import static database.DatabaseStringConstants.GRADE_VALUE;
import static database.DatabaseStringConstants.USER_ID;
import static database.DatabaseStringConstants.WHO_CHANGED;

/**
 * Interfaces with mongo database to manage grades. Used for gradebook functionality mostly.
 *
 * In the mongo database, a grade has the following structure.
 *
 * Grade
 *  {
 *      courseId: ID,
 *      userId: ID,
 *      assignmentId: ID,
 *      problemId: ID,
 *      currentGrade: float,
 *      gradeHistory: [
 *          {
 *              gradeValue: float,
 *              comment: String,
 *              gradedDate: Date,
 *              whoChanged: ID      // ID of the user who changed the grade
 *          },
 *          {
 *              More of the same
 *          }
 *      ]
 *      externalGrade: bool // True if the currentGrade was not generated by auto grading
 *  }
 *
 * Created by Matt on 3/29/2015.
 */
public final class GradeManager {

    /**
     * Private constructor.
     */
    private GradeManager() {
    }

    /**
     * Adds the specified grade if it does not exist. If it does exist, updates the grade value in the database.
     * The code block is an example of what happens when a new problem grade is added.
     * <pre><code>
     * coll.update(
     *  { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId },
     *  {   $addToSet: { gradeHistory: { $each: [gradeToInsertDBObject], $sort: { GRADED_DATE: -1 }}}
     *      $set: { CURRENT_GRADE: currentGrade }
     *      $setOnInsert: { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId }
     *  },
     *  { upsert: true }
     * )
     * </code></pre>
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grade is being added to.
     * @param adderId
     *         The Id of the person trying to add the grade.
     * @param grade
     *         The ProtoObject representing the grade to be added.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to add the grade.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    public static void addGrade(final Authenticator authenticator, final DB dbs, final String adderId, final ProtoGrade grade)
            throws AuthenticationException, DatabaseAccessException {
        // Check authentication so only teachers of the course can add grades
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, grade.getCourseId(), adderId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        auth.clear();
        auth.setCheckUser(true);
        checkUserInGradeTypeCollection(authenticator, auth, grade);

        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);

        final BasicDBObject query = new BasicDBObject(COURSE_ID, grade.getCourseId()).append(USER_ID, grade.getUserId());
        final BasicDBObject setOnInsertFields = new BasicDBObject(query); // Initially setOnInsertFields and query are the same.

        // If the protoGrade has an assignmentId, add it to the query and setOnInsertFields.
        // Else we are looking for a course grade probably which doesn't have an assignmentId.
        if (grade.hasAssignmentId()) {
            query.append(ASSIGNMENT_ID, grade.getAssignmentId());
            setOnInsertFields.append(ASSIGNMENT_ID, grade.getAssignmentId());
        } else {
            query.append(ASSIGNMENT_ID, new BasicDBObject(EXISTS, false));
        }

        // If the protoGrade has a problemId, add it to the query and setOnInsertFields.
        // Else we are looking for something that isn't a problem grade so it does not have a problemId.
        if (grade.hasProblemId()) {
            query.append(COURSE_PROBLEM_ID, grade.getProblemId());
            setOnInsertFields.append(COURSE_PROBLEM_ID, grade.getProblemId());
        } else {
            query.append(COURSE_PROBLEM_ID, new BasicDBObject(EXISTS, false));
        }

        if (grade.hasExternalGrade()) {
            setOnInsertFields.append(EXTERNAL_GRADE, grade.getExternalGrade());
        }

        if (grade.getGradeHistoryCount() <= 0) {
            throw new DatabaseAccessException("A grade history is required to insert a grade.");
        }

        int mostRecentHistoryIndex = -1;
        long mostRecentHistoryTime = -1;
        final BasicDBList gradeHistoryList = new BasicDBList();
        for (int i = 0; i < grade.getGradeHistoryCount(); i++) {
            final BasicDBObject gradeHistoryObject = buildMongoGradeHistory(grade.getGradeHistory(i));
            gradeHistoryList.add(gradeHistoryObject);

            if ((long) gradeHistoryObject.get(GRADED_DATE) > mostRecentHistoryTime) {
                mostRecentHistoryTime = (long) gradeHistoryObject.get(GRADED_DATE);
                mostRecentHistoryIndex = i;
            }
        }
        final BasicDBObject updateObject = new BasicDBObject(ADD_SET_COMMAND, new BasicDBObject(GRADE_HISTORY,
                new BasicDBObject("$each", gradeHistoryList).append("$sort", new BasicDBObject(GRADED_DATE, -1))));

        // Sets currentGrade if the ProtoGrade has the field. Else sets currentGrade base on gradeHistory if the ProtoGrade has a gradeHistory.
        updateObject.append("$set", new BasicDBObject(CURRENT_GRADE, grade.getGradeHistory(mostRecentHistoryIndex).getGradeValue()));

        updateObject.append("$setOnInsert", setOnInsertFields);

        // Query, update, upsert (true), multi-document-update (false).
        gradeCollection.update(query, updateObject, true, false);

    }

    /**
     * Checks if the user is in the corresponding collection for the grade type.
     * If it is a problem grade, checks problem collection. Assignment grade checks assignment collection.
     * Course grade or external grade checks course collection.
     *
     * @param authenticator The object that is performing authentication.
     * @param auth The authentication being performed.
     * @param grade The grade object the authentication is being performed on.
     * @throws DatabaseAccessException if the user is not in the respective collection for the grade type being checked.
     */
    private static void checkUserInGradeTypeCollection(final Authenticator authenticator, final Authenticator.AuthType auth, final ProtoGrade grade)
            throws DatabaseAccessException {
        if (grade.getExternalGrade()) {
            if (!authenticator.isAuthenticated(COURSE_COLLECTION, grade.getCourseId(), grade.getUserId(), 0, auth)) {
                throw new DatabaseAccessException("The user is not in the course that the grade is being inserted for.");
            }
        } else if (grade.hasProblemId()) {
            if (!authenticator.isAuthenticated(COURSE_PROBLEM_COLLECTION, grade.getProblemId(), grade.getUserId(), 0, auth)) {
                throw new DatabaseAccessException("The user is not assigned the problem that the grade is being inserted for.");
            }
        } else if (grade.hasAssignmentId()) {
            if (!authenticator.isAuthenticated(ASSIGNMENT_COLLECTION, grade.getAssignmentId(), grade.getUserId(), 0, auth)) {
                throw new DatabaseAccessException("Th user is not assigned the assignment that the grade is being inserted for.");
            }
        } else {
            if (!authenticator.isAuthenticated(COURSE_COLLECTION, grade.getCourseId(), grade.getUserId(), 0, auth)) {
                throw new DatabaseAccessException("The user is not in the course that the grade is being inserted for.");
            }
        }
    }

    /**
     * Builds a mongo BasicDBObject for a single grade history value.
     *
     * @param gradeHistory
     *         ProtoObject that grade history is being set from.
     * @return BasicDBObject representing the single grade history value in mongo.
     *
     * Package-private
     */
    static BasicDBObject buildMongoGradeHistory(final GradeHistory gradeHistory) {
        final BasicDBObject gradeHistoryDBObject = new BasicDBObject();

        if (gradeHistory.hasGradeValue()) {
            gradeHistoryDBObject.append(GRADE_VALUE, gradeHistory.getGradeValue());
        }

        if (gradeHistory.hasComment()) {
            gradeHistoryDBObject.append(COMMENT, gradeHistory.getComment());
        }

        if (gradeHistory.hasGradedDate()) {
            gradeHistoryDBObject.append(GRADED_DATE, gradeHistory.getGradedDate().getMillisecond());
        }

        if (gradeHistory.hasWhoChanged()) {
            gradeHistoryDBObject.append(WHO_CHANGED, gradeHistory.getWhoChanged());
        }

        return gradeHistoryDBObject;
    }

    /**
     * Finds a single grade for a student in a course. If fields are not required in the search, pass in null.
     * For example, if looking for a particular assignment grade, pass in null for the problemId parameter.
     * If looking for a specific problem grade, you must pass in the assignmentId as well as the problemId.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grades are being retrieved from.
     * @param requesterId
     *         The id of the user requesting the grade. This is required.
     * @param userId
     *         The id of the user that the grade is for. This is required.
     * @param courseId
     *         The id of the course that the grade is for. This is required.
     * @param assignmentId
     *         The id of the assignment that the grade is for. This is optional.
     * @param problemId
     *         The id of the problem that the grade is for. This is optional.
     * @return ProtoGrade object representing the grade requested.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if a grade is not found in the database matching the requested parameters.
     */
    public static ProtoGrade getGrade(final Authenticator authenticator, final DB dbs, final String requesterId, final String userId,
            final String courseId, final String assignmentId, final String problemId) throws AuthenticationException, DatabaseAccessException {
        final Authenticator.AuthType auth = new Authenticator.AuthType();

        // If requester is the user for the grade, check if they are in the course.
        // If requester is not the user for the grade, check if they are an admin for the course.
        if (requesterId.equals(userId)) {
            auth.setCheckUser(true);
        } else {
            auth.setCheckAdminOrMod(true);
        }

        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, requesterId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_ID, courseId).append(USER_ID, userId);

        // Adds to query to look for documents without assignmentId field if assignmentId is not given.
        if (assignmentId != null) {
            query.append(ASSIGNMENT_ID, assignmentId);
        } else {
            query.append(ASSIGNMENT_ID, new BasicDBObject(EXISTS, false));
        }

        // Adds to query to look for documents without problemId field if problemId is not given.
        if (problemId != null) {
            query.append(COURSE_PROBLEM_ID, problemId);
        } else {
            query.append(COURSE_PROBLEM_ID, new BasicDBObject(EXISTS, false));
        }

        final DBCursor cursor = gradeCollection.find(query);
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Did not find a grade matching those parameters for that student in course: " + courseId);
        }

        return buildProtoGrade(cursor.next());
    }

    /**
     * Gets all assignment grades for a certain course. It may not contain grades for all assignments for all users.
     * If an assignment does not have any assignment grades yet, it will not appear in the result.
     * Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     * The query and sort is shown below.
     *
     * <pre><code>
     *     coll.find( { COURSE_ID: courseId, COURSE_PROBLEM_ID: { "exists", false } } )
     *     .sort( { ASSIGNMENT_ID: 1, USER_ID: 1 } )
     * </code></pre>
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grades are being retrieved from.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param requesterId
     *         The user that is requesting the grades. Only users with admin access can get all grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllAssignmentGradesInstructor(final Authenticator authenticator, final DB dbs, final String courseId,
            final String requesterId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication so only teachers of the course can retrieve all grades
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, requesterId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_ID, courseId)
                .append(COURSE_PROBLEM_ID, new BasicDBObject(EXISTS, false));
        final BasicDBObject sortMethod = new BasicDBObject(ASSIGNMENT_ID, 1).append(USER_ID, 1); // Sort by assignmentId then userId
        final DBCursor cursor = gradeCollection.find(query).sort(sortMethod);
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Grades were not found for course with ID " + courseId);
        }

        final List<ProtoGrade> grades = new ArrayList<>();
        while (cursor.hasNext()) {
            grades.add(buildProtoGrade(cursor.next()));
        }

        return grades;
    }

    /**
     * Gets all grades for a certain student in a certain course. It may not contain grades for all assignments.
     * If an assignment does not have an assignment grade yet, it will not appear in the result.
     * Sorted in ascending order by assignmentId.
     * This does not mean the list will be in chronological or alphabetical order.
     * The query and sort is shown below.
     *
     * <pre><code>
     *      coll.find( { COURSE_ID: courseId, USER_ID: userId, COURSE_PROBLEM_ID: { "exists", false } } )
     *     .sort( { ASSIGNMENT_ID: 1 } )
     * </code></pre>
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grades are being retrieved from.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param requesterId
     *         The user that is requesting the grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllAssignmentGradesStudent(final Authenticator authenticator, final DB dbs, final String courseId,
            final String requesterId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication to make sure the user is in the course
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckUser(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, requesterId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_ID, courseId)
                .append(USER_ID, requesterId)
                .append(COURSE_PROBLEM_ID, new BasicDBObject(EXISTS, false));
        final BasicDBObject sortMethod = new BasicDBObject(ASSIGNMENT_ID, 1); // Sort by assignmentId
        final DBCursor cursor = gradeCollection.find(query).sort(sortMethod);
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Grades were not found for specific user in course " + courseId);
        }

        final List<ProtoGrade> grades = new ArrayList<>();
        while (cursor.hasNext()) {
            grades.add(buildProtoGrade(cursor.next()));
        }

        return grades;
    }

    /**
     * Takes in a grade mongo DBObject and returns a ProtoObject matching the data in the DBObject.
     *
     * @param grade
     *         Mongo DBObject representing one assignment grade for one student.
     * @return ProtoObject representing one assignment grade for one student.
     * @throws DatabaseAccessException
     *         Thrown if courseId or userId are not found in the DBObject.
     *
     * Package-private
     */
    static ProtoGrade buildProtoGrade(final DBObject grade) throws DatabaseAccessException {
        if (!grade.containsField(COURSE_ID)) {
            throw new DatabaseAccessException("Missing required field: courseId");
        }
        if (!grade.containsField(USER_ID)) {
            throw new DatabaseAccessException("Missing required field: userId");
        }

        final ProtoGrade.Builder protoGrade = ProtoGrade.newBuilder();
        protoGrade.setCourseId(grade.get(COURSE_ID).toString());
        protoGrade.setUserId(grade.get(USER_ID).toString());

        if (grade.containsField(ASSIGNMENT_ID)) {
            protoGrade.setAssignmentId(grade.get(ASSIGNMENT_ID).toString());
        }

        if (grade.containsField(COURSE_PROBLEM_ID)) {
            protoGrade.setProblemId(grade.get(COURSE_PROBLEM_ID).toString());
        }

        if (grade.containsField(CURRENT_GRADE)) {
            protoGrade.setCurrentGrade((float) grade.get(CURRENT_GRADE));
        }

        if (grade.containsField(GRADE_HISTORY)) {
            final List<DBObject> history = (List<DBObject>) grade.get(GRADE_HISTORY);
            for (int i = 0; i < history.size(); i++) {
                protoGrade.addGradeHistory(buildProtoGradeHistory(history.get(i)));
            }
        }

        if (grade.containsField(EXTERNAL_GRADE)) {
            protoGrade.setExternalGrade((boolean) grade.get(EXTERNAL_GRADE));
        }

        return protoGrade.build();
    }

    /**
     * Takes in a gradeHistory mongo DBObject and returns a ProtoObject matching the data in the DBObject.
     *
     * @param history
     *         Mongo DBObject representing grade history for one assignment grade for one user.
     * @return ProtoObject representing the grade history for one assignment grade for one user.
     * @throws DatabaseAccessException
     *         Thrown if no grade history fields are found.
     *
     * Package-private
     */
    static GradeHistory buildProtoGradeHistory(final DBObject history) throws DatabaseAccessException {
        final GradeHistory.Builder protoHistory = GradeHistory.newBuilder();
        if (history.containsField(GRADE_VALUE)) {
            protoHistory.setGradeValue((float) history.get(GRADE_VALUE));
        }

        if (history.containsField(COMMENT)) {
            protoHistory.setComment(history.get(COMMENT).toString());
        }

        if (history.containsField(GRADED_DATE)) {
            protoHistory.setGradedDate(RequestConverter.getProtoFromMilliseconds(((Number) history.get(GRADED_DATE)).longValue()));
        }

        if (history.containsField(WHO_CHANGED)) {
            protoHistory.setWhoChanged(history.get(WHO_CHANGED).toString());
        }

        // GetAllFields returns a map of fields that have values in the ProtoObject
        // If the size of the map is 0, then no fields exist
        if (protoHistory.getAllFields().size() == 0) {
            throw new DatabaseAccessException("No fields found for gradeHistory object");
        }

        return protoHistory.build();
    }
}
