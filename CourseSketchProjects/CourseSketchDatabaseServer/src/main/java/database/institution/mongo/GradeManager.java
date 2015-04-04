package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COMMENT;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
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
     *  {   $push: { gradeHistory: { $each: [gradeToInsertDBObject], $sort: { GRADED_DATE: -1 }}}
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
        final BasicDBObject gradeHistoryObject = buildMongoGradeHistory(grade.getGradeHistory(0));
        final BasicDBObject updateObject = new BasicDBObject("$push", new BasicDBObject(GRADE_HISTORY, new BasicDBObject("$each", gradeHistoryObject)
                .append("$sort", new BasicDBObject(GRADED_DATE, -1))));
        if (grade.hasCurrentGrade()) {
            updateObject.append("$set", new BasicDBObject(CURRENT_GRADE, grade.getCurrentGrade()));
        }
        updateObject.append("$setOnInsert", setOnInsertFields);

        // Query, update, upsert (true), multi-document-update (false).
        gradeCollection.update(query, updateObject, true, false);

    }

    /**
     * Builds a mongo BasicDBObject for a single grade history value.
     *
     * @param gradeHistory
     *         ProtoObject that grade history is being set from.
     * @return BasicDBObject representing the single grade history value in mongo.
     */
    private static BasicDBObject buildMongoGradeHistory(final GradeHistory gradeHistory) {
        final BasicDBObject temp = new BasicDBObject();

        if (gradeHistory.hasGradeValue()) {
            temp.append(GRADE_VALUE, gradeHistory.getGradeValue());
        }

        if (gradeHistory.hasComment()) {
            temp.append(COMMENT, gradeHistory.getComment());
        }

        if (gradeHistory.hasGradedDate()) {
            temp.append(GRADED_DATE, gradeHistory.getGradedDate());
        }

        if (gradeHistory.hasWhoChanged()) {
            temp.append(WHO_CHANGED, gradeHistory.getWhoChanged());
        }

        return temp;
    }

    /**
     * Gets all grades for a certain course. Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grades are being retrieved from.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param userId
     *         The user that is requesting the grades. Only users with admin access can get all grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllCourseGradesInstructor(final Authenticator authenticator, final DB dbs, final String courseId,
            final String userId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication so only teachers of the course can retrieve all grades
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, userId, 0, auth)) {
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
     * Gets all grades for a certain student in a certain course. Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the grades are being retrieved from.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param userId
     *         The user that is requesting the grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllCourseGradesStudent(final Authenticator authenticator, final DB dbs, final String courseId,
            final String userId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication to make sure the user is in the course
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckUser(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_ID, courseId)
                .append(USER_ID, userId)
                .append(COURSE_PROBLEM_ID, new BasicDBObject(EXISTS, false));
        final BasicDBObject sortMethod = new BasicDBObject(ASSIGNMENT_ID, 1).append(USER_ID, 1); // Sort by assignmentId then userId
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
     * @param grade
     *         Mongo DBObject representing one assignment grade for one student.
     * @return ProtoObject representing one assignment grade for one student.
     * @throws DatabaseAccessException
     *         Thrown if courseId or userId are not found in the DBObject.
     */
    private static ProtoGrade buildProtoGrade(final DBObject grade) throws DatabaseAccessException {
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
     * @param history
     *         Mongo DBObject representing grade history for one assignment grade for one user.
     * @return ProtoObject representing the grade history for one assignment grade for one user.
     * @throws DatabaseAccessException
     *         Thrown if no grade history fields are found.
     */
    private static GradeHistory buildProtoGradeHistory(final DBObject history) throws DatabaseAccessException {
        final GradeHistory.Builder protoHistory = GradeHistory.newBuilder();
        if (history.containsField(GRADE_VALUE)) {
            protoHistory.setGradeValue((float) history.get(GRADE_VALUE));
        }

        if (history.containsField(COMMENT)) {
            protoHistory.setComment(history.get(COMMENT).toString());
        }

        if (history.containsField(GRADED_DATE)) {
            protoHistory.setGradedDate((Util.DateTime) history.get(GRADED_DATE));
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
